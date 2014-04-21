package com.atlassian.crowd.event.role;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.event.DirectoryEvent;

/**
 * An Event that represents the deletion of a Role + Principal membership
 */
public class RoleMembershipDeletedEvent extends DirectoryEvent
{
    private final String username;
    private final String roleName;

    public RoleMembershipDeletedEvent(Object source, Directory directory, String username, String roleName)
    {
        super(source, directory);
        this.username = username;
        this.roleName = roleName;
    }

    public String getUsername()
    {
        return username;
    }

    public String getRoleName()
    {
        return roleName;
    }
}
