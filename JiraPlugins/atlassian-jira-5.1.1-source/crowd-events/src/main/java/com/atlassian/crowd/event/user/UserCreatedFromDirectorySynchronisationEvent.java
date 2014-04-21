package com.atlassian.crowd.event.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.user.User;

/**
 * An Event that represents the creation of a {@link com.atlassian.crowd.model.user.User} as a result of directory synchronisation.
 */
public class UserCreatedFromDirectorySynchronisationEvent extends UserCreatedEvent
{
    public UserCreatedFromDirectorySynchronisationEvent(Object source, Directory directory, User user)
    {
        super(source, directory, user);
    }
}
