package com.atlassian.crowd.directory;

import com.atlassian.crowd.directory.monitor.poller.DirectoryPoller;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.manager.directory.DirectorySynchroniser;
import com.atlassian.crowd.manager.directory.SynchronisationMode;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Directory poller for the {@link com.atlassian.crowd.directory.DbCachingRemoteDirectory}.
 */
public class DbCachingDirectoryPoller implements DirectoryPoller
{
    private static final Logger LOG = LoggerFactory.getLogger(DbCachingDirectoryPoller.class);
    private final DirectorySynchroniser directorySynchroniser;
    private final SynchronisableDirectory remoteDirectory;
    private final long pollingInterval;

    /**
     * Constructs a new DbCachingDirectoryPoller with the correct instance of RemoteDirectory.
     * This is safe because if the RemoteDirectory is re-created the {@link DirectoryPoller} is discarded and a new one is created.
     *
     * @param directorySynchroniser DirectorySynchroniser
     * @param remoteDirectory RemoteDirectory
     */
    public DbCachingDirectoryPoller(DirectorySynchroniser directorySynchroniser, SynchronisableDirectory remoteDirectory)
    {
        this.directorySynchroniser = directorySynchroniser;
        this.remoteDirectory = remoteDirectory;
        pollingInterval = getPollingInterval(remoteDirectory);
    }

    private static long getPollingInterval(RemoteDirectory remoteDirectory)
    {
        final String intervalStr = remoteDirectory.getValue(SynchronisableDirectoryProperties.CACHE_SYNCHRONISE_INTERVAL);
        return NumberUtils.toLong(intervalStr, DEFAULT_CACHE_SYNCHRONISE_INTERVAL);
    }

    public long getPollingInterval()
    {
        return pollingInterval;
    }

    public void pollChanges(SynchronisationMode syncMode)
    {
        try
        {
            directorySynchroniser.synchronise(remoteDirectory, syncMode);
        }
        catch (DirectoryNotFoundException ex)
        {
            LOG.error("Error occurred while refreshing the cache for directory [ " + getDirectoryID() + " ].", ex);
        }
        catch (OperationFailedException ex)
        {
            LOG.error("Error occurred while refreshing the cache for directory [ " + getDirectoryID() + " ].", ex);
        }
        // Need to catch unexpected exceptions (ie bugs) and log them here because we are running in a spawned thread.
        catch (RuntimeException ex)
        {
            LOG.error("Error occurred while refreshing the cache for directory [ " + getDirectoryID() + " ].", ex);
        }
    }

    public long getDirectoryID()
    {
        return remoteDirectory.getDirectoryId();
    }
}
