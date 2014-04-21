package com.atlassian.crowd.manager.directory;

import com.atlassian.crowd.directory.SynchronisableDirectory;
import com.atlassian.crowd.exception.DirectoryNotFoundException;

/**
 * Helper class for performing a synchronisation on the {@link SynchronisableDirectory}'s cache.  Implementations of
 * this interface are responsible for ensuring that database operations are wrapped in a transaction where applicable.
 */
public interface DirectorySynchroniserHelper
{
    /**
     * Updates a {@link SynchronisableDirectory}'s current synchronisation start time.
     *
     * @param synchronisableDirectory directory to update
     * @throws DirectoryNotFoundException if the directory could not be found
     */
    void updateSyncStartTime(SynchronisableDirectory synchronisableDirectory) throws DirectoryNotFoundException;

    /**
     * Updates information relevant to a directory's current synchronisation end time.  The duration, last sync start time
     * and current sync start time will be updated.
     * <p/>
     * {@link #updateSyncStartTime(com.atlassian.crowd.directory.SynchronisableDirectory)} must have been invoked
     * before this method call.
     *
     * @param synchronisableDirectory directory to update
     * @throws IllegalStateException if the directory is not currently synchronising.
     * @throws DirectoryNotFoundException If the directory could not be found
     */
    void updateSyncEndTime(SynchronisableDirectory synchronisableDirectory) throws DirectoryNotFoundException;

    /**
     * Returns true if the given directory is currently being synchronised.
     *
     * @param synchronisableDirectoryId synchronisable directory id
     * @return true if the given directory is currently being synchronised
     * @throws DirectoryNotFoundException if the directory could not be found
     */
    boolean isSynchronising(final long synchronisableDirectoryId) throws DirectoryNotFoundException;
}
