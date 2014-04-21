package com.atlassian.crowd.event.role;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.event.DirectoryEvent;

/**
 * An Event that represents the removal of a Role (Group with GroupType.LEGACY_ROLE).
 */
public class RoleDeletedEvent extends DirectoryEvent
{
    private final String roleName;

    public RoleDeletedEvent(Object source, Directory directory, String roleName)
    {
        super(source, directory);
        this.roleName = roleName;
    }

    public String getRoleName()
    {
        return roleName;
    }
}
