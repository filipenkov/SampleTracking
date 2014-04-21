package com.atlassian.crowd.exception;

/**
 * Thrown to indicate that a user already exists in the directory.
 */
public final class UserAlreadyExistsException extends CrowdException
{
    private final long directoryId;
    private final String userName;

    public UserAlreadyExistsException(long directoryId, String name)
    {
        super("User already exists in directory [" + directoryId + "] with name [" + name + "]");
        this.directoryId = directoryId;
        this.userName = name;
    }

    public long getDirectoryId()
    {
        return directoryId;
    }

    public String getUserName()
    {
        return userName;
    }
}
