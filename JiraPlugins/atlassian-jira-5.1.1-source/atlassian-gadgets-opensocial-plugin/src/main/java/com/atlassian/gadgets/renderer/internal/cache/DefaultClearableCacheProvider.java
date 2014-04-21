package com.atlassian.gadgets.renderer.internal.cache;

import com.google.common.collect.MapMaker;
import org.apache.shindig.common.cache.Cache;
import org.apache.shindig.common.cache.CacheProvider;
import org.apache.shindig.common.cache.LruCacheProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * This CacheProvider allows AG to clear shindig's cache to ensure that if applinks are added/removed
 * shindig doesn't return gagdet specs that are no longer valid.
 *
 * @since v3.0
 */
@SuppressWarnings("unchecked")
public class DefaultClearableCacheProvider implements ClearableCacheProvider
{
    private CacheProvider delegateProvider;
    private final ConcurrentMap<String, ClearableCacheWrapper> caches = new MapMaker().makeMap();
    private ClearableCacheWrapper anonymousCache;

    public DefaultClearableCacheProvider()
    {
        delegateProvider = new LruCacheProvider(1000);
        anonymousCache = new ClearableCacheWrapper(delegateProvider.createCache(null));
    }

    public <K, V> Cache<K, V> createCache(final String name)
    {
        if(name == null)
        {
            return anonymousCache;
        }

        ClearableCacheWrapper value = caches.get(name);
        if(value == null)
        {
            final ClearableCacheWrapper<K, V> cache = new ClearableCacheWrapper<K,V>(delegateProvider.<K,V>createCache(name));
            value = caches.putIfAbsent(name, cache);
            if(value == null) {
                value = cache;
            }
        }
        return value;
    }

    public void clear()
    {
        delegateProvider = new LruCacheProvider(1000);
        for (Map.Entry<String, ClearableCacheWrapper> cacheEntry : caches.entrySet())
        {
            final String cacheName = cacheEntry.getKey();
            final ClearableCacheWrapper cache = cacheEntry.getValue();
            cache.clear(delegateProvider.createCache(cacheName));
        }

        anonymousCache.clear(delegateProvider.createCache(null));
    }

    static class ClearableCacheWrapper<K,V> implements Cache<K, V> {

        private Cache<K, V> delegate;

        ClearableCacheWrapper(Cache<K, V> delegate)
        {
            this.delegate = delegate;
        }

        public V getElement(final K key)
        {
            return delegate.getElement(key);
        }

        public void addElement(final K key, final V value)
        {
            delegate.addElement(key, value);
        }

        public V removeElement(final K key)
        {
            return delegate.removeElement(key);
        }

        public long getCapacity()
        {
            return delegate.getCapacity();
        }

        public long getSize()
        {
            return delegate.getSize();
        }

        public void clear(Cache<K,V> delegate)
        {
            this.delegate = delegate;
        }
    }
}


