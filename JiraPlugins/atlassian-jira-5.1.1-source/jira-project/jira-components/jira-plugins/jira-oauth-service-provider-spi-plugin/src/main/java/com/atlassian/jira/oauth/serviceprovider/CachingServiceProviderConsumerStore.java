package com.atlassian.jira.oauth.serviceprovider;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.serviceprovider.StoreException;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.ManagedLock;
import com.atlassian.util.concurrent.ManagedLocks;
import com.atlassian.util.concurrent.Supplier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caching Consumer Store, responsible for providing Consumers added by administrators in the Admin section. This list
 * will most likely not be very large since consumers are manually added by administrators.
 * <p/>
 * Need to provide a cache however since lookups of a single consumer have to be fast, since everytime and OAUTH request
 * comes in, we need to check if the consumer for this request has been added by an admin.
 *
 * @since v4.0
 */
public class CachingServiceProviderConsumerStore implements ServiceProviderConsumerStore
{
    //no need here for an LRU map since this cache shouldn't really grow very large.
    private final Map<String, Consumer> cache = new ConcurrentHashMap<String, Consumer>();

    private final Function<String, ManagedLock.ReadWrite> lockFactory = ManagedLocks.weakReadWriteManagedLockFactory();
    private ServiceProviderConsumerStore delegateStore;

    public CachingServiceProviderConsumerStore(final ServiceProviderConsumerStore delegateStore)
    {
        notNull("delegateStore", delegateStore);

        this.delegateStore = delegateStore;
    }

    public void put(final Consumer consumer) throws StoreException
    {
        notNull("consumer", consumer);
        notNull("consumer.key", consumer.getKey());
        notNull("consumer.name", consumer.getName());
        notNull("consumer.publicKey", consumer.getPublicKey());

        lockFactory.get(consumer.getKey()).write().withLock(new Runnable()
        {
            public void run()
            {
                delegateStore.put(consumer);
                //ensure we clear the entry for this consumer so that a get() will retrieve the current
                //value again from the database.
                cache.remove(consumer.getKey());
            }
        });
    }

    public Consumer get(final String key) throws StoreException
    {
        notNull("key", key);

        return lockFactory.get(key).read().withLock(new Supplier<Consumer>()
        {
            public Consumer get()
            {
                Consumer consumer = cache.get(key);
                if (consumer == null)
                {
                    consumer = delegateStore.get(key);
                    if (consumer != null)
                    {
                        cache.put(key, consumer);
                    }
                }
                return consumer;
            }
        });
    }

    public Iterable<Consumer> getAll() throws StoreException
    {
        //this will only be used in the admin section when displaying all consumers, so not caching this
        //call.
        return delegateStore.getAll();
    }

    public void remove(final String key) throws StoreException
    {
        notNull("key", key);

        lockFactory.get(key).write().withLock(new Runnable()
        {
            public void run()
            {
                delegateStore.remove(key);
                cache.remove(key);
            }
        });

    }

}
