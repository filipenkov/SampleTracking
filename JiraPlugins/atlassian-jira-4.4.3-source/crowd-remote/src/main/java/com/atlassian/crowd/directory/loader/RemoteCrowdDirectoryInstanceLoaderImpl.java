package com.atlassian.crowd.directory.loader;

import com.atlassian.crowd.directory.*;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;
import org.slf4j.*;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Loads an implementation of a {@link com.atlassian.crowd.directory.RemoteDirectory} for use at runtime
 * by the Crowd security server.
 * <p>
 * The {@link #getDirectory(com.atlassian.crowd.embedded.api.Directory)} method caches instances of RemoteDirectory as
 * allowed in the spec of the interface, but {@link #getRawDirectory(Long, String, java.util.Map)} returns a new instance
 * every time as required.
 */
public class RemoteCrowdDirectoryInstanceLoaderImpl extends CachingDirectoryInstanceLoader implements RemoteCrowdDirectoryInstanceLoader
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final InstanceFactory instanceFactory;

    public RemoteCrowdDirectoryInstanceLoaderImpl(InstanceFactory instanceFactory, EventPublisher eventPublisher)
    {
        super(eventPublisher);
        this.instanceFactory = checkNotNull(instanceFactory);
    }

    public RemoteCrowdDirectory getRawDirectory(final Long id, final String className, final Map<String, String> directoryAttributes)
            throws DirectoryInstantiationException
    {
        return getNewDirectory(id, className, directoryAttributes);
    }

    public boolean canLoad(final String className)
    {
        try
        {
            Class clazz = Class.forName(className);
            return RemoteCrowdDirectory.class.isAssignableFrom(clazz);
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }

    private RemoteCrowdDirectory getNewDirectory(Long id, String className, Map<String, String> directoryAttributes) throws DirectoryInstantiationException
    {
        return RemoteDirectoryInstanceFactoryUtil.newRemoteDirectory(RemoteCrowdDirectory.class, instanceFactory, id, className, directoryAttributes);
    }

    @Override
    protected RemoteDirectory getNewDirectory(final Directory directory) throws DirectoryInstantiationException
    {
        return getNewDirectory(directory.getId(), directory.getImplementationClass(), directory.getAttributes());
    }
}