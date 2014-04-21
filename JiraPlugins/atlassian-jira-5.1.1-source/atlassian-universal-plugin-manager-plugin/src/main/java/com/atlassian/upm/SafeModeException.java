package com.atlassian.upm;

/**
 * Used for exceptions encountered during safe mode.
 */
public class SafeModeException extends RuntimeException
{
    /**
     * Create an exception that wraps the error message
     *
     * @param message the error message
     */
    public SafeModeException(String message)
    {
        super(message);
    }

    /**
     * Create an exception that wraps an underlying {@code Throwable} cause
     *
     * @param message the error message
     * @param cause the underlying cause of the exception
     */
    public SafeModeException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
