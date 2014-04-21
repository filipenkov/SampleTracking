package com.atlassian.crowd.directory.loader;

import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.exception.DirectoryInstantiationException;

import java.util.Map;

/**
 * Loads an implementation of a {@link com.atlassian.crowd.directory.RemoteDirectory} for use at runtime
 * by the Crowd security server.
 */
public interface DirectoryInstanceLoader
{
    /**
     * Loads the {@link com.atlassian.crowd.directory.RemoteDirectory} implementation class for a specific (already saved)
     * {@link com.atlassian.crowd.embedded.api.Directory} configuration.
     * <p>
     * Implementations of this method may choose to cache the RemoteDirectory, so this must only be called for
     * Directories that have been successfully saved.
     *
     * @param directory The directory to load the {@link com.atlassian.crowd.directory.RemoteDirectory} implementation from.
     * @return The {@link com.atlassian.crowd.directory.RemoteDirectory} loaded implementation.
     * @throws com.atlassian.crowd.exception.DirectoryInstantiationException If the {@link com.atlassian.crowd.directory.RemoteDirectory} implementation could not be loaded.
     */
    RemoteDirectory getDirectory(Directory directory) throws DirectoryInstantiationException;

    /**
     * Loads a guaranteed <strong>un-cached</strong> directory implementation for a specific configuration.
     * 
     * @param id Directory ID
     * @param className class name of directory.
     * @param attributes the configuration attributes to pass to the RemoteDirectory
     *
     * @return The loaded RemoteDirectory implementation.
     *
     * @throws DirectoryInstantiationException if a RemoteDirectory implementation can not be loaded.
     */
    RemoteDirectory getRawDirectory(Long id, String className, Map<String, String> attributes) throws DirectoryInstantiationException;

    /**
     * @param className class name of directory.
     * @return <code>true</code> iff the directory can load the directory of the specified class.
     */
    boolean canLoad(String className);
}
