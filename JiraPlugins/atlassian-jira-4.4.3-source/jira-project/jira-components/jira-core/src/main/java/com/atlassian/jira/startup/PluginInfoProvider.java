package com.atlassian.jira.startup;

import com.atlassian.plugin.PluginInformation;

import java.util.Collection;

/**
 * This provides information about the plugins in the system.  Its intended to be used by the system info and startup
 * pages to allow fr better support capabilties
 *
 * @since v4.3
 */
public interface PluginInfoProvider
{
    /**
     * @return Information about the current system plugins in the system
     */
    Collection<Info> getSystemPlugins();

    /**
     * @return Information about the current user plugins in the system
     */
    Collection<Info> getUserPlugins();

    /**
     * This will filter out a series of plugins that are so integral to JIRA thats its hardly worth knowing about them
     *
     * @param pluginInfos the list of PluginInfo objects
     * @return a filtered list removing built in JIRA plugins
     */
    Collection<Info> filterOutTrulyBuiltInPlugins(Collection<Info> pluginInfos);

    /**
     * A value object of collected plugin information
     */
    interface Info
    {

        /**
         * @return the plugin key
         */
        String getKey();

        /**
         * @return the plugin name
         */
        String getName();

        /**
         * @return the plugin information
         */
        PluginInformation getPluginInformation();

        /**
         * @return true if the plugin is unloadable
         */
        boolean isUnloadable();

        /**
         * @return the reason for unloadability
         */
        String getUnloadableReason();

        /**
         * @return true if the plugin is currently enabled
         */
        boolean isEnabled();

        /**
         * @return true if the plugin is a system plugin
         */
        boolean isSystemPlugin();

        /**
         * @return the platform the plugin targets, eg plugins 1 or plugins 2
         */
        int getPluginsVersion();
    }


}
