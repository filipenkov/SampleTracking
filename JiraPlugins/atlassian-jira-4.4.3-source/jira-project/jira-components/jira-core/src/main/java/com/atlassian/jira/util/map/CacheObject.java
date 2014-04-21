package com.atlassian.jira.util.map;

/**
 * Used for keys or values in Maps that do not support null keys, or in caches where it is needed to
 * differentiate between a cached 'null' value and an object not being in the cache.
 *
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class CacheObject<T>
{
    private final T value;

    /**
     * @deprecated Create your own null instance for type safety. Deprecated since v4.0
     */
    @Deprecated
    public static final CacheObject NULL_INSTANCE = new CacheObject(null);

    public CacheObject(final T value)
    {
        this.value = value;
    }

    public T getValue()
    {
        return value;
    }

    // THis object can also be used as a key for maps that do not support null keys, for example ConcurrentHashMap
    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof CacheObject))
        {
            return false;
        }

        final CacheObject<?> cacheObject = (CacheObject<?>) o;

        if (value != null ? !value.equals(cacheObject.value) : cacheObject.value != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return (value != null ? value.hashCode() : 0);
    }

}
