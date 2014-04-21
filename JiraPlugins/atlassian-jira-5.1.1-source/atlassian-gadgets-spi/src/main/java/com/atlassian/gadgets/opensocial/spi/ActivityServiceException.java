package com.atlassian.gadgets.opensocial.spi;

/**
 * Thrown if there is a problem while performing an operation in the ActivityService
 *
 *  @since 2.0
 */
public class ActivityServiceException extends RuntimeException
{
    public ActivityServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ActivityServiceException(String message)
    {
        super(message);
    }

    public ActivityServiceException(Throwable cause)
    {
        super(cause);
    }
}
