package com.atlassian.crowd.exception;

/**
 * Thrown to indicate that the token does not exist in the server.
 */
public class TokenNotFoundException extends InvalidTokenException
{
    public TokenNotFoundException()
    {
    }

    public TokenNotFoundException(String s)
    {
        super(s);
    }

    public TokenNotFoundException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public TokenNotFoundException(Throwable throwable)
    {
        super(throwable);
    }
}
