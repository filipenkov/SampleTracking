package com.atlassian.crowd.event.remote.principal;

import com.atlassian.crowd.model.user.User;

public class RemoteUserUpdatedEvent extends RemoteUserCreatedOrUpdatedEvent implements RemoteUserEvent
{
    public RemoteUserUpdatedEvent(Object source, long directoryID, User user)
    {
        super(source, directoryID, user);
    }
}
