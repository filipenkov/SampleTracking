package com.atlassian.crowd.event.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.user.User;

/**
 * User was automatically updated.
 *
 * Triggered when a user was automatically updated internally on a delegated authentication
 * directory, either after successful LDAP authentication or when specifically requested on the
 * directory itself.
 */
public class AutoUserUpdatedEvent extends UserUpdatedEvent
{
    public AutoUserUpdatedEvent(Object source, Directory directory, User user)
    {
        super(source, directory, user);
    }
}
