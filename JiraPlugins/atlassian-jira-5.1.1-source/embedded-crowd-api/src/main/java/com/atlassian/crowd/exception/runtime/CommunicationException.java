package com.atlassian.crowd.exception.runtime;

/**
 * A specific extension of the Runtime OperationFailedException that is thrown when the host application is unable to
 * communicate with the remote User Directory.
 */
public class CommunicationException extends OperationFailedException
{
    public CommunicationException(final Throwable cause)
    {
        super(cause);
    }
}
