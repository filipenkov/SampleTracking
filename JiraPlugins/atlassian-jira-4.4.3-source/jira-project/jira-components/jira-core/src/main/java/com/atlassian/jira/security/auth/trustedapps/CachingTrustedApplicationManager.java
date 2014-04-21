package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TrustedApplicationManager that caches the info objects in memory.
 *
 * @since v3.12
 */
public final class CachingTrustedApplicationManager implements TrustedApplicationManager, Startable
{
    private final TrustedApplicationManager delegate;
    private final EventPublisher eventPublisher;

    /** when we clear the cache we clear the lazy loaded reference to the cache. */
    private final ResettableLazyReference<Cache> cache = new ResettableLazyReference<Cache>()
    {
        @Override
        protected Cache create()
        {
            return new Cache(delegate.getAll());
        }
    };

    public CachingTrustedApplicationManager(final TrustedApplicationManager delegate, final EventPublisher eventPublisher)
    {
        this.delegate = delegate;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cache.reset();
    }

    // --------------------------------------------------------------------------------------------------------- getters

    @Override
    public Set<TrustedApplicationInfo> getAll()
    {
        return cache.get().getAll();
    }

    @Override
    public TrustedApplicationInfo get(final String applicationId)
    {
        return cache.get().get(applicationId);
    }

    @Override
    public TrustedApplicationInfo get(final long id)
    {
        return cache.get().get(id);
    }

    // -------------------------------------------------------------------------------------------------------- mutators

    @Override
    public boolean delete(final User user, final long id)
    {
        final boolean result = delegate.delete(user, id);
        cache.reset();
        return result;
    }

    @Override
    public boolean delete(final com.opensymphony.user.User user, final long id)
    {
        return delete((User) user, id);
    }

    @Override
    public boolean delete(final User user, final String applicationId)
    {
        boolean result = delegate.delete(user, applicationId);
        cache.reset();
        return result;
    }

    @Override
    public TrustedApplicationInfo store(final User user, final TrustedApplicationInfo info)
    {
        final TrustedApplicationInfo result = delegate.store(user, info);
        cache.reset();
        return result;
    }

    @SuppressWarnings("deprecation")
    @Override
    public TrustedApplicationInfo store(final com.opensymphony.user.User user, final TrustedApplicationInfo info)
    {
        return store((User) user, info);
    }

    // --------------------------------------------------------------------------------------------------------- helpers

    /** Cache implementation that permananently caches ALL elements. */
    private static final class Cache
    {
        final Map<Long, TrustedApplicationInfo> byId;
        final Map<String, TrustedApplicationInfo> byAppId;

        Cache(final Set<TrustedApplicationInfo> infos)
        {
            final Map<Long, TrustedApplicationInfo> byId = new HashMap<Long, TrustedApplicationInfo>(infos.size());
            final Map<String, TrustedApplicationInfo> byAppId = new HashMap<String, TrustedApplicationInfo>(infos.size());
            for (final TrustedApplicationInfo info : infos)
            {
                byId.put(new Long(info.getNumericId()), info);
                byAppId.put(info.getID(), info);
            }
            this.byId = Collections.unmodifiableMap(byId);
            this.byAppId = Collections.unmodifiableMap(byAppId);
        }

        TrustedApplicationInfo get(final long id)
        {
            return byId.get(new Long(id));
        }

        TrustedApplicationInfo get(final String applicationId)
        {
            return byAppId.get(applicationId);
        }

        Set<TrustedApplicationInfo> getAll()
        {
            return ImmutableSet.copyOf(byId.values());
        }
    }
}
