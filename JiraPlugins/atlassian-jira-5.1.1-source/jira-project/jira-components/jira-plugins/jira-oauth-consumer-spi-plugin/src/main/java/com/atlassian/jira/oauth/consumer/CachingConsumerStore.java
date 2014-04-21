package com.atlassian.jira.oauth.consumer;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.core.ConsumerServiceStore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Provides a caching consumer store implementation.  Note that in general on the consumer side there's really only one
 * consumer (the instance 'itself').  But there's the ability for administrators to add additional consumer service
 * information because sites like Netflix and TripIt assign developers the consumer key and consumer secret that they
 * should use when communicating with the site. So to make it possible to write gadgets for these services, we need a
 * way to add that consumer information and have a gadget pick that consumer information to use.
 *
 * @since v4.0
 */
public class CachingConsumerStore implements ConsumerServiceStore
{
    private final ConsumerServiceStore delegateStore;

    private final ConsumerCache cache = new ConsumerCache();
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();

    public CachingConsumerStore(final ConsumerServiceStore delegateStore)
    {
        notNull("delegateStore", delegateStore);

        this.delegateStore = delegateStore;
    }

    public ConsumerAndSecret get(final String service)
    {
        notNull("service", service);

        cacheLock.readLock().lock();
        try
        {
            ConsumerAndSecret cas = cache.getByService(service);
            if (cas == null)
            {
                cas = delegateStore.get(service);
                if (cas != null)
                {
                    cache.put(cas);
                }
            }
            return cas;
        }
        finally
        {
            cacheLock.readLock().unlock();
        }
    }

    public ConsumerAndSecret getByKey(final String key)
    {
        notNull("key", key);

        cacheLock.readLock().lock();
        try
        {
            final String service = cache.getServiceForKey(key);
            //looks like there's no cache entry...fetch it from the delegate store
            if (service == null)
            {
                final ConsumerAndSecret cas = delegateStore.getByKey(key);
                if(cas != null)
                {
                    cache.put(cas);
                }
                return cas;
            }
            //get the cached consumer via its service name.
            return get(service);
        }
        finally
        {
            cacheLock.readLock().unlock();
        }
    }

    public void put(final String service, final ConsumerAndSecret cas)
    {
        notNull("service", service);
        notNull("cas", cas);
        notNull("cas.consumer", cas.getConsumer());

        cacheLock.writeLock().lock();
        try
        {
            delegateStore.put(service, cas);
            //ensure we clear the entry for this consumer so that a get() will retrieve the current
            //value again from the database.
            cache.removeByService(cas);
        }
        finally
        {
            cacheLock.writeLock().unlock();
        }

    }

    public void removeByKey(final String key)
    {
        notNull("key", key);

        cacheLock.writeLock().lock();
        try
        {
            delegateStore.removeByKey(key);
            cache.removeByKey(key);
        }
        finally
        {
            cacheLock.writeLock().unlock();
        }
    }

    public Iterable<Consumer> getAllServiceProviders()
    {
        //non-caching call.  There shouldn't be many of these anyway.
        return delegateStore.getAllServiceProviders();
    }

    private static class ConsumerCache
    {
        //no need here for an LRU map since this consumerCache shouldn't really grow very large.
        private final Map<String, ConsumerAndSecret> consumerCache = new ConcurrentHashMap<String, ConsumerAndSecret>();
        //maintains a key -> service mapping.
        private final Map<String, String> cacheByKey = new ConcurrentHashMap<String, String>();


        public ConsumerAndSecret getByService(final String service)
        {
            return consumerCache.get(service);
        }

        public String getServiceForKey(final String key)
        {
            return cacheByKey.get(key);
        }

        public void put(final ConsumerAndSecret cas)
        {
            consumerCache.put(cas.getServiceName(), cas);
            cacheByKey.put(cas.getConsumer().getKey(), cas.getServiceName());
        }

        public void removeByService(final ConsumerAndSecret cas)
        {
            consumerCache.remove(cas.getServiceName());
            cacheByKey.remove(cas.getConsumer().getKey());
        }

        public void removeByKey(final String key)
        {
            final String service = cacheByKey.remove(key);
            if (service != null)
            {
                consumerCache.remove(service);
            }
        }
    }

}
