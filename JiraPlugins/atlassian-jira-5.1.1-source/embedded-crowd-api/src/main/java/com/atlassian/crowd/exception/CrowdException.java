package com.atlassian.crowd.exception;

/**
 * <code>CrowdException</code> is the superclass of Crowd-specific exceptions
 * that must be caught.
 * <p>
 * This allows consumers of Crowd services to catch all
 * checked exceptions with only one catch block.
 */
public abstract class CrowdException extends Exception
{
    public CrowdException()
    {
        super();
    }

    public CrowdException(String message)
    {
        super(message);
    }

    public CrowdException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public CrowdException(Throwable cause)
    {
        super(cause);
    }
}