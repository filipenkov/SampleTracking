package com.atlassian.crowd.event.remote.principal;

import com.atlassian.crowd.event.remote.ActiveDirectoryEntityDeletedEvent;
import com.atlassian.crowd.model.Tombstone;

public class ActiveDirectoryUserDeletedEvent extends ActiveDirectoryEntityDeletedEvent
{
    public ActiveDirectoryUserDeletedEvent(Object source, long directoryID, Tombstone tombstone)
    {
        super(source, directoryID, tombstone);
    }
}
