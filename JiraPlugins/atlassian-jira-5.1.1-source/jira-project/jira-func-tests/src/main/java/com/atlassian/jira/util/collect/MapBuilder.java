package com.atlassian.jira.util.collect;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapBuilder<K, V>
{
    public static <K, V> MapBuilder<K, V> newBuilder()
    {
        return new MapBuilder<K, V>();
    }

    private final Map<K, V> map = new LinkedHashMap<K, V>();

    public MapBuilder<K, V> add(final K key, final V value)
    {
        map.put(key, value);
        return this;
    }

    public MapBuilder<K, V> addIfValueNotNull(final K key, final V value)
    {
        if (value != null)
        {
            map.put(key, value);
        }
        return this;
    }

    public Map<K, V> toImmutableMap()
    {
        return Collections.unmodifiableMap(new HashMap<K, V>(map));
    }

    public Map<K, V> toMutableMap()
    {
        return new HashMap<K, V>(map);
    }

    public Map<K, V> toMap()
    {
        return toImmutableMap();
    }

    public MapBuilder<K, V> addAll(Map<K, V> mapToAdd)
    {
        for (Map.Entry<K, V> entry : mapToAdd.entrySet())
        {
            map.put(entry.getKey(), entry.getValue());
        }
        return this;
    }
}
