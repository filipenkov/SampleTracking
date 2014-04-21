package com.atlassian.jira.oauth.consumer;

import static com.atlassian.jira.util.collect.LRUMap.synchronizedLRUMap;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import com.atlassian.oauth.consumer.ConsumerToken;
import com.atlassian.oauth.consumer.ConsumerTokenStore;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.ManagedLock;
import com.atlassian.util.concurrent.ManagedLocks;
import com.atlassian.util.concurrent.Supplier;

import java.util.Map;

/**
 * Caching implementation of the Consumer Token Store.  This cache uses an LRU map limited to 4000 entries.  Access is
 * synchronized via a RWLock to guarantee that the cache will be consistent with what's in the database.
 * <p/>
 * Writes will clear the cache value, to be lazy loaded on the next get.
 *
 * @since v4.0
 */
public class CachingConsumerTokenStore implements ConsumerTokenStore
{
    private final Map<Key, ConsumerToken> cache = synchronizedLRUMap(4000);

    private final Function<Key, ManagedLock.ReadWrite> lockFactory = ManagedLocks.weakReadWriteManagedLockFactory();
    private ConsumerTokenStore delegateStore;

    public CachingConsumerTokenStore(final ConsumerTokenStore delegateStore)
    {
        notNull("delegateStore", delegateStore);

        this.delegateStore = delegateStore;
    }

    public ConsumerToken get(final Key key)
    {
        notNull("key", key);

        return lockFactory.get(key).read().withLock(new Supplier<ConsumerToken>()
        {
            public ConsumerToken get()
            {
                ConsumerToken token = cache.get(key);
                if (token == null)
                {
                    token = delegateStore.get(key);
                    if (token != null)
                    {
                        cache.put(key, token);
                    }
                }
                return token;
            }
        });
    }

    public Map<Key, ConsumerToken> getConsumerTokens(String consumerKey)
    {
        return delegateStore.getConsumerTokens(consumerKey);
    }

    public ConsumerToken put(final Key key, final ConsumerToken token)
    {
        notNull("key", key);
        notNull("token", token);

        return lockFactory.get(key).write().withLock(new Supplier<ConsumerToken>()
        {
            public ConsumerToken get()
            {
                final ConsumerToken storedToken = delegateStore.put(key, token);
                cache.put(key, storedToken);
                return storedToken;
            }
        });
    }

    public void remove(final Key key)
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

    public void removeTokensForConsumer(final String consumerKey)
    {
        notNull("consumerKey", consumerKey);

        //don't really care about concurrency here.  The worst that will happen is that a stale/invalid token
        //will be returned from the cache that's not in the database any longer.  This will result in a failed
        //Oauth request.
        delegateStore.removeTokensForConsumer(consumerKey);
        //perhaps we should iterate here, and only remove the tokens for the consumer however this
        //should be a very infrequent operation.
        cache.clear();
    }
}
