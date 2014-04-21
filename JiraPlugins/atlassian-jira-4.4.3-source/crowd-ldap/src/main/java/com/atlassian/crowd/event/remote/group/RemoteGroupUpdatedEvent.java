package com.atlassian.crowd.event.remote.group;

import com.atlassian.crowd.model.group.Group;

public class RemoteGroupUpdatedEvent extends RemoteGroupCreatedOrUpdatedEvent implements RemoteGroupEvent
{
    public RemoteGroupUpdatedEvent(Object source, long directoryID, Group group)
    {
        super(source, directoryID, group);
    }
}
