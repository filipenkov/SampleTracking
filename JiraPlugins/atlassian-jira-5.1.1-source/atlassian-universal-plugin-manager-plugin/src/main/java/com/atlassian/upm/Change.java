package com.atlassian.upm;

import com.atlassian.plugin.PluginRestartState;
import com.atlassian.upm.permission.Permission;
import com.atlassian.upm.spi.Plugin;

import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_INSTALL;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_UNINSTALL;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A change that requires a restart of the plugins system. Contains a name, the action
 * that requires restart, and a permission required to view this change
 */
public class Change
{
    private final Plugin plugin;
    private final String action;
    private final Permission requiredPermission;

    public Change(Plugin plugin, PluginRestartState pluginRestartState)
    {
        this.plugin = checkNotNull(plugin, "plugin");
        this.action = checkNotNull(pluginRestartState, "pluginRestartState").toString().toLowerCase();
        this.requiredPermission = getRequiredPermission(pluginRestartState);
    }

    private static Permission getRequiredPermission(PluginRestartState pluginRestartState)
    {
        switch (pluginRestartState)
        {
            case UPGRADE:
            case INSTALL:
                // Note that most of the time we can't tell the difference between install
                // and update, so we don't want to split the two cases here even though
                // we actually do know the difference at this point
                return MANAGE_PLUGIN_INSTALL;
            case REMOVE:
                return MANAGE_PLUGIN_UNINSTALL;
            case NONE:
                throw new IllegalArgumentException("No restart state");
            default:
                throw new IllegalArgumentException("Unknown restart state");
        }
    }

    public Plugin getPlugin()
    {
        return plugin;
    }

    public String getAction()
    {
        return action;
    }

    public Permission getRequiredPermission()
    {
        return requiredPermission;
    }
}
