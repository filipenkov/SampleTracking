package com.atlassian.applinks.core.property;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.PropertySet;
import com.atlassian.applinks.spi.application.TypeId;

public interface PropertyService
{

    PropertySet getProperties(ApplicationLink application);

    EntityLinkProperties getProperties(EntityLink entity);

    ApplicationLinkProperties getApplicationLinkProperties(ApplicationId id);

    PropertySet getGlobalAdminProperties();

    PropertySet getLocalEntityProperties(String localEntityKey, TypeId localEntityTypeId);

}
