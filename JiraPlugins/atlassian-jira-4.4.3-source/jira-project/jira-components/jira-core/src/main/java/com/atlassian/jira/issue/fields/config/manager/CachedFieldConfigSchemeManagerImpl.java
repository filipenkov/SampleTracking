package com.atlassian.jira.issue.fields.config.manager;

import com.atlassian.bandana.BandanaContext;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.context.persistence.FieldConfigContextPersister;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigSchemePersister;
import com.atlassian.jira.util.cache.JiraCachedManager;
import com.atlassian.jira.util.map.CacheObject;
import org.apache.commons.collections.keyvalue.MultiKey;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple caching wrapper.
 * <p/>
 * NOTE : you may be wondering about the cache invalidation strategy on this cache.  Will the top level classes
 * that use this cache such as {@link com.atlassian.jira.issue.CustomFieldManager@refreshCache} call {@link #init()}
 * and this clears the cache.
 * <p/>
 * TODO: This probably should be rewritten so that the upper lays of code are not responsible for clearing the lower level caches
 * and also the "cache inheritance" pattern should be removed. *
 */
public class CachedFieldConfigSchemeManagerImpl extends FieldConfigSchemeManagerImpl implements JiraCachedManager, Startable
{
    private static final CacheObject<Long> NULL = new CacheObject<Long>(null);
    // ------------------------------------------------------------------------------------------------- Type Properties

    private final ConcurrentMap<MultiKey, CacheObject<Long>> cache = new ConcurrentHashMap<MultiKey, CacheObject<Long>>();
    private final EventPublisher eventPublisher;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public CachedFieldConfigSchemeManagerImpl(final FieldConfigSchemePersister configSchemePersister, final FieldConfigContextPersister contextPersister,
            final JiraContextTreeManager treeManager, final FieldConfigManager configManager, final EventPublisher eventPublisher)
    {
        super(configSchemePersister, contextPersister, treeManager, configManager);
        this.eventPublisher = eventPublisher;
        init();
    }

    // --------------------------------------------------------------------------------------- Bandana Interface Methods
    @Override
    public synchronized void init()
    {
        super.init();
        cache.clear();
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cache.clear();
    }

    @Override
    public Object getValue(final BandanaContext context, final String key, final boolean lookUp)
    {
        final MultiKey multiKey = new MultiKey(context, key, Boolean.valueOf(lookUp));

        final CacheObject<Long> cacheObject = cache.get(multiKey);
        if (cacheObject != null)
        {
            final Long schemeId = cacheObject.getValue();
            return getFieldConfigSchemePersister().getFieldConfigScheme(schemeId);
        }
        else
        {
            final FieldConfigScheme scheme = (FieldConfigScheme) super.getValue(context, key, lookUp);
            cache.putIfAbsent(multiKey, (scheme == null) ? NULL : new CacheObject<Long>(scheme.getId()));
            return scheme;
        }
    }

    // -------------------------------------------------------------------------------------------------- Caching methods

    public long getCacheSize()
    {
        return cache.size();
    }

    public void refreshCache()
    {
        init();
    }
}
