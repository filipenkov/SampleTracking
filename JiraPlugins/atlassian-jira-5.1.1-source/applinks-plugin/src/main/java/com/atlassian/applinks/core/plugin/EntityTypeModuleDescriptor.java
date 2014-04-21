package com.atlassian.applinks.core.plugin;

import com.atlassian.applinks.api.EntityType;
import com.atlassian.plugin.module.ModuleFactory;

public class EntityTypeModuleDescriptor extends AbstractAppLinksTypeModuleDescriptor<EntityType>
{
    public EntityTypeModuleDescriptor(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }
}
