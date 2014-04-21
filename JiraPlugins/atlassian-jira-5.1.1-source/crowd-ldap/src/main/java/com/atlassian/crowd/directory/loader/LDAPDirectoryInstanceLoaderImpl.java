package com.atlassian.crowd.directory.loader;

import com.atlassian.crowd.directory.LDAPDirectory;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.*;
import org.slf4j.*;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Loads an implementation of a {@link com.atlassian.crowd.directory.RemoteDirectory} for use at runtime
 * by the Crowd security server.
 */
public class LDAPDirectoryInstanceLoaderImpl extends CachingDirectoryInstanceLoader implements LDAPDirectoryInstanceLoader
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final InstanceFactory instanceFactory;

    public LDAPDirectoryInstanceLoaderImpl(final InstanceFactory instanceFactory, final EventPublisher eventPublisher)
    {
        super(eventPublisher);
        this.instanceFactory = checkNotNull(instanceFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RemoteDirectory getNewDirectory(Directory directory) throws DirectoryInstantiationException
    {
        return getRawDirectory(directory.getId(), directory.getImplementationClass(), directory.getAttributes());
    }

    public RemoteDirectory getRawDirectory(Long id, String className, Map<String, String> attributes) throws DirectoryInstantiationException
    {
        return RemoteDirectoryInstanceFactoryUtil.newRemoteDirectory(instanceFactory, id, className, attributes);
    }

    public boolean canLoad(final String className)
    {
        try
        {
            return LDAPDirectory.class.isAssignableFrom(Class.forName(className));
        }
        catch (ClassNotFoundException e)
        {
            logger.error("Could not load class <" + className + ">", e);
            return false;
        }
    }
}
