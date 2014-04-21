package com.atlassian.applinks.core;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.applinks.spi.application.TypeId;

public interface InternalTypeAccessor extends TypeAccessor
{

    ApplicationType loadApplicationType(String typeClassName);

    ApplicationType loadApplicationType(TypeId typeId);

    EntityType loadEntityType(String typeClassName);

    EntityType loadEntityType(TypeId typeId);

    Iterable<? extends EntityType> getEntityTypesForApplicationType(TypeId typeId);

}
