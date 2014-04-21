package com.atlassian.crowd.event.directory;

import com.atlassian.crowd.event.DirectoryEvent;
import com.atlassian.crowd.embedded.api.Directory;

public class DirectoryDeletedEvent extends DirectoryEvent
{
    public DirectoryDeletedEvent(Object source, Directory directory)
    {
        super(source, directory);
    }
}
