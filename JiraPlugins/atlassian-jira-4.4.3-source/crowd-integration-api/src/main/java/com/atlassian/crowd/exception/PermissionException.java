package com.atlassian.crowd.exception;

/**
 * Permission Exception this Exception will handle Exceptions to do with CRUD operations
 * on Applications, Directories etc.
 */
public abstract class PermissionException extends Exception
{
    /**
     * Default constructor.
     */
    public PermissionException()
    {
    }

    /**
     * Default constructor.
     *
     * @param s the message.
     */
    public PermissionException(String s)
    {
        super(s);
    }

    /**
     * Default constructor.
     *
     * @param s         the message.
     * @param throwable the {@link Exception Exception}.
     */
    public PermissionException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    /**
     * Default constructor.
     *
     * @param throwable the {@link Exception Exception}.
     */
    public PermissionException(Throwable throwable)
    {
        super(throwable);
    }
}
