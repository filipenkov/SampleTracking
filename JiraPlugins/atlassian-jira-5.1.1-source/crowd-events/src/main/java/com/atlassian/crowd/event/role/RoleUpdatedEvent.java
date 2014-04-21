package com.atlassian.crowd.event.role;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.event.DirectoryEvent;
import com.atlassian.crowd.model.group.Group;

/**
 * An Event that represents the updating of a Role (Group with GroupType.LEGACY_ROLE).
 */
public class RoleUpdatedEvent extends DirectoryEvent
{
    private final Group role;

    public RoleUpdatedEvent(Object source, Directory directory, Group role)
    {
        super(source, directory);
        this.role = role;
    }

    public Group getRole()
    {
        return role;
    }
}
