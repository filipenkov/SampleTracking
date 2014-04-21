package com.atlassian.jira.extra.icalfeed.dateprovider;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

public class DateProviderModuleDescriptor extends AbstractModuleDescriptor<DateProvider>
{
    public DateProviderModuleDescriptor(ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public DateProvider getModule()
    {
        return moduleFactory.createModule(moduleClassName, this);
    }
}
