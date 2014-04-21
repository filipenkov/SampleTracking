package com.atlassian.crowd.event.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.event.DirectoryEvent;

public class UserAuthenticationFailedInvalidAuthenticationEvent extends DirectoryEvent
{
    private final String username;

    public UserAuthenticationFailedInvalidAuthenticationEvent(Object source, Directory directory, String username)
    {
        super(source, directory);
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof UserAuthenticationFailedInvalidAuthenticationEvent))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        UserAuthenticationFailedInvalidAuthenticationEvent that = (UserAuthenticationFailedInvalidAuthenticationEvent) o;

        if (!username.equals(that.username))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + username.hashCode();
        return result;
    }
}
