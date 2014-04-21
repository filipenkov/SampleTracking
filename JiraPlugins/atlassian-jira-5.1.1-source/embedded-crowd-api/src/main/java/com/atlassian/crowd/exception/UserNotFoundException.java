package com.atlassian.crowd.exception;

/**
 * Thrown when the specified user could not be found.
 */
public class UserNotFoundException extends ObjectNotFoundException
{
    private final String userName;

    public UserNotFoundException(String userName)
    {
        this(userName, null);
    }

    public UserNotFoundException(String userName, Throwable t)
    {
        super(String.format("User <%s> does not exist", userName), t);
        this.userName = userName;
    }

    /**
     * Returns the name of the user that could not be found.
     *
     * @return name of the user that could not be found
     */
    public String getUserName()
    {
        return userName;
    }
}
