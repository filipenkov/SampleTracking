package com.atlassian.crowd.manager.directory;

import com.atlassian.crowd.directory.SynchronisableDirectory;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.event.directory.RemoteDirectorySynchronisedEvent;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.manager.lock.DirectoryLockManager;
import com.atlassian.event.api.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;

/**
 * An implementation of a {@link DirectorySynchroniser}.
 */
public class DirectorySynchroniserImpl implements DirectorySynchroniser
{
    private final static Logger log = LoggerFactory.getLogger(DirectorySynchroniser.class);
    private final DirectoryLockManager directoryLockManager;
    private final DirectoryDao directoryDao;
    private final DirectorySynchroniserHelper directorySynchroniserHelper;
    private final SynchronisationStatusManager synchronisationStatusManager;
    private final EventPublisher eventPublisher;

    public DirectorySynchroniserImpl(DirectoryLockManager directoryLockManager,
            DirectoryDao directoryDao,
            DirectorySynchroniserHelper directorySynchroniserHelper,
            SynchronisationStatusManager synchronisationStatusManager, EventPublisher eventPublisher)
    {
        this.directoryLockManager = directoryLockManager;
        this.directoryDao = directoryDao;
        this.directorySynchroniserHelper = directorySynchroniserHelper;
        this.synchronisationStatusManager = synchronisationStatusManager;
        this.eventPublisher = eventPublisher;
    }

    public void synchronise(final SynchronisableDirectory remoteDirectory, final SynchronisationMode mode)
            throws DirectoryNotFoundException, OperationFailedException
    {
        long directoryId = remoteDirectory.getDirectoryId();
        final Directory directory = findDirectoryById(directoryId);
        if (!directory.isActive())
        {
            log.debug("Request to synchronise directory [ {} ] is returning silently because the directory is not active.", directoryId);
            return;
        }

        log.debug("request to synchronise directory [ {} ]", directoryId);

        final Lock lock = directoryLockManager.getLock(directoryId);

        if (lock.tryLock())
        {
            try
            {
                directorySynchroniserHelper.updateSyncStartTime(remoteDirectory);
                synchronisationStatusManager.syncStarted(directory);
                try
                {
                    remoteDirectory.synchroniseCache(mode, synchronisationStatusManager);
                    synchronisationStatusManager.syncStatus(directoryId, "directory.caching.sync.completed.ok");
                }
                catch (OperationFailedException e)
                {
                    synchronisationStatusManager.syncStatus(directoryId, "directory.caching.sync.completed.error");
                    throw e;
                }
                finally
                {
                    directorySynchroniserHelper.updateSyncEndTime(remoteDirectory);
                    synchronisationStatusManager.syncFinished(directoryId);
                }
            }

            finally
            {
                lock.unlock();
            }
            eventPublisher.publish(new RemoteDirectorySynchronisedEvent(this, remoteDirectory));
        }
        else
        {
            log.debug("directory [ {} ] already synchronising", directoryId);
        }
    }

    public boolean isSynchronising(long directoryId) throws DirectoryNotFoundException
    {
        return directorySynchroniserHelper.isSynchronising(directoryId);
    }

    private Directory findDirectoryById(final long directoryId) throws DirectoryNotFoundException
    {
        return directoryDao.findById(directoryId);
    }
}
