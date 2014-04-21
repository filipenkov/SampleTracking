package com.atlassian.applinks.spi.link;

/**
 * Thrown when a reciprocal action, such as the deletion or creation of a link, fails in a remote application
 *
 * @since 3.0
 */
public class ReciprocalActionException extends Exception
{

    public ReciprocalActionException(final String message)
    {
        super(message);
    }

    public ReciprocalActionException(final Throwable cause)
    {
        super(cause);
    }

    public ReciprocalActionException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
