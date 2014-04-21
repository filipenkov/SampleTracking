package com.atlassian.jira.collector.plugin.transformer;

import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.AbstractStringTransformedDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A webresource transformer that replaces the full base url in a webresource!
 *
 * @since 1.0
 */
public class ScriptletTransformer implements WebResourceTransformer
{

    @Override
    public DownloadableResource transform(final Element element, final ResourceLocation resourceLocation, final String s, final DownloadableResource downloadableResource)
    {
        return new JavascriptSubstitutionDownloadableResource(downloadableResource);
    }

    static class JavascriptSubstitutionDownloadableResource
            extends AbstractStringTransformedDownloadableResource
    {
        private static final Pattern VARIABLE_PATTERN = Pattern.compile("@([a-zA-Z][a-zA-Z0-9_]*)");
        private final Map<String, String> params = new HashMap<String, String>();

        public JavascriptSubstitutionDownloadableResource(DownloadableResource originalResource)
        {
            super(originalResource);
            params.put("baseUrl", JiraUrl.constructBaseUrl(ExecutingHttpRequest.get()));
            params.put("contextPath", ExecutingHttpRequest.get().getContextPath());
        }

        @Override
        protected String transform(final String input)
        {
            final Matcher matcher = VARIABLE_PATTERN.matcher(input);
            int start = 0;
            StringBuilder out = null;
            while (matcher.find())
            {
                if (out == null)
                {
                    out = new StringBuilder();
                }

                out.append(input.subSequence(start, matcher.start()));
                String token = matcher.group(1);
                String subst = params.get(token);
                if (subst != null)
                {
                    out.append(subst);
                }
                else
                {
                    out.append(matcher.group());
                }
                start = matcher.end();
            }
            if (out == null)
            {
                return input;
            }
            else
            {
                out.append(input.subSequence(start, input.length()));
                return out.toString();
            }
        }
    }
}
