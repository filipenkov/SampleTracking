package com.atlassian.gadgets.dashboard.spi;

/**
 * Thrown when there is a problem while performing an operation on the persistent data store. 
 * 
 * @since 2.0
 */
public class DashboardStateStoreException extends RuntimeException
{
    public DashboardStateStoreException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DashboardStateStoreException(String message)
    {
        super(message);
    }

    public DashboardStateStoreException(Throwable cause)
    {
        super(cause);
    }
}
