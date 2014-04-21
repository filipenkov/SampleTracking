package com.atlassian.crowd.manager.directory;

/**
 * Synchronisation modes for synchronising a directory.
 */
public enum SynchronisationMode
{
    /**
     * Synchronise the changes since the last synchronisation.
     */
    INCREMENTAL,

    /**
     * Synchronise with the entire userbase from remote directory.
     */
    FULL
}
