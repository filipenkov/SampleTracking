package com.atlassian.gadgets.opensocial.spi;

/**
 * Thrown if there is a problem while performing an operation in the AppDataService
 *
 *  @since 2.0
 */
public class AppDataServiceException extends RuntimeException
{
    public AppDataServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AppDataServiceException(String message)
    {
        super(message);
    }

    public AppDataServiceException(Throwable cause)
    {
        super(cause);
    }
}