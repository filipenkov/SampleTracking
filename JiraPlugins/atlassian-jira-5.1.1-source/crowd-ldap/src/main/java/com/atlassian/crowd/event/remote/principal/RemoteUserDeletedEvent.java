package com.atlassian.crowd.event.remote.principal;

import com.atlassian.crowd.event.remote.RemoteEntityDeletedEvent;

public class RemoteUserDeletedEvent extends RemoteEntityDeletedEvent implements RemoteUserEvent
{
    public RemoteUserDeletedEvent(Object source, long directoryID, String username)
    {
        super(source, directoryID, username);
    }
}
