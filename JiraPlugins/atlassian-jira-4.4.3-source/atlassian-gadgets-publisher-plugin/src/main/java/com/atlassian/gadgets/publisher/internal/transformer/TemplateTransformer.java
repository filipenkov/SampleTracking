package com.atlassian.gadgets.publisher.internal.transformer;

import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.AbstractStringTransformedDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import org.dom4j.Element;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 */
public class TemplateTransformer implements WebResourceTransformer
{
    public DownloadableResource transform(Element configElement, ResourceLocation location, String filePath, DownloadableResource nextResource)
    {
        return new TemplateResource(nextResource);
    }

    /**
     *
     */
    private static class TemplateResource extends AbstractStringTransformedDownloadableResource
    {
        private static final Map<Pattern,String> PATTERNS = new LinkedHashMap<Pattern,String>()
        {{
                put(Pattern.compile("[\\r\\t\\n]"), "");
                put(Pattern.compile("\""), "\\\\\"");
                put(Pattern.compile("'"), "\\\\'");
                put(Pattern.compile("/\\*\\s*@namespace\\W+?([\\w.]*)\\s\\*/"), "$1 = \"");
        }};

        public TemplateResource(DownloadableResource originalResource)
        {
            super(originalResource);
        }

        @Override
        protected String transform(String originalContent)
        {
            String value = originalContent;
            for (Map.Entry<Pattern,String> entry : PATTERNS.entrySet())
            {
                value = entry.getKey().matcher(value).replaceAll(entry.getValue());
            }
            return value + "\";";
        }
    }
}
