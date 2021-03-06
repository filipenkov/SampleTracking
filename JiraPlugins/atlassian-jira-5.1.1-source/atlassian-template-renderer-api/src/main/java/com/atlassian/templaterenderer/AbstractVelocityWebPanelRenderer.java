package com.atlassian.templaterenderer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.web.renderer.WebPanelRenderer;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @deprecated It is broken as it does not listen for when plugins are reloaded. This leads stale references to old
 * renderers being used for reloaded plugins. The non-api class AbstractCachingWebPanelRenderer provides a correct
 * implementation based on this class.
 */
@Deprecated
public abstract class AbstractVelocityWebPanelRenderer implements WebPanelRenderer
{
    private final Map<String, TemplateRenderer> rendererCache = createCacheMap();

    public String getResourceType()
    {
        return "velocity";
    }

    private TemplateRenderer getRenderer(Plugin plugin)
    {
        TemplateRenderer templateRenderer = rendererCache.get(plugin.getKey());
        if (templateRenderer == null) {
            templateRenderer = createRenderer(plugin);
            rendererCache.put(plugin.getKey(), templateRenderer);
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
     * @param plugin the {@link Plugin} to be used as context for the created renderer. It provides the ClassLoader for
     * resolving templates and other resources and a plugin key to be used to resolve template context items from the
     * {@link TemplateContextFactory}.
     * @return a {@link TemplateRenderer} for the supplied {@link Plugin}. This <em>may</em> be cached by the underlying
     * implementation.
     */
    protected abstract TemplateRenderer createRenderer(Plugin plugin);

    //may be overridden in unit tests
    protected Map<String, TemplateRenderer> createCacheMap()
    {
        return Collections.synchronizedMap(new WeakHashMap<String, TemplateRenderer>());
    }
}
