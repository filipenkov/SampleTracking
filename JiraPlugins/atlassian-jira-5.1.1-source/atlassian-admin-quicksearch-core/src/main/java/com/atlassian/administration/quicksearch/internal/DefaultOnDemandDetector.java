package com.atlassian.administration.quicksearch.internal;

import com.atlassian.plugin.PluginAccessor;

/**
 * Default implementation of {@link com.atlassian.administration.quicksearch.internal.OnDemandDetector}.
 *
 * @since 1.0
 */
public class DefaultOnDemandDetector extends AbstractOnDemandDetector
{
    private static final String ON_DEMAND_COMPONENTS_PLUGIN_KEY = "com.atlassian.studio.static";

    private final PluginAccessor pluginAccessor;

    public DefaultOnDemandDetector(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public boolean isOnDemandMode()
    {
        return pluginAccessor.getPlugin(ON_DEMAND_COMPONENTS_PLUGIN_KEY) != null;
    }

}
