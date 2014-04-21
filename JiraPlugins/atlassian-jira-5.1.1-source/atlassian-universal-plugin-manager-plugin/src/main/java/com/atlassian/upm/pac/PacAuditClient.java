package com.atlassian.upm.pac;

/**
 * Communicates with the PAC server for requests that are only for usage data collection.
 * This is separate from PacClient in order to avoid a circular dependency between PacClientImpl
 * and PluginAccessorAndControllerImpl.
 */
public interface PacAuditClient
{
    /**
     * Tells the PAC server that we have disabled a plugin.
     */
    void logPluginDisabled(String key, String version);
    
    /**
     * Tells the PAC server that we have uninstalled a plugin.
     */
    void logPluginUninstalled(String key, String version);
}
