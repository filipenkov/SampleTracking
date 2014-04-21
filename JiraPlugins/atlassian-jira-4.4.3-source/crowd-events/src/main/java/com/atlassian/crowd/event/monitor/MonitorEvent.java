package com.atlassian.crowd.event.monitor;

import com.atlassian.crowd.directory.monitor.DirectoryMonitor;
import com.atlassian.crowd.event.Event;

/**
 * A notification event pertaining to a monitor's
 * execution. It is not directly relevant remote
 * directory mutations.
 */
public abstract class MonitorEvent extends Event
{
    private final long directoryID;

    protected MonitorEvent(DirectoryMonitor source, long directoryID)
    {
        super(source);
        this.directoryID = directoryID;
    }

    public long getDirectoryID()
    {
        return directoryID;
    }
}
