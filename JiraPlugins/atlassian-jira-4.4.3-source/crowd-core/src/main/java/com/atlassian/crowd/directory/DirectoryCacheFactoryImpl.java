package com.atlassian.crowd.directory;

import com.atlassian.crowd.directory.ldap.cache.DirectoryCache;
import com.atlassian.crowd.directory.ldap.cache.DirectoryCacheFactory;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.manager.directory.SynchronisationStatusManager;
import com.atlassian.event.api.EventPublisher;

/**
 * DirectoryCacheFactory that returns DbCachingRemoteDirectoryCache instance.
 */
public class DirectoryCacheFactoryImpl implements DirectoryCacheFactory
{
    private final DirectoryDao directoryDao;
    private final SynchronisationStatusManager synchronisationStatusManager;
    private final EventPublisher eventPublisher;

    public DirectoryCacheFactoryImpl(DirectoryDao directoryDao,
                                     SynchronisationStatusManager synchronisationStatusManager,
                                     EventPublisher eventPublisher)
    {
        this.directoryDao = directoryDao;
        this.synchronisationStatusManager = synchronisationStatusManager;
        this.eventPublisher = eventPublisher;
    }

    DirectoryCacheChangeOperations createDirectoryCacheChangeOperations(RemoteDirectory remoteDirectory, InternalRemoteDirectory internalDirectory)
    {
        return new DbCachingRemoteChangeOperations(directoryDao,
                remoteDirectory,
                internalDirectory,
                synchronisationStatusManager,
                eventPublisher);
    }
    
    public final DirectoryCache createDirectoryCache(RemoteDirectory remoteDirectory, InternalRemoteDirectory internalDirectory)
    {
        DirectoryCacheChangeOperations x = createDirectoryCacheChangeOperations(
                                                 remoteDirectory,
                                                 internalDirectory);
        
        return new DirectoryCacheImplUsingChangeOperations(x);
    }
}
