package com.atlassian.crowd.directory.monitor;

/**
 * Signifies an error during the creation of a DirectoryMonitor instance.
 */
public class DirectoryMonitorCreationException extends Exception
{
    public DirectoryMonitorCreationException()
    {
    }

    public DirectoryMonitorCreationException(String s)
    {
        super(s);
    }

    public DirectoryMonitorCreationException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public DirectoryMonitorCreationException(Throwable throwable)
    {
        super(throwable);
    }
}
