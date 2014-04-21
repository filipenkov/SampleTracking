package com.atlassian.crowd.manager.login.exception;

/**
 * Thrown when an invalid reset password token is provided.
 */
public class InvalidResetPasswordTokenException extends Exception
{
    /**
     * Default constructor.
     */
    public InvalidResetPasswordTokenException()
    {
    }

    /**
     * Default constructor.
     *
     * @param s The message.
     */
    public InvalidResetPasswordTokenException(String s)
    {
        super(s);
    }

    /**
     * Default constructor.
     *
     * @param s         The message.
     * @param throwable the {@link Exception Exception}.
     */
    public InvalidResetPasswordTokenException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    /**
     * Default constructor.
     *
     * @param throwable the {@link Exception Exception}.
     */
    public InvalidResetPasswordTokenException(Throwable throwable)
    {
        super(throwable);
    }
}

