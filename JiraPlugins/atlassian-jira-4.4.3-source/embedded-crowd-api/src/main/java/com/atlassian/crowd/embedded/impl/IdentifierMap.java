package com.atlassian.crowd.embedded.impl;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * This class behaves like a HashMap with lower-case String keys. All key
 * arguments are lower-cased before further processing.
 *
 * @param <V> value type
 */
public class IdentifierMap<V> extends ForwardingMap<String, V>
{
    private final HashMap<String, V> delegate;

    public IdentifierMap()
    {
        delegate = Maps.newHashMap();
    }

    public IdentifierMap(int expectedSize)
    {
        delegate = Maps.newHashMapWithExpectedSize(expectedSize);
    }

    @Override
    protected Map<String, V> delegate()
    {
        return delegate;
    }

    private Object lowercase(Object key)
    {
        return key instanceof String ? IdentifierUtils.toLowerCase((String) key) : key;
    }

    @Override
    public V remove(Object key)
    {
        return delegate().remove(lowercase(key));
    }

    @Override
    public boolean containsKey(Object key)
    {
        return delegate().containsKey(lowercase(key));
    }

    @Override
    public V get(Object key)
    {
        return delegate().get(lowercase(key));
    }

    @Override
    public V put(String key, V value)
    {
        return delegate().put((String) lowercase(key), value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> map)
    {
        Map<String, V> lowercaseMap = Maps.newHashMapWithExpectedSize(map.size());
        for (Entry<? extends String, ? extends V> entry : map.entrySet())
        {
            lowercaseMap.put((String) lowercase(entry.getKey()), entry.getValue());
        }

        delegate().putAll(lowercaseMap);
    }
}
