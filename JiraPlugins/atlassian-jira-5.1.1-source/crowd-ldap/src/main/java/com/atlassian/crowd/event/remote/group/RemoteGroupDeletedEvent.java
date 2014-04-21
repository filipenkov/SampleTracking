package com.atlassian.crowd.event.remote.group;

import com.atlassian.crowd.event.remote.RemoteEntityDeletedEvent;

public class RemoteGroupDeletedEvent extends RemoteEntityDeletedEvent implements RemoteGroupEvent
{
    public RemoteGroupDeletedEvent(Object source, long directoryID, String groupName)
    {
        super(source, directoryID, groupName);
    }
}
