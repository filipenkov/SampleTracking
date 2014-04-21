package com.atlassian.gadgets.publisher.internal.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.publisher.internal.GadgetProcessor;
import com.atlassian.gadgets.util.Uri;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.LocaleResolver;

import com.google.common.collect.ImmutableList;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringEscapeUtils.escapeXml;

/**
 * Default implementation of {@link GadgetProcessor} that handles host-specific
 * base URLs and the pseudomacro #requireResource.
 */
public class GadgetProcessorImpl implements GadgetProcessor
{
    private static final String ATLASSIAN_BASE_URL = "__ATLASSIAN_BASE_URL__";

    // a best guess at allowable plugin key characters: alphanumerics, dots,
    // backslashes, a single space, and a dash. Couldn't find an authoritative
    // reference...
    private static final String VALID_KEY_CHARS_REGEXP = "[ \\p{Alnum}\\.\\\\-]+";
    private static final String PLUGIN_KEY_REGEXP = VALID_KEY_CHARS_REGEXP + ":" + VALID_KEY_CHARS_REGEXP;
    private static final String RESOURCE_PATH_REGEXP = VALID_KEY_CHARS_REGEXP + "(/" + VALID_KEY_CHARS_REGEXP + ")*";
    private static final Pattern REQUIRE_RESOURCE = Pattern.compile(
            "#requireResource\\(" +          // opening part of macro with # included
            "\"(" + PLUGIN_KEY_REGEXP + ")\"" +    // module-complete key (numbers and letters plus a separator dot)
            "\\)"                              // closing part of macro
            );
    private static final Pattern STATIC_RESOURCE_URL = Pattern.compile(
            "#staticResourceUrl\\(" +          // opening part of macro with # included
            "\"(" + PLUGIN_KEY_REGEXP + ")\"" +    // module-complete key (numbers and letters plus a separator dot)
            ",\\p{Space}*" +                              // argument separator
            "\"(" + RESOURCE_PATH_REGEXP + ")\"" +    // resource name
            "\\)"                              // closing part of macro
            );
    private static final Pattern GET_SUPPORTED_LOCALES = Pattern.compile(
            "#supportedLocales\\(" + // opening part of macro with # included
            "\"((" + VALID_KEY_CHARS_REGEXP + ",*)*"  + ")\"" +    // prefix (numbers and letters plus a separator dot), can be chained together with a comma
            "\\)"                              // closing part of macro
            );
    private static final Pattern INCLUDE_RESOURCES = Pattern.compile(
            "#includeResources(?:\\(\\))?"
    );
    private static final Pattern OAUTH = Pattern.compile("#oauth");

    // regexp indices for the parts of requireResource, staticResourceUrl, and getSupportedLocales we need
    private static final int MESSAGES_PREFIX = 1;
    private static final int RESOURCE_NAME = 2;
    private static final int MODULE_COMPLETE_PLUGIN_KEY = 1;
    
    private final Log log = LogFactory.getLog(GadgetProcessorImpl.class);

    private final ApplicationProperties applicationProperties;
    private final WebResourceManager webResourceManager;
    private final LocaleResolver localeResolver;
    private final I18nResolver i18nResolver;

    /**
     * Constructor.
     * @param applicationProperties provides the application-specific base URL
     * @param webResourceManager used to insert links to web resources into the gadget
     * @param localeResolver used to find supported {@code Locale}s
     * @param i18nResolver used to retrieve all i18n properties that match a specified prefix
     */
    public GadgetProcessorImpl(ApplicationProperties applicationProperties,
                               WebResourceManager webResourceManager,
                               LocaleResolver localeResolver,
                               I18nResolver i18nResolver)
    {
        this.applicationProperties = applicationProperties;
        this.webResourceManager = webResourceManager;
        this.localeResolver = localeResolver;
        this.i18nResolver = i18nResolver;
    }

