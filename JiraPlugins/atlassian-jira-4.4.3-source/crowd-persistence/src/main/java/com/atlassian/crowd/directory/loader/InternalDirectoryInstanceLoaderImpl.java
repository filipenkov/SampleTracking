package com.atlassian.crowd.directory.loader;

import com.atlassian.crowd.directory.AbstractInternalDirectory;
import com.atlassian.crowd.directory.InternalRemoteDirectory;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Loads an implementation of a {@link com.atlassian.crowd.directory.RemoteDirectory} for use at runtime
 * by the Crowd security server.
 * <p/>
 * The {@link #getDirectory(com.atlassian.crowd.embedded.api.Directory)} method caches instances of RemoteDirectory as
 * allowed in the spec of the interface, but {@link #getRawDirectory(Long, String, java.util.Map)} returns a new instance
 * every time as required.
 * This class listens for update events on EventPublisher to know when to refresh the cache.
 * <p/>
 * This loads both {@link com.atlassian.crowd.directory.AbstractInternalDirectory} and
 * {@link com.atlassian.crowd.directory.DelegatedAuthenticationDirectory} instances.
 */
public class InternalDirectoryInstanceLoaderImpl extends CachingDirectoryInstanceLoader implements InternalDirectoryInstanceLoader
{
    private final static Logger logger = LoggerFactory.getLogger(InternalDirectoryInstanceLoaderImpl.class);

    private final InstanceFactory instanceFactory;

    public InternalDirectoryInstanceLoaderImpl(InstanceFactory instanceFactory, EventPublisher eventPublisher)
    {
        super(eventPublisher);
        this.instanceFactory = checkNotNull(instanceFactory);
    }

    public InternalRemoteDirectory getRawDirectory(Long id, String className, Map<String, String> directoryAttributes)
            throws DirectoryInstantiationException
    {
        return getNewDirectory(id, className, directoryAttributes);
    }

    public boolean canLoad(final String className)
    {
        try
        {
            Class clazz = Class.forName(className);
            return AbstractInternalDirectory.class.isAssignableFrom(clazz);
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }

    @Override
    public InternalRemoteDirectory getDirectory(final Directory directory) throws DirectoryInstantiationException
    {
        return (InternalRemoteDirectory) super.getDirectory(directory);
    }

    @Override
    protected RemoteDirectory getNewDirectory(final Directory directory) throws DirectoryInstantiationException
    {
        final Long id = directory.getId();
        final String className = directory.getImplementationClass();
        final Map<String, String> directoryAttributes = directory.getAttributes();
        return getNewDirectory(id, className, directoryAttributes);
    }

    private InternalRemoteDirectory getNewDirectory(Long id, String className, Map<String, String> directoryAttributes) throws DirectoryInstantiationException
    {
        return RemoteDirectoryInstanceFactoryUtil.newRemoteDirectory(InternalRemoteDirectory.class, instanceFactory, id, className, directoryAttributes);
    }
}
