package com.atlassian.jira.issue.fields.option;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.cache.JiraCachedManager;
import com.atlassian.jira.util.cache.ManagedCache;
import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.util.Collection;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@EventComponent
public class CachedOptionSetManager implements OptionSetManager, JiraCachedManager
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private final ManagedCache<FieldConfig, OptionSet> cache = ManagedCache.newManagedCache(
            new OptionSetForConfig(), new KeyFromFieldConfig()
    );

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final OptionSetManagerImpl optionSetManager;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public CachedOptionSetManager(OptionSetPersister optionSetPersister, ConstantsManager constantsManager)
    {
        optionSetManager = new OptionSetManagerImpl(optionSetPersister, constantsManager);
        init();
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refreshCache();
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public void init()
    {
        cache.clear();
    }

    public OptionSet getOptionsForConfig(FieldConfig config)
    {
        return cache.get(notNull("config", config));
    }

    public OptionSet createOptionSet(FieldConfig config, Collection optionIds)
    {
        notNull("config", config);
        try
        {
            return optionSetManager.createOptionSet(config, optionIds);
        }
        finally
        {
            cache.remove(config);
        }
    }

    public OptionSet updateOptionSet(FieldConfig config, Collection optionIds)
    {
        notNull("config", config);
        try
        {
            return optionSetManager.updateOptionSet(config, optionIds);
        }
        finally
        {
            cache.remove(config);
        }
    }

    public void removeOptionSet(FieldConfig config)
    {
        notNull("config", config);
        try
        {
            optionSetManager.removeOptionSet(config);
        }
        finally
        {
            cache.remove(config);
        }
    }

    public long getCacheSize()
    {
        return cache.size();
    }

    public void refreshCache()
    {
        init();
    }

    // -------------------------------------------------------------------------------------------------- Private Helpers
    private class OptionSetForConfig implements Function<FieldConfig, OptionSet>
    {
        public OptionSet apply(@Nullable final FieldConfig fieldConfig)
        {
            return optionSetManager.getOptionsForConfig(fieldConfig);
        }
    }
    
    private static class KeyFromFieldConfig implements Function<FieldConfig, Long>
    {
        public Long apply(@Nullable final FieldConfig fieldConfig)
        {
            return fieldConfig.getId();
        }
    }
}