    public void process(final InputStream in, final OutputStream out) throws GadgetParsingException
    {
        checkNotNull(in);
        checkNotNull(out);
        try
        {
            // TODO major character encoding issues here! AG-361
            String gadgetString = IOUtils.toString(in, "UTF-8");
            gadgetString = processAtlassianBaseUrl(gadgetString);
            gadgetString = processGetSupportedLocales(gadgetString);
            gadgetString = processIncludeResources(gadgetString);
            gadgetString = processStaticResourceUrl(gadgetString);
            gadgetString = processOAuth(gadgetString);
            out.write(gadgetString.getBytes("UTF-8"));
        }
        catch (IOException e)
        {
            throw new GadgetParsingException(e);
        }
    }

    private String processAtlassianBaseUrl(final String gadgetSpecString)
    {
        String baseUrl = applicationProperties.getBaseUrl();
        if (StringUtils.isNotBlank(baseUrl))
        {
            return gadgetSpecString.replace(ATLASSIAN_BASE_URL, baseUrl);
        }
        else
        {
            log.warn("GadgetProcessorImpl: empty application base URL; "
                     + "processed gadget spec may not be valid");
            return gadgetSpecString;
        }
    }

    private String processGetSupportedLocales(final String gadgetSpecString) throws IOException
    {
        // Matcher.appendReplacement and appendTail don't work with StringBuilder in Java 5 or 6.
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5066679
        StringBuffer processedGadgetSpec = new StringBuffer();
        Matcher matcher = GET_SUPPORTED_LOCALES.matcher(gadgetSpecString);
        Set<Locale> locales = localeResolver.getSupportedLocales();

        if (matcher.find())
        {
            Iterable<String> prefixes = ImmutableList.of(matcher.group(MESSAGES_PREFIX).split(","));
            StringBuilder supportedLocalesString = new StringBuilder();

            // create the Locale element for the default message file
            createLocaleElement(supportedLocalesString, prefixes, new Locale(""));

            // create the Locale elements for the supported Locales
            for (Locale locale : locales)
            {
                createLocaleElement(supportedLocalesString, prefixes, locale);
            }

            matcher.appendReplacement(processedGadgetSpec, Matcher.quoteReplacement(supportedLocalesString.toString()));
        }

        matcher.appendTail(processedGadgetSpec);
        return processedGadgetSpec.toString();
    }

    private void createLocaleElement(final Appendable supportedLocalesString,
                                     final Iterable<String> prefixes,
                                     final Locale locale)
        throws IOException
    {

        supportedLocalesString.append("<Locale");
        if (StringUtils.isNotEmpty(locale.getLanguage()))
        {
            supportedLocalesString.append(" lang=\"");
            supportedLocalesString.append(escapeXml(locale.getLanguage()));
            supportedLocalesString.append("\"");
        }
        if (StringUtils.isNotEmpty(locale.getCountry()))
        {
            supportedLocalesString.append(" country=\"");
            supportedLocalesString.append(escapeXml(locale.getCountry()));
            supportedLocalesString.append("\"");
        }
        supportedLocalesString.append(">");

        supportedLocalesString.append("<messagebundle>");
        for (String prefix : prefixes)
        {
            Map<String, String> messages = i18nResolver.getAllTranslationsForPrefix(prefix, locale);
            for (Map.Entry<String, String> message : messages.entrySet())
            {

                supportedLocalesString
                    .append("<msg name=\"").append(escapeXml(message.getKey())).append("\">")
                    .append(escapeXml(message.getValue()))
                    .append("</msg>");
            }
        }
        supportedLocalesString.append("</messagebundle>");

        supportedLocalesString.append("</Locale>");
    }

