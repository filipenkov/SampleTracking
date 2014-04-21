package com.atlassian.crowd.event.group;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.event.DirectoryEvent;
import com.atlassian.crowd.model.group.Group;

/**
 * An Event that represents the updating of a {@link com.atlassian.crowd.model.group.Group}
 */
public class GroupUpdatedEvent extends DirectoryEvent
{
    private final Group group;

    public GroupUpdatedEvent(Object source, Directory directory, Group group)
    {
        super(source, directory);
        this.group = group;
    }

    public Group getGroup()
    {
        return group;
    }
}
