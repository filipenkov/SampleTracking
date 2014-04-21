package com.atlassian.crowd.directory.loader;

import com.atlassian.crowd.directory.*;
import com.atlassian.crowd.embedded.api.*;
import com.atlassian.crowd.event.directory.*;
import com.atlassian.crowd.event.migration.*;
import com.atlassian.crowd.exception.*;
import com.atlassian.event.api.*;
import com.atlassian.util.concurrent.*;

import java.util.concurrent.*;

/**
 * Implements a DirectoryInstanceLoader. Provides caching for RemoteDirectory instances.
 *
 * @since v2.1
 */
public abstract class CachingDirectoryInstanceLoader implements DirectoryInstanceLoader
{
    private final EventPublisher eventPublisher;
    private final ConcurrentMap<Long, RemoteDirectory> directoryCache;

    public CachingDirectoryInstanceLoader(final EventPublisher eventPublisher)
    {
        directoryCache = CopyOnWriteMap.<Long, RemoteDirectory>builder().newHashMap();
        this.eventPublisher = eventPublisher;
        this.eventPublisher.register(this);
    }

    public RemoteDirectory getDirectory(final Directory directory) throws DirectoryInstantiationException
    {
        Long directoryId = directory.getId();

        RemoteDirectory remoteDirectory = directoryCache.get(directoryId);
        if (remoteDirectory != null)
        {
            return remoteDirectory;
        }
        else
        {
            remoteDirectory = getNewDirectory(directory);
            RemoteDirectory existingDirectory = directoryCache.putIfAbsent(directoryId, remoteDirectory);
            return (existingDirectory != null ? existingDirectory : remoteDirectory);
        }
    }

    protected EventPublisher getEventPublisher()
    {
        return eventPublisher;
    }

    /**
     * Always returns a new instance of a RemoteDirectory.
     *
     * @param directory directory configuration
     * @return new instance of a RemoteDirectory
     * @throws DirectoryInstantiationException if the directory could not be instantiated.
     */
    protected abstract RemoteDirectory getNewDirectory(final Directory directory) throws DirectoryInstantiationException;

    // -----------------------------------------------------------------------------------------------------------------
    // EventListeners for refresh of directory cache
    // -----------------------------------------------------------------------------------------------------------------

    @com.atlassian.event.api.EventListener
    public void handleEvent(DirectoryUpdatedEvent event)
    {
        // Get the new Directory
        Directory directory = event.getDirectory();
        // Remove the old directory instance from the cache
        directoryCache.remove(directory.getId());
    }

    @com.atlassian.event.api.EventListener
    public void handleEvent(DirectoryDeletedEvent event)
    {
        // Get the new Directory
        Directory directory = event.getDirectory();
        // Remove the old directory instance from the cache
        directoryCache.remove(directory.getId());
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @com.atlassian.event.api.EventListener
    public void handleEvent(XMLRestoreFinishedEvent event)
    {
        // Clear all instances from the cache.
        directoryCache.clear();
    }
}
