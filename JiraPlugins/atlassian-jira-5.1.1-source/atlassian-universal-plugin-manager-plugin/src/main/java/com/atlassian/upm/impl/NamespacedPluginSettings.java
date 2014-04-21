package com.atlassian.upm.impl;

import com.atlassian.sal.api.pluginsettings.PluginSettings;

import static com.atlassian.upm.license.internal.impl.LongKeyHasher.hashKeyIfTooLong;

/**
 * Simple decorator that prepends a key namespace to a global {@code PluginSettings}.
 */
public class NamespacedPluginSettings implements PluginSettings
{
    private PluginSettings pluginSettings;
    private String keyPrefix;

    /**
     * Constructor.
     *
     * @param pluginSettings the {@code PluginSettings} implementation to use
     * @param keyPrefix the namespace prefix to use
     */
    public NamespacedPluginSettings(PluginSettings pluginSettings, String keyPrefix)
    {
        this.pluginSettings = pluginSettings;
        this.keyPrefix = keyPrefix;
    }

    public Object get(String s)
    {
        return pluginSettings.get(hashKeyIfTooLong(keyPrefix + s));
    }

    public Object put(String s, Object o)
    {
        return pluginSettings.put(hashKeyIfTooLong(keyPrefix + s), o);
    }

    public Object remove(String s)
    {
        return pluginSettings.remove(hashKeyIfTooLong(keyPrefix + s));
    }
}
