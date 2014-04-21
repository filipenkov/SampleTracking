package com.atlassian.crowd.event.application;

import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.RemoteAddress;

/**
 * Event which occurs when a RemoteAddress is added to an Application's list of allowed remote addresses.
 */
public class ApplicationRemoteAddressAddedEvent
{
    private final Application application;
    private final RemoteAddress remoteAddress;

    public ApplicationRemoteAddressAddedEvent(final Application application, final RemoteAddress remoteAddress)
    {
        this.application = application;
        this.remoteAddress = remoteAddress;
    }

    public Application getApplication()
    {
        return application;
    }

    public RemoteAddress getRemoteAddress()
    {
        return remoteAddress;
    }
}
