package com.atlassian.crowd.event.directory;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.event.DirectoryEvent;

public class DirectoryCreatedEvent extends DirectoryEvent
{
    public DirectoryCreatedEvent(Object source, Directory directory)
    {
        super(source, directory);
    }
}
