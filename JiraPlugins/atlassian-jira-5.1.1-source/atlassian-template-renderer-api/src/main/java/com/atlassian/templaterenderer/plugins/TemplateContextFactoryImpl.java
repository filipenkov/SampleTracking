package com.atlassian.templaterenderer.plugins;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.templaterenderer.TemplateContextFactory;

import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

/**
 * Implementation of the template context factory
 */
public class TemplateContextFactoryImpl implements TemplateContextFactory, DisposableBean
{
    private static final Logger log = Logger.getLogger(TemplateContextFactoryImpl.class);
    private final PluginModuleTracker<Object, TemplateContextItemModuleDescriptor> templateContextItemTracker;

    public TemplateContextFactoryImpl(PluginAccessor pluginAccessor, PluginEventManager eventManager)
    {
        this.templateContextItemTracker = new DefaultPluginModuleTracker<Object, TemplateContextItemModuleDescriptor>(
                pluginAccessor, eventManager, TemplateContextItemModuleDescriptor.class);
    }

    /**
     * Create a context for a template renderer
     *
     * @param contextParams Any extra context parameters that should be added to the context
     * @return A map of the context
     */
    public Map<String, Object> createContext(String pluginKey, Map<String, Object> contextParams)
    {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("context", context);

        for (TemplateContextItemModuleDescriptor desc : templateContextItemTracker.getModuleDescriptors())
        {
            if (desc.isGlobal() || desc.getPluginKey().equals(pluginKey))
            {
                try
                {
                    context.put(desc.getContextKey(), desc.getModule());
                }
                catch (RuntimeException re)
                {
                    log.error("Error loading module for " + desc.getPluginKey() + ":" + desc.getKey(), re);
                }
            }
        }
        context.putAll(contextParams);
        return context;
    }

    public void destroy()
    {
        templateContextItemTracker.close();
    }
}
