package com.atlassian.upm;

import com.atlassian.upm.spi.Plugin;

import com.google.common.base.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities for {@link Plugin}s.
 */
public class Plugins
{
    public static Function<String, Plugin> fromPluginKey(PluginAccessorAndController pluginAccessorAndController)
    {
        return new FromPluginKey(pluginAccessorAndController);
    }

    static class FromPluginKey implements Function<String, Plugin>
    {
        private final PluginAccessorAndController pluginAccessorAndController;

        FromPluginKey(PluginAccessorAndController pluginAccessorAndController)
        {
            this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
        }

        @Override
        public Plugin apply(String pluginKey)
        {
            return pluginAccessorAndController.getPlugin(pluginKey);
        }
    };
}
