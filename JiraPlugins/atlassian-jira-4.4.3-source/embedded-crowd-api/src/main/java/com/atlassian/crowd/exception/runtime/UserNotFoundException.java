package com.atlassian.crowd.exception.runtime;

/**
 * Thrown when the user is not found.
 *
 * @since v2.1
 */
public class UserNotFoundException extends CrowdRuntimeException
{
    private final String userName;

    public UserNotFoundException(String userName)
    {
        this(userName, null);
    }

    public UserNotFoundException(String userName, Throwable t)
    {
        super("User <" + userName + "> does not exist", t);
        this.userName = userName;
    }

    public String getUserName()
    {
        return userName;
    }
}
