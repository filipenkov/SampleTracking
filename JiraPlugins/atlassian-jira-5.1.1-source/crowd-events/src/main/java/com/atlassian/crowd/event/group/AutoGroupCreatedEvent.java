package com.atlassian.crowd.event.group;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.group.Group;

/**
 * Group was automatically created.
 * <p/>
 * Triggered when a group was automatically created internally on a delegated authentication
 * directory after successful LDAP authentication.
 */
public class AutoGroupCreatedEvent extends GroupCreatedEvent
{

    public AutoGroupCreatedEvent(Object source, Directory directory, Group group)
    {
        super(source, directory, group);
    }

}
