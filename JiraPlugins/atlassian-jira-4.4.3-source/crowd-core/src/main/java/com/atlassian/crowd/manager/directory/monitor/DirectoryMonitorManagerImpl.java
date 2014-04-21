package com.atlassian.crowd.manager.directory.monitor;

import com.atlassian.crowd.directory.DbCachingDirectoryPoller;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.directory.SynchronisableDirectory;
import com.atlassian.crowd.directory.monitor.poller.DirectoryPoller;
import com.atlassian.crowd.directory.monitor.DirectoryMonitorCreationException;
import com.atlassian.crowd.event.DirectoryEvent;
import com.atlassian.crowd.event.directory.DirectoryDeletedEvent;
import com.atlassian.crowd.event.directory.DirectoryUpdatedEvent;
import com.atlassian.crowd.event.migration.XMLRestoreFinishedEvent;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.manager.directory.DirectorySynchroniser;
import com.atlassian.crowd.manager.directory.monitor.poller.DirectoryPollerManager;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import org.apache.log4j.Logger;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DirectoryMonitorManagerImpl implements DirectoryMonitorManager
{
    private final Logger logger = Logger.getLogger(this.getClass());

    // polling/listening monitor managers
    private final DirectoryPollerManager directoryPollerManager;
    private final DirectorySynchroniser directorySynchroniser;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public DirectoryMonitorManagerImpl(DirectoryPollerManager directoryPollerManager,
                                       DirectorySynchroniser directorySynchroniser,
                                       EventPublisher eventPublisher)
    {
        this.directoryPollerManager = directoryPollerManager;
        this.directorySynchroniser = directorySynchroniser;
        eventPublisher.register(this);
    }

    public void addMonitor(RemoteDirectory remoteDirectory) throws DirectoryInstantiationException, DirectoryMonitorCreationException, DirectoryMonitorRegistrationException, DirectoryMonitorAlreadyRegisteredException
    {
        logger.info("Attempting to add monitor for directory with id: " + remoteDirectory.getDirectoryId());

        if (hasMonitor(remoteDirectory.getDirectoryId()))
        {
            // disallow registering more than one monitor
            throw new DirectoryMonitorAlreadyRegisteredException("Remove existing monitor to add a monitor");
        }

        readWriteLock.writeLock().lock();

        try
        {
            if (remoteDirectory instanceof SynchronisableDirectory)
            {
                // register poller
                logger.info("Directory is synchronisable. Adding poller.");
                DirectoryPoller poller = new DbCachingDirectoryPoller(directorySynchroniser, (SynchronisableDirectory) remoteDirectory);
                directoryPollerManager.addPoller(poller);
            }
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }

    public boolean removeMonitor(long directoryID) throws DirectoryMonitorUnregistrationException
    {
        logger.info("Attempting to remove monitor for directory with id: " + directoryID);

        readWriteLock.writeLock().lock();

        try
        {
            if (directoryPollerManager.hasPoller(directoryID))
            {
                logger.info("Directory is registered with a poller, removing poller");
                return directoryPollerManager.removePoller(directoryID);
            }
            else
            {
                logger.info("Directory is not registered with a poller or listener");
                return false;
            }
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }

    public boolean hasMonitor(long directoryID)
    {
        readWriteLock.readLock().lock();

        try
        {
            return (directoryPollerManager.hasPoller(directoryID));
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
    }

    protected void removeAllMonitors()
    {
        readWriteLock.writeLock().lock();

        try
        {
            directoryPollerManager.removeAllPollers();
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }

    @com.atlassian.event.api.EventListener
    public void handleEvent(DirectoryUpdatedEvent event)
    {
        handleEvent((DirectoryEvent) event);
    }

    @com.atlassian.event.api.EventListener
    public void handleEvent(DirectoryDeletedEvent event)
    {
        handleEvent((DirectoryEvent) event);
    }

    private void handleEvent(DirectoryEvent event)
    {
        final long directoryID = event.getDirectory().getId();

        // whether we update or delete the directory, we remove the monitor
        // the monitor is restarted ONLY if we require access to the directory again
        // (via the DirectoryInstanceLoader)
        try
        {
            if (hasMonitor(directoryID))
            {
                removeMonitor(directoryID);
            }
        }
        catch (DirectoryMonitorUnregistrationException e)
        {
            logger.error("Unable to remove monitor for directory with ID: " + directoryID);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @EventListener
    public void handleEvent(XMLRestoreFinishedEvent event)
    {
        // xml restore finished, so remove any existing monitors
        removeAllMonitors();
    }
}
