package com.atlassian.crowd.event.user;

import com.atlassian.crowd.event.DirectoryEvent;
import com.atlassian.crowd.embedded.api.Directory;

/**
 * An Event that represents the deletion of a {@link com.atlassian.crowd.model.user.User}
 */
public class UserDeletedEvent extends DirectoryEvent
{
    private final String username;

    public UserDeletedEvent(Object source, Directory directory, String username)
    {
        super(source, directory);
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }
}
