package com.atlassian.upm;

/**
 * Thrown if there is an exception updating a plugins enabled/disabled status.
 */
public final class PluginStateUpdateException extends RuntimeException
{
    private final PluginConfiguration pluginConfiguration;

    public PluginStateUpdateException(PluginConfiguration pluginConfiguration)
    {
        this.pluginConfiguration = pluginConfiguration;
    }

    public PluginConfiguration getPlugin()
    {
        return pluginConfiguration;
    }
}