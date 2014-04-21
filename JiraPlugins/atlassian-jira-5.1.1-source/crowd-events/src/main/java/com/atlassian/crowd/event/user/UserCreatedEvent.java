package com.atlassian.crowd.event.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.event.DirectoryEvent;
import com.atlassian.crowd.model.user.User;

/**
 * An Event that represents the creation of a {@link com.atlassian.crowd.model.user.User}
 */
public class UserCreatedEvent extends DirectoryEvent
{
    private final User user;

    public UserCreatedEvent(Object source, Directory directory, User user)
    {
        super(source, directory);
        this.user = user;
    }

    public User getUser()
    {
        return user;
    }
}
