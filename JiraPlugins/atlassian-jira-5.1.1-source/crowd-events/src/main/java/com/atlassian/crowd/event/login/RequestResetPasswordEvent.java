package com.atlassian.crowd.event.login;

import com.atlassian.crowd.model.user.User;

/**
 * An event fired when the user requests their password to be reset.
 */
public class RequestResetPasswordEvent
{
    private final User user;
    private final String resetLink;

    /**
     * Constructs a new instance of RequestResetPasswordEvent.
     * 
     * @param user      user to email
     * @param resetLink link to set a new password
     */
    public RequestResetPasswordEvent(final User user, final String resetLink)
    {
        this.user = user;
        this.resetLink = resetLink;
    }

    public User getUser()
    {
        return user;
    }

    public String getResetLink()
    {
        return resetLink;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RequestResetPasswordEvent))
        {
            return false;
        }

        final RequestResetPasswordEvent that = (RequestResetPasswordEvent) o;

        if (!resetLink.equals(that.resetLink))
        {
            return false;
        }
        if (!user.equals(that.user))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = 17;
        result = 31 * result + user.hashCode();
        result = 31 * result + resetLink.hashCode();
        return result;
    }
}
