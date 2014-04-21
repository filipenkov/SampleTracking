package com.atlassian.crowd.event.monitor;

import com.atlassian.crowd.directory.monitor.DirectoryMonitor;

/**
 * Indicates an error has occured while attempting to
 * monitor a particular directory.
 *
 * This generally implies the monitor is in an unusable
 * state and needs to be restarted (removed and recreated).
 */
public class MonitorErrorEvent extends MonitorEvent
{
    private final Exception exception;

    public MonitorErrorEvent(DirectoryMonitor source, long directoryID, Exception e)
    {
        super(source, directoryID);
        this.exception = e;
    }

    public Exception getException()
    {
        return exception;
    }
}
