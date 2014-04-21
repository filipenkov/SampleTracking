package com.atlassian.crowd.manager.directory.monitor;

import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.directory.monitor.DirectoryMonitorCreationException;
import com.atlassian.crowd.exception.DirectoryInstantiationException;

/**
 * Manages directory monitoring by allowing adding and removal of DirectoryMonitors.
 *
 * Once a monitor has been added via this manager, it will monitor the directory either via listening to remote mutation
 * events or via polling the remote directory periodically.
 *
 * One directory can have at most one monitor.
 *
 * Implementations are required to be thread-safe.
 */
public interface DirectoryMonitorManager
{
    /**
     * Adds a monitor to a particular directory.
     *
     * @param directory directory to monitor.
     * @throws com.atlassian.crowd.exception.DirectoryInstantiationException if the raw remote directory cannot be instantiated.
     * @throws com.atlassian.crowd.directory.monitor.DirectoryMonitorCreationException if there was an error creating a monitor for the directory.
     * @throws DirectoryMonitorRegistrationException if there was an error registering the monitor.
     * @throws DirectoryMonitorAlreadyRegisteredException hasMonitor(directoryID) returns true.
     */
    void addMonitor(RemoteDirectory directory) throws DirectoryInstantiationException, DirectoryMonitorCreationException, DirectoryMonitorRegistrationException, DirectoryMonitorAlreadyRegisteredException;

    /**
     * Removes a monitor from a monitored directory.
     *
     * @param directoryID directory ID.
     * @return <code>true</code> if a monitor was removed.
     * @throws DirectoryMonitorUnregistrationException if there was an error removing the monitor.
     */
    boolean removeMonitor(long directoryID) throws DirectoryMonitorUnregistrationException;

    /**
     * Determines whether a directory is currently being monitored.
     *
     * @param directoryID directory ID.
     * @return <code>true</code> if an only if the directory is being monitored.
     */
    boolean hasMonitor(long directoryID);
}
