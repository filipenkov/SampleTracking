package com.atlassian.crowd.embedded.spi;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.search.query.entity.EntityQuery;

import java.util.List;

/**
 * Stores and retrieves directories. Directories are used for identifying a collection of users, groups and memberships.
 */
public interface DirectoryDao
{
    /**
     * Returns the directory with the specified ID, throwing {@link DirectoryNotFoundException} if it cannot be found.
     *
     * @param directoryId the ID of the directory to find
     * @return the directory with the specified ID
     * @throws DirectoryNotFoundException if there is no directory with the specified ID
     */
    Directory findById(long directoryId) throws DirectoryNotFoundException;

    /**
     * Returns the directory with the specified name, throwing {@link DirectoryNotFoundException} if it cannot be found.
     *
     * @param name the name of the directory to find
     * @return the directory with the specified name
     * @throws DirectoryNotFoundException if there is no directory with the specified name
     */
    Directory findByName(String name) throws DirectoryNotFoundException;

    /**
     * Returns the list of all directories in the data store, or empty list if there are no directories.
     */
    List<Directory> findAll();

    /**
     * Store a new directory in the data store.
     *
     * @param directory the directory to persist
     * @return the newly-persisted directory, which should be used for subsequent operations
     */
    Directory add(Directory directory);

    /**
     * Persists any changes made to the provided directory.
     *
     * @param directory the directory which has changes to persist
     * @return the updated directory after it has been persisted, which should be used for subsequent operations
     * @throws DirectoryNotFoundException if the directory is not found in the data store
     */
    Directory update(Directory directory) throws DirectoryNotFoundException;

    /**
     * Removes the specified directory from the data store.
     *
     * @param directory the directory to remove
     * @throws DirectoryNotFoundException if the directory does not exist
     */
    void remove(Directory directory) throws DirectoryNotFoundException;

    /**
     * Search for directories matching the specified query.
     *
     * @param entityQuery the search query to run against the directory data store
     * @return a list of directories matching the query
     * @see com.atlassian.crowd.search.builder.QueryBuilder#queryFor
     */
    List<Directory> search(EntityQuery<Directory> entityQuery);
}