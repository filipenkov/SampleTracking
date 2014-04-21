package com.atlassian.crowd.event.monitor.poller;

import com.atlassian.crowd.directory.monitor.DirectoryMonitor;
import com.atlassian.crowd.event.monitor.MonitorEvent;

/**
 * Fired every time a poller finishes the polling process
 * for detecting changes.
 */
public class PollingFinishedEvent extends MonitorEvent
{
    public PollingFinishedEvent(DirectoryMonitor source, long directoryID)
    {
        super(source, directoryID);
    }
}
