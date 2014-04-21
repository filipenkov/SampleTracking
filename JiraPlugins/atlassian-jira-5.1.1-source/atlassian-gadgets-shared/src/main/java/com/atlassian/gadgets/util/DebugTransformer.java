package com.atlassian.gadgets.util;

import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.util.PluginUtils;
import com.atlassian.plugin.webresource.transformer.AbstractStringTransformedDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import org.dom4j.Element;

/**
 *
 */
public class DebugTransformer implements WebResourceTransformer
{
    public DownloadableResource transform(Element configElement, ResourceLocation location, String filePath, DownloadableResource nextResource)
    {
        return new DebugResource(nextResource);
    }

    private static class DebugResource extends AbstractStringTransformedDownloadableResource
    {
        public DebugResource(DownloadableResource originalResource)
        {
            super(originalResource);
        }

        @Override
        protected String transform(String originalContent)
        {
            if (isDevMode())
            {
                return originalContent;
            }
            else
            {
                return originalContent.replace("AJS.debug=true;", "AJS.debug=false;");
            }
        }

        private boolean isDevMode()
        {
            return Boolean.getBoolean(PluginUtils.ATLASSIAN_DEV_MODE);
        }


    }
}
