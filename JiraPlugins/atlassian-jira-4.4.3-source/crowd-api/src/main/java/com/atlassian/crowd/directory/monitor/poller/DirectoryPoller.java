package com.atlassian.crowd.directory.monitor.poller;

import com.atlassian.crowd.directory.monitor.DirectoryMonitor;
import com.atlassian.crowd.manager.directory.SynchronisationMode;

/**
 * Allows polling for remote directory mutations.
 */
public interface DirectoryPoller extends DirectoryMonitor
{
    /**
     * Default cache synchronisation interval in seconds.
     */
    public static final int DEFAULT_CACHE_SYNCHRONISE_INTERVAL = 3600;

    /**
     * Polls the directory for mutations and synchronises accordingly.
     * @param syncMode determines whether the poll only requests elements that have changed since a timestamp or if
     * it queries for the entire user base from the remote directory and determines changes locally.
     */
    void pollChanges(SynchronisationMode syncMode);

    /**
     * @return polling interval in seconds
     */
    long getPollingInterval();
}
