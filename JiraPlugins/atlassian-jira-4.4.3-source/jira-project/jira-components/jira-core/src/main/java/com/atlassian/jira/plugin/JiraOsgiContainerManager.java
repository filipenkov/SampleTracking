package com.atlassian.jira.plugin;

import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.osgi.container.PackageScannerConfiguration;
import com.atlassian.plugin.osgi.container.felix.FelixOsgiContainerManager;
import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;

/**
 * Need as Pico can't resolve a File required for super constructor.
 */
public class JiraOsgiContainerManager extends FelixOsgiContainerManager
{
    public JiraOsgiContainerManager(final PluginPath pluginPath, final PackageScannerConfiguration packageScannerConfig, final HostComponentProvider provider, final PluginEventManager eventManager)
    {
        super(pluginPath.getOsgiPersistentCache(), packageScannerConfig, provider, eventManager);
    }
}
