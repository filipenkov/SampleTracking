package com.atlassian.jira.issue.customfields.manager;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.util.cache.JiraCachedManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CachedGenericConfigManager implements GenericConfigManager, JiraCachedManager, Startable
{
    private static final Object NULL_VALUE = new Object();

    // ---------------------------------------------------------------------------------------------------- Dependencies

    private final GenericConfigManager delegate;
    private final EventPublisher eventPublisher;

    private final ConcurrentMap<Key, Object> cache = new ConcurrentHashMap<Key, Object>();

    // ---------------------------------------------------------------------------------------------------- Constructors
    public CachedGenericConfigManager(final GenericConfigManager delegate, final EventPublisher eventPublisher)
    {
        this.delegate = delegate;
        this.eventPublisher = eventPublisher;
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refreshCache();
    }
    
    // -------------------------------------------------------------------------------------------------- Main methods
    public void create(final String dataType, final String key, final Object obj)
    {
        delegate.create(dataType, key, obj);

        // Remove from cache.. although it shouldn't really exist
        cache.remove(new Key(dataType, key));
    }

    public void update(final String dataType, final String key, final Object obj)
    {
        delegate.update(dataType, key, obj);

        // Remove from cache
        cache.remove(new Key(dataType, key));
    }

    public Object retrieve(final String dataType, final String key)
    {
        final Key cacheKey = new Key(dataType, key);
        {
            final Object result = cache.get(cacheKey);
            if (result != null)
            {
                if (result == NULL_VALUE)
                {
                    return null;
                }
                return result;
            }
        }
        final Object retrieved = delegate.retrieve(dataType, key);
        if (retrieved == null)
        {
            // doesn't exist, can't cache null, let the cache know the database doesn't contain the value
            cache.putIfAbsent(cacheKey, NULL_VALUE);
        }
        else
        {
            cache.putIfAbsent(cacheKey, retrieved);
        }
        return retrieved;
    }

    public void remove(final String dataType, final String key)
    {
        delegate.remove(dataType, key);

        // Remove from cache
        cache.remove(new Key(dataType, key));
    }

    public long getCacheSize()
    {
        return cache.size();
    }

    public void refreshCache()
    {
        cache.clear();
    }

    private static class Key
    {
        final String dataType;
        final String key;
        transient int hashCode;

        Key(final String dataType, final String key)
        {
            this.dataType = dataType;
            this.key = key;
        }

        @Override
        public int hashCode()
        {
            if (hashCode == 0)
            {
                final int PRIME = 31;
                int result = 1;
                result = PRIME * result + ((dataType == null) ? 0 : dataType.hashCode());
                result = PRIME * result + ((key == null) ? 0 : key.hashCode());
                hashCode = result;
            }
            return hashCode;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final Key other = (Key) obj;
            if (dataType == null)
            {
                if (other.dataType != null)
                {
                    return false;
                }
            }
            else if (!dataType.equals(other.dataType))
            {
                return false;
            }
            if (key == null)
            {
                if (other.key != null)
                {
                    return false;
                }
            }
            else if (!key.equals(other.key))
            {
                return false;
            }
            return true;
        }
    }
}
