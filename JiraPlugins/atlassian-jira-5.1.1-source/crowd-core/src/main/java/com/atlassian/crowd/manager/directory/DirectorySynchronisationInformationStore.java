package com.atlassian.crowd.manager.directory;

import com.atlassian.crowd.embedded.api.DirectorySynchronisationInformation;

/**
 * Storage for directory synchronisation information.
 */
public interface DirectorySynchronisationInformationStore
{
    /**
     * Returns directory synchronisation information for a directory with the given id.
     *
     * @param directoryId directory id
     * @return directory synchronisation information for a directory
     */
    DirectorySynchronisationInformation get(long directoryId);

    /**
     * Set directory synchronisation information for a directory with the given id.
     *
     * @param directoryId directory id
     * @param syncInfo directory synchronisation information
     */
    void set(long directoryId, DirectorySynchronisationInformation syncInfo);

    /**
     * Clears directory synchronisation information of a directory with the given id.
     *
     * @param directoryId directory id
     */
    void clear(long directoryId);

    /**
     * Clears directory synchronisation information of all directories.
     */
    void clear();
}
