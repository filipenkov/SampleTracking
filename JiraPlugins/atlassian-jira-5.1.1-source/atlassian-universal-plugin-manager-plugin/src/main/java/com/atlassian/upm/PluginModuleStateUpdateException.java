package com.atlassian.upm;

/**
 * Thrown if there is an exception updating a plugins enabled/disabled status.
 */
public final class PluginModuleStateUpdateException extends RuntimeException
{
    private final PluginConfiguration pluginConfiguration;
    private final PluginModuleConfiguration pluginModuleConfiguration;

    public PluginModuleStateUpdateException(PluginConfiguration pluginConfiguration, PluginModuleConfiguration pluginModuleConfiguration)
    {
        this.pluginConfiguration = pluginConfiguration;
        this.pluginModuleConfiguration = pluginModuleConfiguration;
    }

    public PluginConfiguration getPlugin()
    {
        return pluginConfiguration;
    }

    public PluginModuleConfiguration getPluginModule()
    {
        return pluginModuleConfiguration;
    }
}
