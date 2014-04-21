package com.atlassian.applinks.core.property;

import com.atlassian.applinks.api.PropertySet;
import com.atlassian.sal.api.pluginsettings.PluginSettings;

/**
 * Default {@link PropertySet} implementation that delegates to SAL
 */
public class SalPropertySet implements PropertySet
{

    private final PluginSettings pluginSettings;
    private final String keyPrefix;

    public SalPropertySet(PluginSettings pluginSettings, String keyPrefix)
    {
        this.pluginSettings = pluginSettings;
        this.keyPrefix = keyPrefix;
    }

    public Object getProperty(String s)
    {
        return pluginSettings.get(namespace(s));
    }

    public Object putProperty(String s, Object o)
    {
        return pluginSettings.put(namespace(s), o);
    }

    public Object removeProperty(String s)
    {
        return pluginSettings.remove(namespace(s));
    }

    private String namespace(String key)
    {
        return keyPrefix + "." + key;
    }

}
