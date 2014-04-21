package com.atlassian.crowd.exception;

/**
 * Checked exception thrown if the requested operation is not supported.
 *
 * @since 2.2
 */
public class OperationNotSupportedException extends OperationFailedException
{
    public OperationNotSupportedException()
    {
        super();
    }

    public OperationNotSupportedException(final Throwable cause)
    {
        super(cause);
    }

    public OperationNotSupportedException(final String message)
    {
        super(message);
    }

    public OperationNotSupportedException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
