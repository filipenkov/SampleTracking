package com.atlassian.crowd.directory.monitor;

/**
 * Allows monitoring remote directory mutations.
 */
public interface DirectoryMonitor
{
    /**
     * Returns the ID of the directory that
     * is being polled.
     *
     * @return directory ID.
     */
    long getDirectoryID();
}
