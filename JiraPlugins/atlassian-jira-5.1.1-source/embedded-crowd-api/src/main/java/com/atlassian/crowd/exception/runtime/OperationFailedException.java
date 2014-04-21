package com.atlassian.crowd.exception.runtime;

/**
 * Thrown when an operation failed for some reason.
 *
 * @since v2.1
 */
public class OperationFailedException extends CrowdRuntimeException
{
    public OperationFailedException()
    {
        super();
    }

    public OperationFailedException(String message)
    {
        super(message);
    }

    public OperationFailedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public OperationFailedException(Throwable cause)
    {
        this(null, cause);
    }
}
