package com.atlassian.crowd.manager.directory.monitor.poller;

import com.atlassian.crowd.directory.monitor.poller.DirectoryPoller;
import com.atlassian.crowd.manager.directory.SynchronisationMode;
import com.atlassian.crowd.manager.directory.monitor.DirectoryMonitorRegistrationException;
import com.atlassian.crowd.manager.directory.monitor.DirectoryMonitorUnregistrationException;

/**
 * Manager for adding and removing DirectoryPollers.
 *
 * The pollers are scheduled via the Quartz/Spring
 * scheduling subsystem.
 *
 * Implementations are used in a thread-safe manner by
 * the DirectoryMonitorManager.
 */
public interface DirectoryPollerManager
{
    /**
     * Adds a configured DirectoryPoller to the scheduler.
     *
     * @param poller configured DirectoryPoller.
     * @throws DirectoryMonitorRegistrationException error registering poller with scheduler.
     */
    void addPoller(DirectoryPoller poller) throws DirectoryMonitorRegistrationException;

    /**
     * Checks whether a DirectoryPoller is scheduled to
     * poll a given directory.
     *
     * @param directoryID directory ID.
     * @return <code>true</code> if and only if a poller has been scheduled.
     */
    boolean hasPoller(long directoryID);

    /**
     * Manually triggers the directory poller to immediately poll the directory and synchronise changes.
     *
     * This can be used to asynchronously start a directory synchronisation using the
     * underlying scheduler.
     *
     * If a manual poll is triggered while another poll is running, the manual poll will
     * not queue (i.e. it will do nothing). You can check if a directory is currently synchronising
     * by calling the {@link com.atlassian.crowd.manager.directory.DirectoryManager#isSynchronising(long)}.
     *
     * If the directory does not exist or has no poller associated with it, this method
     * will do nothing. You can check if a directory has a poller associated with it by calling
     * {@link #hasPoller(long)}.
     *
     * @param directoryID directory ID.
     * @param synchronisationMode determines whether the poll only requests elements that have changed since a timestamp or if
     * it queries for the entire user base from the remote directory and determines changes locally.
     */
    void triggerPoll(long directoryID, SynchronisationMode synchronisationMode);

    /**
     * Removes the DirectoryPoller from the scheduler if
     * one exists for the given directory.
     *
     * @param directoryID directory ID.
     * @return <code>true</code> if and only if a poller existed
     * and was removed from polling the given directory.
     *
     * @throws DirectoryMonitorUnregistrationException If an error occurs during remove.
     */
    boolean removePoller(long directoryID) throws DirectoryMonitorUnregistrationException;

    /**
     * Attempts to unregister all registered pollers.
     *
     * Skips over any directories that fail unregistration.
     */
    void removeAllPollers();
}
