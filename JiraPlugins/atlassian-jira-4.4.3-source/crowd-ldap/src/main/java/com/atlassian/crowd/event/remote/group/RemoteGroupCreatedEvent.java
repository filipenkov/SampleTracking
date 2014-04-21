package com.atlassian.crowd.event.remote.group;

import com.atlassian.crowd.model.group.Group;

public class RemoteGroupCreatedEvent extends RemoteGroupCreatedOrUpdatedEvent implements RemoteGroupEvent
{
    public RemoteGroupCreatedEvent(Object source, long directoryID, Group group)
    {
        super(source, directoryID, group);
    }
}
