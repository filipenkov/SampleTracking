package com.atlassian.crowd.event.role;

import com.atlassian.crowd.event.DirectoryEvent;
import com.atlassian.crowd.embedded.api.Directory;

/**
 * An Event that represents the creation of a Role + Principal membership
 */
public class RoleMembershipCreatedEvent extends DirectoryEvent
{
    private final String username;
    private final String rolename;

    public RoleMembershipCreatedEvent(Object source, Directory directory, String username, String rolename)
    {
        super(source, directory);
        this.username = username;
        this.rolename = rolename;
    }

    public String getUsername()
    {
        return username;
    }

    public String getRolename()
    {
        return rolename;
    }
}
