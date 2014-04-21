package com.atlassian.crowd.exception;

/**
 * Thrown to indicate that the token has expired and is not valid anymore.
 */
public class TokenExpiredException extends InvalidTokenException
{
    public TokenExpiredException()
    {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public TokenExpiredException(String msg)
    {
        super(msg);
    }

    /**
     * {@inheritDoc}
     */
    public TokenExpiredException(String msg, Throwable throwable)
    {
        super(msg, throwable);
    }

    /**
     * {@inheritDoc}
     */
    public TokenExpiredException(Throwable throwable)
    {
        super(throwable);
    }
}
