package com.atlassian.crowd.exception;

/**
 * Thrown when the operation is not permitted.
 */
public class OperationNotPermittedException extends CrowdException
{
    public OperationNotPermittedException()
    {
    }

    public OperationNotPermittedException(String message)
    {
        super(message);
    }

    public OperationNotPermittedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public OperationNotPermittedException(Throwable cause)
    {
        super(cause);
    }
}
