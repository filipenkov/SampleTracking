package com.atlassian.crowd.event.directory;

import com.atlassian.crowd.event.DirectoryEvent;
import com.atlassian.crowd.embedded.api.Directory;

public class DirectoryUpdatedEvent extends DirectoryEvent
{
    public DirectoryUpdatedEvent(Object source, Directory directory)
    {
        super(source, directory);
    }
}
