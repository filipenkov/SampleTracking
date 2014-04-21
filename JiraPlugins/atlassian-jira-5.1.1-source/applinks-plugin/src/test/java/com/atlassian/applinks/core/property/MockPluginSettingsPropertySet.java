package com.atlassian.applinks.core.property;

import com.atlassian.applinks.api.PropertySet;
import com.atlassian.sal.api.pluginsettings.PluginSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.fail;

/**
 * Simple non-persistent implementation for tests
 */
public class MockPluginSettingsPropertySet implements PluginSettings, PropertySet
{
    private final Map<String, Object> map = new HashMap<String, Object>();

    public Object get(final String key)
    {
        return map.get(key);
    }

    public Object put(final String key, final Object value)
    {
        return map.put(key, checkType(value));
    }

    public Object remove(final String key)
    {
        return map.remove(key);
    }

    public Object getProperty(final String key)
    {
        return get(key);
    }

    public Object putProperty(final String key, final Object value)
    {
        return put(key, checkType(value));
    }

    public Object removeProperty(final String key)
    {
        return remove(key);
    }

    private <T> T checkType(final T obj)
    {
        if (!(obj instanceof String || obj instanceof List))
        {
            if (obj instanceof Map)
            {
                for (final Object o : ((Map) obj).entrySet())
                {
                    final Map.Entry e = (Map.Entry) o;
                    if (!(e.getKey() instanceof String))
                    {
                        fail("Tried to store " + obj.getClass().getSimpleName() + " that contains a key of type " + e.getKey().getClass());
                    }
                    if (!(e.getValue() instanceof String))
                    {
                        fail("Tried to store " + obj.getClass().getSimpleName() + " that contains a value of type " + e.getValue().getClass());
                    }
                }
            }
            else
            {
                fail("Tried to store object of type " + obj.getClass().getName() + " in PropertySet");
            }
        }
        return obj;
    }
}
