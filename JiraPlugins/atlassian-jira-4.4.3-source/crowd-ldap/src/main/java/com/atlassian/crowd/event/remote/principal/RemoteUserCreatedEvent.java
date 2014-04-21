package com.atlassian.crowd.event.remote.principal;

import com.atlassian.crowd.model.user.User;

public class RemoteUserCreatedEvent extends RemoteUserCreatedOrUpdatedEvent implements RemoteUserEvent
{
    public RemoteUserCreatedEvent(Object source, long directoryID, User user)
    {
        super(source, directoryID, user);
    }
}
