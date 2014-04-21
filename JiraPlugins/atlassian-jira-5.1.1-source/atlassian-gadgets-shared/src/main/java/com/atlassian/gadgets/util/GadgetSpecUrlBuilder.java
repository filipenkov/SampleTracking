package com.atlassian.gadgets.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;
import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.plugin.Plugin;
import com.atlassian.sal.api.ApplicationProperties;

import static com.atlassian.gadgets.util.Uri.decodeUriComponent;
import static com.atlassian.gadgets.util.Uri.encodeUriComponent;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builds URLs to gadget specs that are hosted in plugins.
 */
public class GadgetSpecUrlBuilder
{
    private final ApplicationProperties applicationProperties;

    /**
     * Constructs a new {@code GadgetSpecUrlBuilder} that uses the specified {@code ApplicationProperties} to determine
     * the application base URL.
     *
     * @param applicationProperties {@code ApplicationProperties} implementation of the host application.  Must not be
     *                              {@code null}, or a {@code NullPointerException} will be thrown.
     * @throws NullPointerException if {@code applicationProperties} is {@code null}
     */
    public GadgetSpecUrlBuilder(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
    }
    
    /**
     * Builds a URL for the feed containing all the gadget specs published by this application.
     * 
     * @return URL for the feed containing all the gadget specs published by this application
     */
    public String buildGadgetSpecFeedUrl()
    {
        return applicationProperties.getBaseUrl() + "/" + getBaseGadgetSpecUrl() + "feed";
    }

    /**
     * Builds a URL for the gadget spec that can be found with the {@code Plugin}.
     *
     * @param pluginKey key of the {@link Plugin} in which the gadget spec can be found.  Must not be {@code null},
     *                  or a {@code NullPointerException} will be thrown.
     * @param location  path to the gadget spec resource in the plugin. Must not be {@code null}, or a {@code
     *                  NullPointerException} will be thrown.
     * @param moduleKey key of the {@link GadgetModuleDescriptor}.  Must not be {@code null},
     *                  or a {@code NullPointerException} will be thrown.
     * @return URL that the gadget spec will be made available on
     * @throws NullPointerException if any argument is {@code null}
     */
    public String buildGadgetSpecUrl(String pluginKey, String moduleKey, String location)
    {
        checkNotNull(pluginKey, "pluginKey");
        checkNotNull(moduleKey, "moduleKey");
        checkNotNull(location, "location");

        StringBuilder sb = new StringBuilder();
        sb.append(getBaseGadgetSpecUrl());
        sb.append(encodeUriComponent(pluginKey));
        sb.append(':');
        sb.append(encodeUriComponent(moduleKey));
        for (String component : location.split("\\/"))
        {
            sb.append('/');
            sb.append(encodeUriComponent(component));
        }
        return sb.toString();
    }

    /**
     * Parses a gadget spec URL in the format returned from {@link #buildGadgetSpecUrl(String, String)} into a {@code
     * PluginResourceKey} containing the plugin key and resource location specified by the URL.  The URL must contain
     * the standard resource prefix, and both a plugin key and resource location in the path.  It may contain a module
     * key after the plugin key separated by a colon. If the URL is absolute, it must match the base URL for this
     * application.
     * <p/>
     * For example:
     * <ul>
     * <li>Relative URL without module key: {@code rest/gadgets/1.0/g/test.plugin/path/to/gadget.xml} </li>
     * <li>Absolute URL without module key: {@code http://localhost:8080/dashboards/rest/gadgets/1.0/g/test.plugin/path/to/gadget.xml})</li>
     * <li>Relative URL with module key: {@code rest/gadgets/1.0/g/test.plugin:module-key/path/to/gadget.xml}</li>
     * <li>Absolute URL with module key: {@code http://localhost:8080/dashboards/rest/gadgets/1.0/g/test.plugin:module-key/path/to/gadget.xml})</li>
     * </ul>
     * 
     * @param gadgetSpecUrl a valid, URL-encoded gadget spec URL, as produced by {@link #buildGadgetSpecUrl(String,
     *                      String)} or {@link #buildGadgetSpecUrl(com.atlassian.gadgets.plugins.PluginGadgetSpec.Key)}.
     *                      May be absolute, if the base URL matches the base URL retrieved from the {@link
     *                      ApplicationProperties} instance this {@code GadgetSpecUrlBuilder} was initialized with. Must
     *                      not be {@code null}, or a {@code NullPointerException} will be thrown.
     * @return a {@code PluginResourceKey} built from the plugin key and resource location parsed and decoded from the
     *         URL
     * @throws GadgetSpecUriNotAllowedException
     *                              if the URL does not start with the prefix for gadget specs published from this
     *                              application, or if either the plugin key or resource location is missing or blank
     * @throws NullPointerException if {@code gadgetSpecUrl} is {@code null}
     */
    public PluginGadgetSpec.Key parseGadgetSpecUrl(String gadgetSpecUrl)
    {
        checkNotNull(gadgetSpecUrl, "gadgetSpecUrl");

        Matcher validGadgetSpecUrlMatcher = validGadgetSpecUrlPattern().matcher(gadgetSpecUrl);
        if (!validGadgetSpecUrlMatcher.matches())
        {
            throw new GadgetSpecUriNotAllowedException("URL is not a gadget spec URL for this server " + gadgetSpecUrl);
        }

        String pluginKey = decodeUriComponent(validGadgetSpecUrlMatcher.group(1));
        String location = decodeUriComponent(validGadgetSpecUrlMatcher.group(2));
        return new PluginGadgetSpec.Key(pluginKey, location);
    }

    private String getBaseGadgetSpecUrl()
    {
        return "rest/gadgets/1.0/g/";
    }

    private Pattern validGadgetSpecUrlPattern()
    {
        return Pattern.compile(
            "(?:" + Pattern.quote(applicationProperties.getBaseUrl()) + "/)?" + // optional base URL
            Pattern.quote(getBaseGadgetSpecUrl()) +
            "([^/:]+)(?::[^/]+)?/(.+)" // plugin key (group 1) : module key (group 2) / path to spec (group 3)
        );
    }
}
