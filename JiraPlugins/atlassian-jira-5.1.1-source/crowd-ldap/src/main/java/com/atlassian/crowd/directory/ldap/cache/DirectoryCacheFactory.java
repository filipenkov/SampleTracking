package com.atlassian.crowd.directory.ldap.cache;

import com.atlassian.crowd.directory.InternalRemoteDirectory;
import com.atlassian.crowd.directory.RemoteDirectory;

/**
 * Factory for creating DirectoryCache instances.
 */
public interface DirectoryCacheFactory
{
    /**
     * Creates a new DirectoryCache instance for the given remoteDirectory and
     * internalDirectory.
     *
     * @param remoteDirectory remote directory to use
     * @param internalDirectory internal directory to use
     * @return new DirectoryCache instance
     */
    DirectoryCache createDirectoryCache(RemoteDirectory remoteDirectory, InternalRemoteDirectory internalDirectory);
}
