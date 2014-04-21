package com.atlassian.crowd.event.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.user.User;

/**
 * User was automatically created.
 *
 * Triggered when a user was automatically created internally on a delegated authentication
 * directory, either after successful LDAP authentication or when specifically requested on the
 * directory itself.
 */
public class AutoUserCreatedEvent extends UserCreatedEvent
{
    public AutoUserCreatedEvent(Object source, Directory directory, User user)
    {
        super(source, directory, user);
    }
}
