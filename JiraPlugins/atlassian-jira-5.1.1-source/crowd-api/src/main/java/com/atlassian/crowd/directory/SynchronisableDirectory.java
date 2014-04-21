package com.atlassian.crowd.directory;

import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.manager.directory.SynchronisationMode;
import com.atlassian.crowd.manager.directory.SynchronisationStatusManager;

/**
 * A {@link RemoteDirectory} that holds a local cache to remote data.
 * The methods on this interface offer methods related to synchronising the remote data.
 *
 * @since v2.1
 */
public interface SynchronisableDirectory extends RemoteDirectory
{
    /**
     * Requests that this directory should update its cache by synchronising with the remote data.
     *
     * Implementations of this method should publish a RemoteDirectorySynchronisedEvent after completing the synchronisation.
     *
     * @param mode synchronisation mode
     * @param synchronisationStatusManager listener for synchronisation status change notifications
     * @throws com.atlassian.crowd.exception.OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void synchroniseCache(SynchronisationMode mode, SynchronisationStatusManager synchronisationStatusManager) throws OperationFailedException;
}
