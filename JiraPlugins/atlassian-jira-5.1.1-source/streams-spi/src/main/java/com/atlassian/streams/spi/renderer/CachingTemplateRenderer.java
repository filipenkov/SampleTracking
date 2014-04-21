package com.atlassian.streams.spi.renderer;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.atlassian.templaterenderer.RenderingException;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.templaterenderer.velocity.one.six.VelocityTemplateRendererFactory;

import com.google.common.collect.ImmutableMap;

/**
 * Implemented as a temporary stop-gap until a version of template renderer with ATR-34 is available in all the
 * products.
 */
public class CachingTemplateRenderer implements TemplateRenderer
{
    private final TemplateRenderer renderer;

    public CachingTemplateRenderer(VelocityTemplateRendererFactory factory)
    {
        renderer = factory.getInstance(
                ImmutableMap.of("classpath.resource.loader.cache", Boolean.toString(!Boolean.getBoolean("atlassian.dev.mode"))));
    }

    public void render(String templateName, Map<String, Object> context, Writer writer) throws RenderingException,
            IOException
    {
        renderer.render(templateName, context, writer);
    }

    public void render(String templateName, Writer writer) throws RenderingException, IOException
    {
        renderer.render(templateName, writer);
    }

    public String renderFragment(String fragment, Map<String, Object> context) throws RenderingException
    {
        return renderer.renderFragment(fragment, context);
    }

    public boolean resolve(String templateName)
    {
        return renderer.resolve(templateName);
    }
}
