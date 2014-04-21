package com.atlassian.crowd.directory.loader;

import com.atlassian.crowd.directory.*;
import com.atlassian.crowd.embedded.api.*;
import com.atlassian.crowd.event.directory.*;
import com.atlassian.crowd.event.migration.*;
import com.atlassian.crowd.exception.*;
import com.atlassian.event.api.*;
import com.atlassian.event.api.EventListener;
import com.atlassian.util.concurrent.*;
import org.slf4j.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * CacheableDirectoryInstanceLoader loads directories which have the ability to have caching enabled and disabled.
 *
 * @since v2.1
 */
public class CacheableDirectoryInstanceLoader implements DirectoryInstanceLoader
{
    private static final Logger log = LoggerFactory.getLogger(CacheableDirectoryInstanceLoader.class);

    private final InternalHybridDirectoryInstanceLoader delegate;
    private final EventPublisher eventPublisher;

    // only cache the remote directory instance when DirectoryProperties.CACHE_ENABLED is false
    private final ConcurrentMap<Long, RemoteDirectory> directoryCache;

    public CacheableDirectoryInstanceLoader(final InternalHybridDirectoryInstanceLoader delegate, final EventPublisher eventPublisher)
    {
        this.delegate = delegate;
        this.eventPublisher = eventPublisher;
        directoryCache = CopyOnWriteMap.<Long, RemoteDirectory>builder().newHashMap();
        this.eventPublisher.register(this);
    }

    public RemoteDirectory getDirectory(final Directory directory) throws DirectoryInstantiationException
    {
        boolean cacheEnabled = Boolean.parseBoolean(directory.getAttributes().get(DirectoryProperties.CACHE_ENABLED));
        if (cacheEnabled)
        {
            return delegate.getDirectory(directory);
        }
        else
        {
            // try to get the directory from the cache
            // We MUST only serve a single instance of DbCachingRemoteDirectory per directory ID because of concurrency controls on the DbCachingRemoteDirectory sync refresh.
            final Long id = directory.getId();
            RemoteDirectory remoteDirectory = directoryCache.get(id);
            if (remoteDirectory != null)
            {
                // Cache hit
                return remoteDirectory;
            }
            else
            {
                remoteDirectory = delegate.getRawDirectory(id, directory.getImplementationClass(), directory.getAttributes());
                // Put the new directory in the cache if another thread didn't just beat us.
                RemoteDirectory existingDirectory = directoryCache.putIfAbsent(id, remoteDirectory);
                return (existingDirectory != null ? existingDirectory : remoteDirectory);
            }
        }
    }

    public RemoteDirectory getRawDirectory(final Long id, final String className, final Map<String, String> attributes)
            throws DirectoryInstantiationException
    {
        return delegate.getRawDirectory(id, className, attributes);
    }

    public boolean canLoad(final String className)
    {
        return delegate.canLoad(className);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // EventListeners for refresh of directory cache
    // -----------------------------------------------------------------------------------------------------------------

    @EventListener
    public void handleEvent(DirectoryUpdatedEvent event)
    {
        // Get the new Directory
        Directory directory = event.getDirectory();
        // Remove the old directory instance from the cache
        directoryCache.remove(directory.getId());
    }

    @EventListener
    public void handleEvent(DirectoryDeletedEvent event)
    {
        // Get the new Directory
        Directory directory = event.getDirectory();
        // Remove the old directory instance from the cache
        directoryCache.remove(directory.getId());
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void handleEvent(XMLRestoreFinishedEvent event)
    {
        // Clear all instances from the cache.
        directoryCache.clear();
        log.debug("Directory Cache cleared.");
    }
}
