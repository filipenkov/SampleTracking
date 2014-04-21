package com.atlassian.upm.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.atlassian.sal.api.pluginsettings.PluginSettings;

/**
 * A map-based implementation of {@code PluginSettings} used for testing purposes. You probably want to use it like this:
 * <p/>
 * Map settingsMap = new HashMap();
 * when(pluginSettingsFactory.createGlobalSettings()).thenReturn(new MapBackedPluginSettings(settingsMap));
 * <p/>
 * <p/>
 * Then query settingsMap in your tests
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class MapBackedPluginSettings implements PluginSettings
{
    private final Map map;

    public MapBackedPluginSettings()
    {
        this.map = new HashMap();
    }

    public MapBackedPluginSettings(Map map)
    {
        this.map = map;
    }

    public Object get(String key)
    {
        return map.get(key);
    }

    public Object put(String key, Object value)
    {
        if (!checkValueType(value))
        {
            throw new IllegalArgumentException("Object " + value + " of type " + value.getClass() + " + cannot be stored in PluginSettings");
        }
        return map.put(key, value);
    }

    public Object remove(String key)
    {
        return map.remove(key);
    }

    private boolean checkValueType(Object value)
    {
        if (value instanceof String || value instanceof Properties)
        {
            return true;
        }
        else if (value instanceof List)
        {
            for (Object o : ((List) value))
            {
                if (!(o instanceof String))
                {
                    return false;
                }
            }
        }
        else if (value instanceof Map)
        {
            for (Map.Entry entry : ((Map<?, ?>) value).entrySet())
            {
                if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof String))
                {
                    return false;
                }
            }
        }
        return true;
    }
}
