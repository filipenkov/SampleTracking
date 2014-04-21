package com.atlassian.crowd.exception;

import java.util.Set;

/**
 * Thrown to indicate that a bulk add operation has failed.
 */
public class BulkAddFailedException extends Exception
{
    private final Set<String> failedUsers;
    private final Set<String> existingUsers;

    public BulkAddFailedException(final Set<String> failedUsers, final Set<String> existingUsers)
    {
        super();
        this.failedUsers = failedUsers;
        this.existingUsers = existingUsers;
    }

    public BulkAddFailedException(final String message, final Set<String> failedUsers, final Set<String> existingUsers)
    {
        super(message);
        this.failedUsers = failedUsers;
        this.existingUsers = existingUsers;
    }

    public BulkAddFailedException(final String message, final Set<String> failedUsers, final Set<String> existingUsers, final Throwable throwable)
    {
        super(message, throwable);
        this.failedUsers = failedUsers;
        this.existingUsers = existingUsers;
    }

    /**
     * @return the usernames of the users that it failed to create
     */
    public Set<String> getFailedUsers()
    {
        return failedUsers;
    }

    /**
     * @return the usernames of the users that it failed to create because they already existed.
     */
    public Set<String> getExistingUsers()
    {
        return existingUsers;
    }
}

