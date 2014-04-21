package com.atlassian.plugins.rest.common.expand;

public class ExpandException extends RuntimeException
{
    public ExpandException(Throwable cause)
    {
        super(cause);
    }

    public ExpandException(String message)
    {
        super(message);
    }
}
