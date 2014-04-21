package com.atlassian.crowd.event.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.user.User;

/**
 * A simple ResetPasswordEvent. Fired when the password is reset (via "I forgot my password"), rather than changed to a
 * new value.
 */
public class ResetPasswordEvent extends UserUpdatedEvent
{
    private final String newPassword;

    public ResetPasswordEvent(Object source, Directory directory, User user, String newPassword)
    {
        super(source, directory, user);
        this.newPassword = newPassword;
    }

    public String getNewPassword()
    {
        return newPassword;
    }
}
