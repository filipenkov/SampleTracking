package com.atlassian.upm.license.internal;

import com.atlassian.confluence.event.events.admin.LicenceUpdatedEvent;
import com.atlassian.upm.license.internal.HostLicenseEventReader;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConfluenceHostLicenseEventReader implements HostLicenseEventReader
{
    @Override
    public boolean isHostLicenseUpdated(Object event)
    {
        checkNotNull(event, "event");
        return LicenceUpdatedEvent.class.isAssignableFrom(event.getClass());
    }
}
