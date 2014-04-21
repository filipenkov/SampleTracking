package com.atlassian.crowd.exception;

/**
 * Represents an error when executing an operation on the remote directory failed for some reason. E.g. network error,
 * LDAP errors.
 */
public class OperationFailedException extends CrowdException
{
    public OperationFailedException()
    {
        super();
    }

    public OperationFailedException(final Throwable cause)
    {
        super(cause);
    }

    public OperationFailedException(final String message)
    {
        super(message);
    }

    public OperationFailedException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
