package com.atlassian.plugins.rest.module.security.descriptor;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugins.rest.common.security.descriptor.CorsDefaults;
import com.atlassian.plugins.rest.common.security.descriptor.CorsDefaultsModuleDescriptor;

/**
 * Basic module descriptor for Cross-Origin Resource Sharing default instances
 */
public class CorsDefaultsModuleDescriptorImpl extends AbstractModuleDescriptor<CorsDefaults> implements CorsDefaultsModuleDescriptor
{
    private final ModuleFactory moduleFactory;

    public CorsDefaultsModuleDescriptorImpl(ModuleFactory moduleFactory)
    {
        this.moduleFactory = moduleFactory;
    }

    @Override
    public CorsDefaults getModule()
    {
        return moduleFactory.createModule(moduleClassName, this);
    }
}
