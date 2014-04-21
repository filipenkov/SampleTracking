package com.atlassian.crowd.directory.loader;

import com.atlassian.crowd.directory.InternalRemoteDirectory;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.exception.DirectoryInstantiationException;

import java.util.Map;

/**
 * An extension of {@link DirectoryInstanceLoader} specialised for Internal Directories.
 * <p>
 * Note that the {@link #getDirectory(com.atlassian.crowd.embedded.api.Directory)} and {@link #getRawDirectory(Long, String, java.util.Map)}
 * methods have been specialised to return instances of {@link InternalRemoteDirectory}.
 */
public interface InternalDirectoryInstanceLoader extends DirectoryInstanceLoader
{
    /**
     * @see com.atlassian.crowd.directory.loader.DirectoryInstanceLoader
     * @return instance of {@link InternalRemoteDirectory}.
     */
    InternalRemoteDirectory getDirectory(Directory directory) throws DirectoryInstantiationException;

    /**
     * @see com.atlassian.crowd.directory.loader.DirectoryInstanceLoader
     * @return instance of {@link InternalRemoteDirectory}.
     */
    InternalRemoteDirectory getRawDirectory(Long id, String className, Map<String, String> attributes) throws DirectoryInstantiationException;
}
