package com.atlassian.crowd.manager.directory.monitor;

/**
 * Thrown when a directory is already being monitored.
 */
public class DirectoryMonitorAlreadyRegisteredException extends Exception
{
    public DirectoryMonitorAlreadyRegisteredException()
    {
    }

    public DirectoryMonitorAlreadyRegisteredException(String s)
    {
        super(s);
    }

    public DirectoryMonitorAlreadyRegisteredException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public DirectoryMonitorAlreadyRegisteredException(Throwable throwable)
    {
        super(throwable);
    }
}
