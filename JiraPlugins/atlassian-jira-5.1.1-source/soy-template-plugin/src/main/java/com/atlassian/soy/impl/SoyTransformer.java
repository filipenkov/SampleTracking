package com.atlassian.soy.impl;

import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.CharSequenceDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.jssrc.SoyJsSrcOptions;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v1.0
 */
public class SoyTransformer implements WebResourceTransformer
{
    private final SoyManager soyManager;

    public SoyTransformer(SoyManager soyManager)
    {
        this.soyManager = soyManager;
    }

    @Override
    public DownloadableResource transform(Element element, ResourceLocation location, String filePath, DownloadableResource nextResource)
    {
        return new SoyResource(getFunctionModules(element), nextResource, location.getLocation());
    }

    private String[] getFunctionModules(Element element)
    {
        List<String> functionModules = new ArrayList<String>();

        for (Object functionElement : element.elements("functions"))
            functionModules.add(((Element)functionElement).getTextTrim());

        return functionModules.toArray(new String[functionModules.size()]);
    }

    private class SoyResource extends CharSequenceDownloadableResource
    {
        private final String[] pluginKeys;
        private final String location;
        private final SoyJsSrcOptions jsSrcOptions;

        private SoyResource(String[] pluginKeys, DownloadableResource originalResource, String location)
        {
            super(originalResource);
            this.pluginKeys = pluginKeys;
            this.location = location;
            this.jsSrcOptions = new SoyJsSrcOptions();
            this.jsSrcOptions.setShouldGenerateJsdoc(false);
        }

        @Override
        public String getContentType()
        {
            return "text/javascript";
        }

        @Override
        protected CharSequence transform(CharSequence originalContent)
        {
            SoyFileSet.Builder sfsBuilder = soyManager.makeBuilder(pluginKeys);
            sfsBuilder.add(originalContent, location);

            SoyFileSet sfs = sfsBuilder.build();

            final List<String> output = sfs.compileToJsSrc(jsSrcOptions, null);
            if (output.size() != 1)
            {
                throw new IllegalStateException("Did not manage to compile soy template at:" + location + ", size=" + output.size());
            }
            return output.get(0);
        }
    }
}