    private String processIncludeResources(final String gadgetSpecString) throws GadgetParsingException
    {
        StringBuffer processedGadgetSpec = new StringBuffer();
        Matcher matcher = INCLUDE_RESOURCES.matcher(gadgetSpecString);
        int startSearchSpace = 0;
        // TODO : this is an existing bug; this code won't work if there are multiple matches
        while (matcher.find())
        {
            String resourceSearchString = gadgetSpecString.substring(startSearchSpace, matcher.start());
            List<String> collectedResources = new ArrayList<String>();
            processedGadgetSpec.append(processRequireResource(resourceSearchString, collectedResources));
            StringWriter tagWriter = new StringWriter();
            webResourceManager.includeResources(collectedResources, tagWriter, UrlMode.ABSOLUTE);
            processedGadgetSpec.append(tagWriter.toString());
            startSearchSpace = matcher.end();
        }
        processedGadgetSpec.append(gadgetSpecString.substring(startSearchSpace));

        return processedGadgetSpec.toString();
    }

    private String processRequireResource(final String resourceSearchSpace, List<String> resources) throws GadgetParsingException
    {
        StringBuffer processedGadgetSpec = new StringBuffer();
        Matcher matcher = REQUIRE_RESOURCE.matcher(resourceSearchSpace);
        while (matcher.find())
        {
            String moduleCompleteKey = matcher.group(MODULE_COMPLETE_PLUGIN_KEY);
            resources.add(moduleCompleteKey);
            matcher.appendReplacement(processedGadgetSpec, "");
        }
        matcher.appendTail(processedGadgetSpec);

        return processedGadgetSpec.toString();
    }

    private String processStaticResourceUrl(final String gadgetSpecString) throws GadgetParsingException
    {
        StringBuffer processedGadgetSpec = new StringBuffer();
        Matcher matcher = STATIC_RESOURCE_URL.matcher(gadgetSpecString);
        while (matcher.find())
        {
            String moduleCompleteKey = matcher.group(MODULE_COMPLETE_PLUGIN_KEY);
            String resourceName = matcher.group(RESOURCE_NAME);
            String staticResourceUrl =
                webResourceManager.getStaticPluginResource(moduleCompleteKey, resourceName, UrlMode.ABSOLUTE);
            matcher.appendReplacement(processedGadgetSpec, Matcher.quoteReplacement(staticResourceUrl));
        }
        matcher.appendTail(processedGadgetSpec);

        return processedGadgetSpec.toString();
    }
    
    private String processOAuth(String gadgetSpec)
    {
        StringBuffer processedGadgetSpec = new StringBuffer();
        Matcher matcher = OAUTH.matcher(gadgetSpec);
        if (matcher.find())
        {
            matcher.appendReplacement(processedGadgetSpec, Matcher.quoteReplacement(createOAuthElement()));
        }
        matcher.appendTail(processedGadgetSpec);

        return processedGadgetSpec.toString();
    }

    private static final String ACCESS_TOKEN_PATH = "/plugins/servlet/oauth/access-token";
    private static final String REQUEST_TOKEN_PATH = "/plugins/servlet/oauth/request-token";
    private static final String AUTHORIZE_PATH = "/plugins/servlet/oauth/authorize";
    private static final String OAUTH_CALLBACK = "http://oauth.gmodules.com/gadgets/oauthcallback";
    private String createOAuthElement()
    {
        StringBuilder builder = new StringBuilder("<OAuth><Service>");
        builder.append("<Access url=\"");
        builder.append(applicationProperties.getBaseUrl());
        builder.append(ACCESS_TOKEN_PATH);
        builder.append("\" method=\"POST\" />");
        builder.append("<Request url=\"");
        builder.append(applicationProperties.getBaseUrl());
        builder.append(REQUEST_TOKEN_PATH);
        builder.append("\" method=\"POST\" />");
        builder.append("<Authorization url=\"");
        builder.append(applicationProperties.getBaseUrl());
        builder.append(AUTHORIZE_PATH);
        builder.append("?oauth_callback=");
        builder.append(Uri.encodeUriComponent(OAUTH_CALLBACK));
        builder.append("\" /></Service></OAuth>");
        return builder.toString();
    }
}
