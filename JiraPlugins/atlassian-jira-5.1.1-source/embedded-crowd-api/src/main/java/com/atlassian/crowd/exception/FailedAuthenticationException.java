package com.atlassian.crowd.exception;

public class FailedAuthenticationException extends CrowdException
{
    public FailedAuthenticationException()
    {
    }

    public FailedAuthenticationException(String message)
    {
        super(message);
    }

    public FailedAuthenticationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public FailedAuthenticationException(Throwable cause)
    {
        super(cause);
    }
}
