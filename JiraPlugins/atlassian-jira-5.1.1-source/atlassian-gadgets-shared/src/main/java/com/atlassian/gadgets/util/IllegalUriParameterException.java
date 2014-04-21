package com.atlassian.gadgets.util;

/**
 * Exception thrown when a parameter found in the URI of a request is invalid.
 */
public class IllegalUriParameterException extends RuntimeException
{
    public IllegalUriParameterException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public IllegalUriParameterException(String message)
    {
        super(message);
    }
}
