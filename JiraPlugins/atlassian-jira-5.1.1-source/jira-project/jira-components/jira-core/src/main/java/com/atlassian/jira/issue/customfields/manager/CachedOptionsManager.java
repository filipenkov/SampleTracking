package com.atlassian.jira.issue.customfields.manager;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.CaseFolding;
import com.atlassian.jira.util.CollectionReorderer;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Cache for Options Manager. Not particularly neat More a problem with how the OptionsManager is used really
 */
@EventComponent
public class CachedOptionsManager extends DefaultOptionsManager
{
    // ------------------------------------------------------------------------------------------------- Type Properties
    private final ConcurrentMap<Long, Options> optionsCache = new ConcurrentHashMap<Long, Options>();
    private final ConcurrentMap<Long, Option> optionCache = new ConcurrentHashMap<Long, Option>();
    private final ConcurrentMap parentcache = new ConcurrentHashMap();
    private final ConcurrentMap<String, List<Option>> valueCache = new ConcurrentHashMap<String, List<Option>>();
    private final AtomicReference<List<Option>> allCache = new AtomicReference<List<Option>>(null);
    // ---------------------------------------------------------------------------------------------------- Dependencies

    // ---------------------------------------------------------------------------------------------------- Constructors

    public CachedOptionsManager(OfBizDelegator delegator, CollectionReorderer<Option> reorderer,
            FieldConfigManager fieldConfigManager)
    {
        super(delegator, reorderer, fieldConfigManager);
        init();
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        init();
    }

    private synchronized void init()
    {
        optionsCache.clear();
        parentcache.clear();
        optionCache.clear();
        valueCache.clear();
        allCache.set(null);
    }

    @Override
    public List<Option> getAllOptions()
    {
        List<Option> allOptions = allCache.get();
        if (allOptions == null)
        {
            allOptions = super.getAllOptions();
            allCache.compareAndSet(null, allOptions);
        }
        return allOptions;
    }

    @Override
    public List<Option> findByOptionValue(String value)
    {
        if (value == null)
        {
            return null;
        }

        // make the cache lookup case insensitive too
        value = CaseFolding.foldString(value);

        List<Option> options = valueCache.get(value);
        if (options != null)
        {
            return options;
        }

        options = super.findByOptionValue(value);
        if (options != null)
        {
            valueCache.put(value, options);
        }
        return options;
    }

    public Options getOptions(FieldConfig fieldConfig)
    {
        final Long key = (fieldConfig == null) ? null : fieldConfig.getId();
        if (key == null)
        {
            // we can only cache something, not nothing
            return super.getOptions(fieldConfig);
        }
        Options value = (Options) optionsCache.get(key);
        if (value != null)
        {
            return value;
        }

        final Options options = super.getOptions(fieldConfig);
        if (options != null)
        {
            final Options result = optionsCache.putIfAbsent(key, options);
            return (result == null) ? options : result;
        }
        else
        {
            return options;
        }
    }

    public void setRootOptions(FieldConfig fieldConfig, Options options)
    {
        super.setRootOptions(fieldConfig, options);

        init();
    }

    public void removeCustomFieldOptions(CustomField customField)
    {
        super.removeCustomFieldOptions(customField);

        // Nuke it all if a custom field is removed
        init();
    }

    @Override
    public void removeCustomFieldConfigOptions(final FieldConfig fieldConfig)
    {
        super.removeCustomFieldConfigOptions(fieldConfig);

        // Nuke it all if a custom field is removed
        init();
    }

    public void updateOptions(Collection options)
    {
        super.updateOptions(options);

        init();
    }

    public Option createOption(FieldConfig fieldConfig, Long parentOptionId, Long sequence, String value)
    {
        Option option = super.createOption(fieldConfig, parentOptionId, sequence, value);
        init();
        return option;
    }

    public void deleteOptionAndChildren(Option option)
    {
        super.deleteOptionAndChildren(option);

        init();
    }

	public void setValue(Option option, String value) {
		super.setValue(option, value);
		init();
	}

	public void disableOption(Option option) {
		super.disableOption(option);
        init();
	}

	public void enableOption(Option option) {
		super.enableOption(option);
        init();
	}

    public Option findByOptionId(Long optionId)
    {
        if (optionId == null)
        {
            return null;
        }
        Option value = optionCache.get(optionId);
        if (value != null)
        {
            return value;
        }

        final Option option = super.findByOptionId(optionId);
        if (option != null)
        {
            final Option result = optionCache.putIfAbsent(optionId, option);
            return (result == null) ? option : result;
        }
        else
        {
            return option;
        }
    }


    public List findByParentId(Long parentOptionId)
    {
        if (parentOptionId == null)
        {
            return null;
        }
        List value = (List) parentcache.get(parentOptionId);
        if (value != null)
        {
            return value;
        }

        final List options = super.findByParentId(parentOptionId);
        if (options != null)
        {
            parentcache.put(parentOptionId, options);
        }
        return options;
    }
}
