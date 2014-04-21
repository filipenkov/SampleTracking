package com.atlassian.crowd.exception;

/**
 * Thrown when the credentials have expired. This exception should only be thrown when the authentication is successful
 * using the old credentials.
 */
public class ExpiredCredentialException extends FailedAuthenticationException
{
    public ExpiredCredentialException()
    {
    }

    public ExpiredCredentialException(Throwable cause)
    {
        super(cause);
    }

    public ExpiredCredentialException(String message)
    {
        super(message);
    }

    public ExpiredCredentialException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
