package com.atlassian.crowd.manager.directory;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectorySynchronisationInformation;

import java.io.Serializable;

public interface SynchronisationStatusManager
{
    /**
     * Notify that directory synchronisation has started.
     *
     * @param directory directory
     */
    void syncStarted(Directory directory);

    /**
     * Notify that directory synchronisation status has changed.
     *
     * @param directoryId directory id
     * @param key synchronisation status message key
     * @param parameters synchronisation status message parameters
     * @throws IllegalStateException if the directory is not currently synchronising
     */
    void syncStatus(long directoryId, String key, Serializable... parameters);

    /**
     * Notify that directory synchronisation has finished.
     *
     * @param directoryId directory id
     */
    void syncFinished(long directoryId);

    /**
     * Returns directory synchronisation information. The returned value is never be null.
     *
     * @param directory directory to retrieve information from
     * @return directory synchronisation information
     */
    DirectorySynchronisationInformation getDirectorySynchronisationInformation(Directory directory);
}
