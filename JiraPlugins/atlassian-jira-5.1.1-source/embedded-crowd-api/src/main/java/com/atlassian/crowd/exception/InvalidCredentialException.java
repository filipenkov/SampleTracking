package com.atlassian.crowd.exception;

/**
 * Thrown when the supplied credential is not valid.
 */
public class InvalidCredentialException extends CrowdException
{
    public InvalidCredentialException()
    {
    }

    public InvalidCredentialException(String message)
    {
        super(message);
    }

    public InvalidCredentialException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Default constructor.
     *
     * @param throwable the {@link Exception Exception}.
     */
    public InvalidCredentialException(Throwable throwable)
    {
        super(throwable);
    }
}