package com.atlassian.crowd.event.monitor.poller;

import com.atlassian.crowd.directory.monitor.DirectoryMonitor;
import com.atlassian.crowd.event.monitor.MonitorEvent;

/**
 * Fired every time a poller starts the polling process.
 */
public class PollingStartedEvent extends MonitorEvent
{
    public PollingStartedEvent(DirectoryMonitor source, long directoryID)
    {
        super(source, directoryID);
    }
}
