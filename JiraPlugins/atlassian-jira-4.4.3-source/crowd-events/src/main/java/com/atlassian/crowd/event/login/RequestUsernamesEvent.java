package com.atlassian.crowd.event.login;

import com.atlassian.crowd.model.user.User;

import java.util.List;

/**
 * An event fired when the user requests their usernames to be sent to their email.
 */
public class RequestUsernamesEvent
{
    private final User user;
    private final List<String> usernames;

    public RequestUsernamesEvent(final User user, final List<String> usernames)
    {
        this.user = user;
        this.usernames = usernames;
    }

    public User getUser()
    {
        return user;
    }

    public List<String> getUsernames()
    {
        return usernames;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RequestUsernamesEvent))
        {
            return false;
        }

        final RequestUsernamesEvent that = (RequestUsernamesEvent) o;

        if (!usernames.equals(that.usernames))
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
        result = 31 * result + usernames.hashCode();
        return result;
    }
}
