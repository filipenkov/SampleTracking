package com.atlassian.upm.token;

/**
 * A {@code RuntimeException} thrown when an error occurs working with tokens
 */
public class TokenException extends RuntimeException
{
    public TokenException(String message)
    {
        super(message);
    }

    public TokenException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
