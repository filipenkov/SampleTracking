package com.atlassian.crowd.exception;

/**
 * Thrown when a Crowd client is not communicating with a valid Crowd service.
 *
 * @since v2.1
 */
public class InvalidCrowdServiceException extends OperationFailedException
{
    public InvalidCrowdServiceException(final String message)
    {
        super(message);
    }

    public InvalidCrowdServiceException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
