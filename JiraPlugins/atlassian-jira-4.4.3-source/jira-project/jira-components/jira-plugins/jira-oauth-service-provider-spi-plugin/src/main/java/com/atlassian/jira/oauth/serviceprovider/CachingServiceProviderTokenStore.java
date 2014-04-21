package com.atlassian.jira.oauth.serviceprovider;

import static com.atlassian.jira.util.collect.LRUMap.synchronizedLRUMap;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import com.atlassian.oauth.serviceprovider.ServiceProviderToken;
import com.atlassian.oauth.serviceprovider.ServiceProviderTokenStore;
import com.atlassian.oauth.serviceprovider.StoreException;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.ManagedLock;
import com.atlassian.util.concurrent.ManagedLocks;
import com.atlassian.util.concurrent.Supplier;

import java.util.Map;

/**
 * Caching Service provider token store, responsible for caching OAuth service provider authentication tokens.
 *
 * @since v4.0
 */
public class CachingServiceProviderTokenStore implements ServiceProviderTokenStore
{
    private final Map<String, ServiceProviderToken> cache = synchronizedLRUMap(4000);

    private ServiceProviderTokenStore delegateStore;
    private final Function<String, ManagedLock.ReadWrite> lockFactory = ManagedLocks.weakReadWriteManagedLockFactory();

    public CachingServiceProviderTokenStore(final ServiceProviderTokenStore delegateStore)
    {
        this.delegateStore = delegateStore;
    }

    public ServiceProviderToken get(final String token) throws StoreException
    {
        notNull("token", token);

        return lockFactory.get(token).read().withLock(new Supplier<ServiceProviderToken>()
        {
            public ServiceProviderToken get()
            {
                ServiceProviderToken ret = cache.get(token);
                if (ret == null)
                {
                    ret = delegateStore.get(token);
                    if (ret != null)
                    {
                        cache.put(token, ret);
                    }
                }
                return ret;
            }
        });
    }

    public Iterable<ServiceProviderToken> getAccessTokensForUser(final String username)
    {
        //this will only be called when a user goes to this section in their profile,
        //so this can be a non-caching call.
        return delegateStore.getAccessTokensForUser(username);
    }

    public ServiceProviderToken put(final ServiceProviderToken token) throws StoreException
    {
        notNull("token", token);

        return lockFactory.get(token.getToken()).write().withLock(new Supplier<ServiceProviderToken>()
        {
            public ServiceProviderToken get()
            {
                final ServiceProviderToken storedToken = delegateStore.put(token);
                cache.put(storedToken.getToken(), storedToken);
                return storedToken;
            }
        });
    }

    public void remove(final String token) throws StoreException
    {
        notNull("token", token);

        lockFactory.get(token).write().withLock(new Runnable()
        {
            public void run()
            {
                delegateStore.remove(token);
                cache.remove(token);
            }
        });
    }

    public void removeExpiredTokens() throws StoreException
    {
        //Will be called every 10 hrs.  Perhaps we should do some smarter cache removal here.
        delegateStore.removeExpiredTokens();
        cache.clear();
    }

    public void removeByConsumer(final String consumerKey)
    {
        //This will only be called when an admin removes an oauth consumer, so not very often.
        delegateStore.removeByConsumer(consumerKey);
        cache.clear();
    }
}
