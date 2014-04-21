package com.atlassian.upm;

import com.atlassian.sal.api.net.ResponseException;

/**
 * Used if authorization is needed to access a resource, but appropriate credentials were not supplied.
 */
public class AccessDeniedException extends ResponseException
{
    public AccessDeniedException()
    {
    }

    /**
     * Create an exception that wraps an error message and an underlying {@code Throwable} cause
     *
     * @param s the error message
     * @param throwable the underlying cause of the service exception
     */
    public AccessDeniedException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    /**
     * Create an exception that wraps the error message
     *
     * @param s the error message
     */
    public AccessDeniedException(String s)
    {
        super(s);
    }

    /**
     * Create an exception that wraps an underlying {@code Throwable} cause
     *
     * @param throwable the underlying cause of the service exception
     */
    public AccessDeniedException(Throwable throwable)
    {
        super(throwable);
    }
}
