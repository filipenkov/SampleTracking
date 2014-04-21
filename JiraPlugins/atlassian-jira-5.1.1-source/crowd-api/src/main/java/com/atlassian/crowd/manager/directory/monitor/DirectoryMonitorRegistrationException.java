package com.atlassian.crowd.manager.directory.monitor;

/**
 * Error registering directory monitor.
 */
public class DirectoryMonitorRegistrationException extends Exception
{
    public DirectoryMonitorRegistrationException()
    {
        super();
    }

    public DirectoryMonitorRegistrationException(String s)
    {
        super(s);
    }

    public DirectoryMonitorRegistrationException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public DirectoryMonitorRegistrationException(Throwable throwable)
    {
        super(throwable);
    }
}
