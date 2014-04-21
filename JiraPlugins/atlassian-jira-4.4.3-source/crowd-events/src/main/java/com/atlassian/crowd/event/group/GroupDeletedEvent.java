package com.atlassian.crowd.event.group;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.event.DirectoryEvent;

/**
 * An Event that represents the removal of a {@link com.atlassian.crowd.model.group.Group}
 */
public class GroupDeletedEvent extends DirectoryEvent
{
    private final String groupName;

    public GroupDeletedEvent(Object source, Directory directory, String groupName)
    {
        super(source, directory);
        this.groupName = groupName;
    }

    public String getGroupName()
    {
        return groupName;
    }
}
