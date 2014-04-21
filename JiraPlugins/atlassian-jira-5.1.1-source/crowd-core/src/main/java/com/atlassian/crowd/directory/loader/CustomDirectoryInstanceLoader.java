package com.atlassian.crowd.directory.loader;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * CustomDirectoryInstanceLoader loads a RemoteDirectory by using an {@link com.atlassian.crowd.util.InstanceFactory}
 * to create a RemoteDirectory.
 *
 * @since v2.1
 */
public class CustomDirectoryInstanceLoader extends CachingDirectoryInstanceLoader implements DirectoryInstanceLoader
{
    private static final Logger logger = LoggerFactory.getLogger(CustomDirectoryInstanceLoader.class);
    private final ConcurrentMap<String, Boolean> canLoadCache;
    private final InstanceFactory instanceFactory;

    public CustomDirectoryInstanceLoader(final InstanceFactory instanceFactory, final EventPublisher eventPublisher)
    {
        super(eventPublisher);
        this.instanceFactory = checkNotNull(instanceFactory);

        // store the result of attempting to load the specified RemoteDirectory class in a cache
        canLoadCache = new MapMaker().softKeys().makeComputingMap(new Function<String, Boolean>()
        {
            public Boolean apply(final String className)
            {
                Boolean canLoadRemoteDirectoryClass;
                try
                {
                    final Class<?> clazz = ClassLoaderUtils.loadClass(className, CustomDirectoryInstanceLoader.this.getClass().getClassLoader());
                    canLoadRemoteDirectoryClass = RemoteDirectory.class.isAssignableFrom(clazz);
                }
                catch (ClassNotFoundException e)
                {
                    logger.warn("Could not load class: {}", className);
                    canLoadRemoteDirectoryClass = Boolean.FALSE;
                }
                return canLoadRemoteDirectoryClass;
            }
        });
    }

    @Override
    protected RemoteDirectory getNewDirectory(final Directory directory) throws DirectoryInstantiationException
    {
        return getRawDirectory(directory.getId(), directory.getImplementationClass(), directory.getAttributes());
    }

    public RemoteDirectory getRawDirectory(final Long id, final String className, final Map<String, String> attributes)
            throws DirectoryInstantiationException
    {
        return RemoteDirectoryInstanceFactoryUtil.newRemoteDirectory(instanceFactory, id, className, attributes);
    }

    public boolean canLoad(final String className)
    {
        return canLoadCache.get(className); // can do this only because we are using MapMaker
    }
}
