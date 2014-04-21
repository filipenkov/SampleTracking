package com.atlassian.crowd.event.remote;

public class RemoteDirectoryMonitorErrorEvent extends RemoteDirectoryEvent
{
    private final Exception exception;
    
    public RemoteDirectoryMonitorErrorEvent(Object source, long directoryID, Exception e)
    {
        super(source, directoryID);
        this.exception = e;
    }

    public Exception getException()
    {
        return exception;
    }
}
