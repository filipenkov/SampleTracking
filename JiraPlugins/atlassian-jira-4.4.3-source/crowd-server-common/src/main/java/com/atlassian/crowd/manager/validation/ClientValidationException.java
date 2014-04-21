package com.atlassian.crowd.manager.validation;

/**
 * Exception is thrown when a client validation fails.
 */
public class ClientValidationException extends Exception
{
    /**
     * Default constructor.
     */
    public ClientValidationException()
    {
    }

    /**
     * Default constructor.
     *
     * @param s the message.
     */
    public ClientValidationException(String s)
    {
        super(s);
    }

    /**
     * Default constructor.
     *
     * @param s         the message.
     * @param throwable the {@link Exception Exception}.
     */
    public ClientValidationException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    /**
     * Default constructor.
     *
     * @param throwable the {@link Exception Exception}.
     */
    public ClientValidationException(Throwable throwable)
    {
        super(throwable);
    }
}
