package com.atlassian.crowd.exception;

import com.atlassian.crowd.embedded.api.User;

public class InvalidUserException extends CrowdException
{
    private User user;

    public InvalidUserException()
    {
        super();
    }

    public InvalidUserException(User user, String message)
    {
        super(message);
        setUser(user);
    }

    public InvalidUserException(User user, Throwable cause)
    {
        super(cause);
        setUser(user);
    }

    public InvalidUserException(User user, String message, Throwable cause)
    {
        super(message, cause);
        setUser(user);
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public User getUser()
    {
        return user;
    }
}
