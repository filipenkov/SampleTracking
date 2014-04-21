package com.atlassian.templaterenderer.velocity;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.web.renderer.WebPanelRenderer;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.util.concurrent.CopyOnWriteMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractCachingWebPanelRenderer implements WebPanelRenderer, InitializingBean
{
    private static final Logger log = LoggerFactory.getLogger(AbstractCachingWebPanelRenderer.class);

    private final ConcurrentMap<String, TemplateRenderer> rendererCache
            = CopyOnWriteMap.<String, TemplateRenderer>builder().newHashMap();
    private final PluginEventManager pluginEventManager;

    protected AbstractCachingWebPanelRenderer(PluginEventManager pluginEventManager)
    {
        this.pluginEventManager = pluginEventManager;
    }

    public String getResourceType()
    {
        return "velocity";
    }

    private TemplateRenderer getRenderer(Plugin plugin)
    {
        TemplateRenderer templateRenderer = rendererCache.get(plugin.getKey());

        if (templateRenderer == null)
        {
            templateRenderer = createRenderer(plugin);
            TemplateRenderer cachedRenderer = rendererCache.putIfAbsent(plugin.getKey(), templateRenderer);
            templateRenderer = cachedRenderer == null ? templateRenderer : cachedRenderer;
        }
        return templateRenderer;
    }

    public void render(String s, Plugin plugin, Map<String, Object> stringObjectMap, Writer writer) throws IOException
    {
        getRenderer(plugin).render(s, stringObjectMap, writer);
    }

    public String renderFragment(String fragment, Plugin plugin, Map<String, Object> stringObjectMap)
    {
        return getRenderer(plugin).renderFragment(fragment, stringObjectMap);
    }

    public void renderFragment(Writer writer, String fragment, Plugin plugin, Map<String, Object> stringObjectMap) throws IOException
    {
        writer.write(getRenderer(plugin).renderFragment(fragment, stringObjectMap));
    }

    /**
     * @param plugin the {@link com.atlassian.plugin.Plugin} to be used as context for the created renderer. It provides the ClassLoader for
     * resolving templates and other resources and a plugin key to be used to resolve template context items from the
     * {@link com.atlassian.templaterenderer.TemplateContextFactory}.
     * @return a {@link com.atlassian.templaterenderer.TemplateRenderer} for the supplied {@link com.atlassian.plugin.Plugin}. This <em>may</em> be cached by the underlying
     * implementation.
     */
    protected abstract TemplateRenderer createRenderer(Plugin plugin);

    public void destroy()
    {
        log.debug("destroy()");
        pluginEventManager.unregister(this);
    }

    public void afterPropertiesSet()
    {
        log.debug("afterPropertiesSet()");
        pluginEventManager.register(this);
    }

    @PluginEventListener
    public void pluginUnloaded(PluginDisabledEvent disabledEvent)
    {
        rendererCache.remove(disabledEvent.getPlugin().getKey());
    }
}
