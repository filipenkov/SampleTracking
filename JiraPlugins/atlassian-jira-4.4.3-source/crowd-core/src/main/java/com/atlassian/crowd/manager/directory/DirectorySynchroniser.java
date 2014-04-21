package com.atlassian.crowd.manager.directory;

import com.atlassian.crowd.directory.SynchronisableDirectory;
import com.atlassian.crowd.exception.*;

/**
 * An object that synchronises a {@link SynchronisableDirectory} with a cache.
 */
public interface DirectorySynchroniser
{
    /**
     * Executes a synchronisation in the current thread.  If the directory is not active, then the method will return
     * silently.
     *
     * @param synchronisableDirectory directory to synchronise.
     * @param mode synchronisation mode.
     */
    void synchronise(SynchronisableDirectory synchronisableDirectory, SynchronisationMode mode)
            throws DirectoryNotFoundException, OperationFailedException;

    /**
     * Returns whether the directory is currently synchronising.
     *
     * @param directoryId ID of the directory
     * @return true if the directory is current synchronising, otherwise false.
     * @throws DirectoryNotFoundException if the directory could not be found
     */
    boolean isSynchronising(long directoryId) throws DirectoryNotFoundException;
}
