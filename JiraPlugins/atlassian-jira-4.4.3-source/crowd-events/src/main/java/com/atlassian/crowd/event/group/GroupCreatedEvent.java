package com.atlassian.crowd.event.group;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.event.DirectoryEvent;
import com.atlassian.crowd.model.group.Group;

/**
 * An Event that represents the creation of a {@link com.atlassian.crowd.model.group.Group}
 */
public class GroupCreatedEvent extends DirectoryEvent
{
    private final Group group;

    public GroupCreatedEvent(Object source, Directory directory, Group group)
    {
        super(source, directory);
        this.group = group;
    }

    public Group getGroup()
    {
        return group;
    }
}
