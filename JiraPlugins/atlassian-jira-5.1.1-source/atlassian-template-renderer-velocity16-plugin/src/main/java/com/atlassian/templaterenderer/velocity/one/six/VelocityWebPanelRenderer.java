package com.atlassian.templaterenderer.velocity.one.six;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.templaterenderer.TemplateContextFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.templaterenderer.velocity.AbstractCachingWebPanelRenderer;
import com.atlassian.templaterenderer.velocity.one.six.internal.VelocityTemplateRendererImpl;

import java.util.Collections;

/**
 * @since   2.5.0
 */
public class VelocityWebPanelRenderer extends AbstractCachingWebPanelRenderer
{
    private final TemplateContextFactory templateContextFactory;

    public VelocityWebPanelRenderer(TemplateContextFactory templateContextFactory, PluginEventManager pluginEventManager)
    {
        super(pluginEventManager);
        this.templateContextFactory = templateContextFactory;
    }

    @Override
    protected TemplateRenderer createRenderer(Plugin plugin)
    {
        return new VelocityTemplateRendererImpl(plugin.getClassLoader(), plugin.getKey(),
                Collections.<String, String>emptyMap(), templateContextFactory);
    }
}
