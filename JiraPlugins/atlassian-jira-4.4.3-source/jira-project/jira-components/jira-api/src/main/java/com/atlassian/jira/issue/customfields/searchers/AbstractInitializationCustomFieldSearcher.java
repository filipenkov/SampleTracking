package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract base class for searchers that performs the init methods for a searcher.
 *
 * @since v4.0
 */
public abstract class AbstractInitializationCustomFieldSearcher implements CustomFieldSearcher
{
    private final AtomicReference<CustomFieldSearcherModuleDescriptor> customFieldSearcherModuleDescriptor = new AtomicReference<CustomFieldSearcherModuleDescriptor>(null);

    @Override
    public void init(CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor)
    {
        this.customFieldSearcherModuleDescriptor.set(customFieldSearcherModuleDescriptor);
    }

    @Override
    public CustomFieldSearcherModuleDescriptor getDescriptor()
    {
        return customFieldSearcherModuleDescriptor.get();
    }
}