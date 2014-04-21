package com.atlassian.jira.issue.fields.config.persistence;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.issue.context.persistence.FieldConfigContextPersister;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.cache.JiraCachedManager;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.map.CacheObject;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A simple caching wrapper
 * <p/>
 * NOTE : you may be wondering about the cache invalidation strategy on this cache.  Will the top level classes
 * that use this cache such as {@link com.atlassian.jira.issue.CustomFieldManager@refreshCache} call {@link #init()}
 * and this clears the cache.
 * <p/>
 * TODO: This probably should be rewritten so that the upper lays of code are not responsible for clearing the lower level caches
 * and also the "cache inheritance" pattern should be removed.
 */
public class CachedFieldConfigSchemePersister extends FieldConfigSchemePersisterImpl implements JiraCachedManager, Startable
{
    private final ConcurrentMap<Long, CacheObject<FieldConfigScheme>> cacheById = new ConcurrentHashMap<Long, CacheObject<FieldConfigScheme>>();
    private final ConcurrentMap<String, CacheObject<List<FieldConfigScheme>>> cacheByCustomField = new ConcurrentHashMap<String, CacheObject<List<FieldConfigScheme>>>();
    private final ConcurrentMap<Long, FieldConfigScheme> cacheByFieldConfigId = new ConcurrentHashMap<Long, FieldConfigScheme>();
    private final EventPublisher eventPublisher;

    public CachedFieldConfigSchemePersister(OfBizDelegator delegator, ConstantsManager constantsManager, FieldConfigPersister fieldConfigPersister, final EventPublisher eventPublisher, final FieldConfigContextPersister fieldContextPersister)
    {
        super(delegator, constantsManager, fieldConfigPersister, fieldContextPersister);
        this.eventPublisher = eventPublisher;
        init();
    }

    @Override
    public synchronized void init()
    {
        super.init();
        cacheById.clear();
        cacheByCustomField.clear();
        cacheByFieldConfigId.clear();
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cacheById.clear();
        cacheByCustomField.clear();
        cacheByFieldConfigId.clear();
    }

    @Override
    public FieldConfigScheme getFieldConfigScheme(Long configSchemeId)
    {
        if (configSchemeId != null)
        {
            CacheObject cacheObject = cacheById.get(configSchemeId);
            if (cacheObject != null)
            {
                return (FieldConfigScheme) cacheObject.getValue();
            }
            else
            {
                FieldConfigScheme scheme = super.getFieldConfigScheme(configSchemeId);
                cacheById.putIfAbsent(configSchemeId, new CacheObject<FieldConfigScheme>(scheme));
                return scheme;
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public List<FieldConfigScheme> getConfigSchemesForCustomField(final ConfigurableField field)
    {
        if (field == null)
        {
            return null;
        }

        final String customFieldId = field.getId();
        CacheObject<List<FieldConfigScheme>> cacheObject = cacheByCustomField.get(customFieldId);
        if (cacheObject != null)
        {
            return cacheObject.getValue();
        }

        // Store in both custom field cache & checks the id cache
        List<FieldConfigScheme> schemes = CollectionUtil.copyAsImmutableList(super.getConfigSchemesForCustomField(field));
        cacheByCustomField.putIfAbsent(customFieldId, new CacheObject<List<FieldConfigScheme>>(schemes));

        if (schemes != null)
        {
            for (final FieldConfigScheme scheme : schemes)
            {
                final Long schemeId = scheme.getId();
                if (!cacheById.containsKey(schemeId))
                {
                    cacheById.putIfAbsent(schemeId, new CacheObject<FieldConfigScheme>(scheme));
                }
            }
        }
        return schemes;
    }

    @Override
    public FieldConfigScheme getConfigSchemeForFieldConfig(final FieldConfig fieldConfig)
    {
        if (fieldConfig == null)
        {
            return null;
        }

        Long fieldConfigId = fieldConfig.getId();

        FieldConfigScheme scheme = cacheByFieldConfigId.get(fieldConfigId);
        if (scheme == null)
        {
            scheme = super.getConfigSchemeForFieldConfig(fieldConfig);
            final FieldConfigScheme result = cacheByFieldConfigId.putIfAbsent(fieldConfigId, scheme);
            return (result == null) ? scheme : result;
        }
        else
        {
            return scheme;
        }
    }

    public long getCacheSize()
    {
        return cacheByCustomField.size() + cacheById.size() + cacheByFieldConfigId.size();
    }

    @Override
    public void remove(Long fieldConfigSchemeId)
    {
        final CacheObject<FieldConfigScheme> cacheObject = cacheById.get(fieldConfigSchemeId);
        //only remove the object from the cache if it exists!
        if (cacheObject != null)
        {
            final FieldConfigScheme configScheme = cacheObject.getValue();
            cacheById.remove(fieldConfigSchemeId);
            final ConfigurableField field = configScheme.getField();
            if (field != null)
            {
                cacheByCustomField.remove(field.getId());
            }

            final FieldConfig config = configScheme.getOneAndOnlyConfig();
            if (config != null)
            {
                cacheByFieldConfigId.remove(config.getId());
            }
        }
        super.remove(fieldConfigSchemeId);
    }

    public void refreshCache()
    {
        init();
    }
}
