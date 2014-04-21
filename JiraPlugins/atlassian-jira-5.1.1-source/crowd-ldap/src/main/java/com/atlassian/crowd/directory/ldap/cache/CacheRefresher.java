package com.atlassian.crowd.directory.ldap.cache;

import com.atlassian.crowd.exception.OperationFailedException;

/**
 * Represents a way of refreshing a local cache of an external directory.
 * <p>
 * This was introduced in order to enable Active Directory delta cache updates.
 *
 * @since v2.1
 */
public interface CacheRefresher
{

    /**
     * Will visit all Users and Groups in the external directory in order to do a Full refresh.
     *
     * @param directoryCache the DirectoryCache to update.
     * @throws com.atlassian.crowd.exception.OperationFailedException if there was an error processing the operation
     */
    void synchroniseAll(DirectoryCache directoryCache) throws OperationFailedException;

    /**
     * Attempts to synchronise changes since the last refresh as opposed to performing a full synchronisation.
     *
     * Returns true if changes since the last refresh were synchronised successfully.
     *
     * @param directoryCache the DirectoryCache to update.
     * @return true if changes were synchronised
     * @throws com.atlassian.crowd.exception.OperationFailedException if there was an error processing the operation
     */
    boolean synchroniseChanges(DirectoryCache directoryCache) throws OperationFailedException;
}
