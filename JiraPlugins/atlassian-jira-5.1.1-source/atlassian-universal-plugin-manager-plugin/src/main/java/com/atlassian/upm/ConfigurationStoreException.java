package com.atlassian.upm;

/**
 * A runtime exception that wraps errors encountered during access to stored configurations
 */
public class ConfigurationStoreException extends RuntimeException
{
    /**
     * Create an exception that wraps an underlying {@code Throwable} cause
     *
     * @param message the error message
     * @param cause the underlying cause of the service exception
     */
    public ConfigurationStoreException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Create an exception that wraps the error message
     *
     * @param message the error message
     */
    public ConfigurationStoreException(String message)
    {
        super(message);
    }
}
