package com.atlassian.crowd.embedded.api;

import com.atlassian.crowd.exception.DirectoryCurrentlySynchronisingException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;

import java.util.List;
/**
 * Provides the interface for performing Directory Operations in Crowd for applications embedding Crowd.
 * Null parameters for methods may throw {@link NullPointerException} or {@link IllegalArgumentException}.
 */
public interface CrowdDirectoryService
{
    /**
     * Will add a new {@link com.atlassian.crowd.embedded.api.Directory} into the local database.
     * @param directory The directory to be saved
     * @return the persisted {@link com.atlassian.crowd.embedded.api.Directory}
     * @throws OperationFailedException if the operation failed for any reason
     */
    Directory addDirectory(Directory directory) throws OperationFailedException;

    /**
     * Test if a connection to the directory server can be established.
     *
     * @param directory Directory to test
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    public void testConnection(Directory directory) throws OperationFailedException;

    /**
     * Will return a {@code List<Directory>} ordered by the order specified by the application.
     * @return a {@code List<Directory>} or if there are no directories, an empty list (this should never happen)
     */
    List<Directory> findAllDirectories();

    /**
     * Will return a {@link Directory} based on the given {@code directoryId}
     * @param directoryId the id of the directory
     * @return the directory or {@code null} if the directory is not found
     */
    Directory findDirectoryById(long directoryId);

    /**
     * Will update the {@link com.atlassian.crowd.embedded.api.Directory}. The Directory must have at the bare minimum an {@link Directory#getId()}, {@link Directory#getName()}, {@link Directory#getType()} and {@link Directory#getImplementationClass()}}
     * @param directory the directory to update
     * @return the updated directory
     * @throws OperationFailedException if the directory does not exist or the operation failed for any other reason.
     */
    Directory updateDirectory(Directory directory) throws OperationFailedException;

    /**
     * Will place the directory with the given {@code directoryId} and the passed in {@code position} in the current list of directories configured for an application.
     * @param directoryId the id of the directory
     * @param position the position in the list of directories where you would like this one to be
     * @throws OperationFailedException if the directory does not exist or the operation failed for any other reason
     */
    void setDirectoryPosition(long directoryId, int position) throws OperationFailedException;

    /**
     * Will remove a directory from the Application, this will also remove all associated entities (users/groups/memberships). TODO: We should discuss this
     * 
     * @param directoryId the directory id of the {@link Directory} to remove.
     * @return {@code true} if the directory and associated entities were removed, or {@code false} otherwise
     * @throws DirectoryCurrentlySynchronisingException if the Directory is currently synchronising.
     * @throws OperationFailedException if the directory does not exist or the operation failed for any other reason
     */
    boolean removeDirectory(long directoryId) throws DirectoryCurrentlySynchronisingException, OperationFailedException;

    /**
     * Returns true if the underlying directory implementation supports nested groups.
     *
     * @param directoryId ID of directory.
     * @return true if the directory supports nested groups
     * @throws OperationFailedException if the operation failed for any reason
     */
    boolean supportsNestedGroups(long directoryId) throws OperationFailedException;

    /**
     * Returns true if the underlying directory implementation supports manual synchronisation of the directory's local cache.
     *
     * @param directoryId ID of directory.
     * @return true if the directory supports synchronisation
     * @throws OperationFailedException if the operation failed for any reason
     */
    boolean isDirectorySynchronisable(long directoryId) throws OperationFailedException;

    /**
     * Requests that this directory should update its cache by synchronising with the remote server.
     * The synchronisation will occur asynchronously, i.e. this method returns immediately and the
     * synchronization continues in the background.
     * <p>
     * If a synchronisation is currently in progress when this method is called, then this method does nothing.
     *
     * @param directoryId ID of directory.
     * @throws OperationFailedException if the operation failed for any reason
     */
    void synchroniseDirectory(long directoryId) throws OperationFailedException;

    /**
     * Requests that this directory should update its cache by synchronising with the remote server.
     * <p>
     * If a synchronisation is currently in progress when this method is called,
     * then this method does nothing if runInBackGround is true, otherwise it will throw OperationFailedException.
     *
     * @param directoryId ID of directory.
     * @param runInBackground If True the synchronise will happen asynchronously.
     * @throws OperationFailedException if the operation failed for any reason
     */
    void synchroniseDirectory(long directoryId, boolean runInBackground) throws OperationFailedException;

    /**
     * Returns true if the given Directory is currently synchronising.
     * @param directoryId ID of directory.
     * @return true if the given Directory is currently synchronising.
     * @throws OperationFailedException if the operation failed for any reason
     */
    boolean isDirectorySynchronising(long directoryId) throws OperationFailedException;

    /**
     * Returns the synchronisation information for the directory. This includes the last sync start time and duration, and the current sync start time
     * (if directory is currently synchronising).
     *
     * @param directoryId ID of directory
     * @return a DirectorySynchronisationInformation object that contains the synchronisation information for the directory.
     *      null if the RemoteDirectory is not an instance of SynchronisableDirectory
     * @throws OperationFailedException if the operation failed for any reason
     */
    DirectorySynchronisationInformation getDirectorySynchronisationInformation(long directoryId) throws OperationFailedException;

    /**
     * Stores the provided LDAP connection pool properties so they can be applied when the system next restarts.
     * @param poolProperties the LDAP connection pool properties to be stored and applied on the next restart
     * @see #getStoredConnectionPoolProperties()
     * @see #getSystemConnectionPoolProperties()
     */
    void setConnectionPoolProperties(ConnectionPoolProperties poolProperties);

    /**
     * Retrieves the stored LDAP connection pool properties which will be applied when the system restarts.
     * @return LdapPoolProperties the stored connection pool settings
     * @see #getSystemConnectionPoolProperties() to retrieve the currently active settings
     */
    ConnectionPoolProperties getStoredConnectionPoolProperties();

    /**
     * Retrieves the system LDAP connection pool properties (i.e. the currently active settings).
     * @return LdapPoolProperties the system connection pool settings
     * @see #getStoredConnectionPoolProperties() to retrieve the stored configuration which will be applied
     * when the system next restarts
     */
    ConnectionPoolProperties getSystemConnectionPoolProperties();

}
