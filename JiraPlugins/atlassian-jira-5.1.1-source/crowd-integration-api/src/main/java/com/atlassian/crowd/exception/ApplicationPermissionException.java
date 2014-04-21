package com.atlassian.crowd.exception;

/**
 * Thrown to indicate that an Application does not have the required permission to perform the operation.
 */
public class ApplicationPermissionException extends PermissionException
{
    /**
     * Default constructor.
     */
    public ApplicationPermissionException()
    {
    }

    /**
     * Default constructor.
     *
     * @param s the message.
     */
    public ApplicationPermissionException(String s)
    {
        super(s);
    }

    /**
     * Default constructor.
     *
     * @param s         the message.
     * @param throwable the {@link Exception Exception}.
     */
    public ApplicationPermissionException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    /**
     * Default constructor.
     *
     * @param throwable the {@link Exception Exception}.
     */
    public ApplicationPermissionException(Throwable throwable)
    {
        super(throwable);
    }
}
