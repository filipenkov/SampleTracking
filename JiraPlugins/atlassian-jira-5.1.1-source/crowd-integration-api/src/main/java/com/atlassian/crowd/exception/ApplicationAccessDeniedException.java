package com.atlassian.crowd.exception;

/**
 * Thrown to indicate that a user does not have access to authenticate against an application.
 */
public class ApplicationAccessDeniedException extends Exception
{
    /**
     * {@inheritDoc}
     */
    public ApplicationAccessDeniedException()
    {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public ApplicationAccessDeniedException(String s)
    {
        super(s);
    }

    /**
     * {@inheritDoc}
     */
    public ApplicationAccessDeniedException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    /**
     * {@inheritDoc}
     */
    public ApplicationAccessDeniedException(Throwable throwable)
    {
        super(throwable);
    }
}
