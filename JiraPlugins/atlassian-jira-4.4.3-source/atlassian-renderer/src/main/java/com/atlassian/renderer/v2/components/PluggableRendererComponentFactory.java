package com.atlassian.renderer.v2.components;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;

/**
 * A factory used to instantiate RendererComponents via the plugin system.
 *
 * @since v3.12
 */
public interface PluggableRendererComponentFactory
{
    /**
     * Initialises the RendererComponentFactory, with parameters provided by a plugin module descriptor.
     * @param moduleDescriptor plugin module descriptor to initialize the factory
     * @throws com.atlassian.plugin.PluginParseException if there's an error initializing the factory
     */
    void init(ModuleDescriptor moduleDescriptor) throws PluginParseException;

    /**
     * Constructs a {@link com.atlassian.renderer.v2.components.RendererComponent}.
     *
     * @return a new RendererComponent or null if there was a problem initialising the component */
    RendererComponent getRendererComponent();
}
