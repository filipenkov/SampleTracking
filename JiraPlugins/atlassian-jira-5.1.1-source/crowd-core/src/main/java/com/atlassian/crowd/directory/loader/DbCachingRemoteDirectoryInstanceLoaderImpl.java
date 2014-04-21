package com.atlassian.crowd.directory.loader;

import com.atlassian.crowd.directory.CachingDirectory;
import com.atlassian.crowd.directory.DbCachingRemoteDirectory;
import com.atlassian.crowd.directory.InternalRemoteDirectory;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.directory.ldap.cache.DirectoryCacheFactory;
import com.atlassian.crowd.directory.monitor.DirectoryMonitorCreationException;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.manager.directory.monitor.DirectoryMonitorAlreadyRegisteredException;
import com.atlassian.crowd.manager.directory.monitor.DirectoryMonitorManager;
import com.atlassian.crowd.manager.directory.monitor.DirectoryMonitorRegistrationException;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.event.api.EventPublisher;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loader that allows for proxying of a remote directory through a local cache/mirror. To enable proxying for a remote
 * directory you should not expose its loader directly to the Crowd service, but instead delegate through this loader.
 */
public class DbCachingRemoteDirectoryInstanceLoaderImpl extends CachingDirectoryInstanceLoader implements InternalHybridDirectoryInstanceLoader
{
    private static final Logger log = Logger.getLogger(DbCachingRemoteDirectoryInstanceLoaderImpl.class);

    private final InternalDirectoryInstanceLoader internalDirectoryInstanceLoader;
    private final DirectoryMonitorManager directoryMonitorManager;
    private final DirectoryInstanceLoader remoteDirectoryInstanceLoader;
    private final DirectoryCacheFactory directoryCacheFactory;

    /**
     * Spring-friendly constructor.
     *
     * @param remoteDirectoryInstanceLoader the remote directory instance loader.
     * @param internalDirectoryInstanceLoader the internal directory in which to do the caching
     * @param directoryMonitorManager system directory monitor manager
     * @param eventPublisher system event publisher
     */
    public DbCachingRemoteDirectoryInstanceLoaderImpl(DirectoryInstanceLoader remoteDirectoryInstanceLoader,
                                                      InternalDirectoryInstanceLoader internalDirectoryInstanceLoader,
                                                      DirectoryMonitorManager directoryMonitorManager,
                                                      DirectoryCacheFactory directoryCacheFactory,
                                                      EventPublisher eventPublisher)
    {
        super(eventPublisher);
        this.remoteDirectoryInstanceLoader = remoteDirectoryInstanceLoader;
        this.internalDirectoryInstanceLoader = internalDirectoryInstanceLoader;
        this.directoryMonitorManager = directoryMonitorManager;
        this.directoryCacheFactory = directoryCacheFactory;
    }

    /**
     * Pico-friendly constructor. Because Pico can not accept list arguments in its constructor it instead hard-codes
     * the two delegate loaders that JIRA needs. This constructor <i>must</i> have more arguments than the spring-friendly
     * constructor for Pico to find it.
     *
     * @param ldapDirectoryInstanceLoader the delegate LDAP directory loader
     * @param remoteCrowdDirectoryInstanceLoader the delegate remote Crowd directory loader
     * @param internalDirectoryInstanceLoader the internal directory in which to do the caching
     * @param directoryMonitorManager system directory monitor manager
     * @param eventPublisher system event publisher
     */
    public DbCachingRemoteDirectoryInstanceLoaderImpl(LDAPDirectoryInstanceLoader ldapDirectoryInstanceLoader,
            RemoteCrowdDirectoryInstanceLoader remoteCrowdDirectoryInstanceLoader, InternalDirectoryInstanceLoader internalDirectoryInstanceLoader,
            DirectoryMonitorManager directoryMonitorManager, DirectoryCacheFactory directoryCacheFactory, EventPublisher eventPublisher)
    {
        this(makeDelegatingInstanceLoader(Arrays.asList(ldapDirectoryInstanceLoader, remoteCrowdDirectoryInstanceLoader)), internalDirectoryInstanceLoader, directoryMonitorManager, directoryCacheFactory, eventPublisher);
    }

    private static DirectoryInstanceLoader makeDelegatingInstanceLoader(List<DirectoryInstanceLoader> delegateLoaders)
    {
        DirectoryInstanceLoader directoryInstanceLoader = new DelegatingDirectoryInstanceLoader(delegateLoaders);
        return directoryInstanceLoader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RemoteDirectory getNewDirectory(final Directory directory) throws DirectoryInstantiationException
    {
        // make sure we get a brand new instance of the underlying directories (otherwise there may be a race condition on who gets the DirectoryUpdatedEvent first)
        final RemoteDirectory remoteDirectory = getRawDirectory(directory.getId(), directory.getImplementationClass(), directory.getAttributes());
        InternalRemoteDirectory internalDirectory = getRawInternalDirectory(directory);
        RemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        try
        {
            if (directory.isActive() && !directoryMonitorManager.hasMonitor(directory.getId()))
            {
                directoryMonitorManager.addMonitor(dbCachingRemoteDirectory);
            }
        }
        catch (DirectoryMonitorAlreadyRegisteredException e)
        {
            // don't care
        }
        catch (DirectoryMonitorCreationException e)
        {
            log.error("Could not add a monitor for the directory with id: " + directory.getId(), e);
        }
        catch (DirectoryMonitorRegistrationException e)
        {
            log.error("Could not add a monitor for the directory with id: " + directory.getId(), e);
        }

        return dbCachingRemoteDirectory;
    }

    private InternalRemoteDirectory getRawInternalDirectory(Directory directory) throws DirectoryInstantiationException
    {
        DirectoryImpl internal = new DirectoryImpl(directory);

        // internal directory needs a password encoder (even if it's just to store blank passwords)
        final Map<String, String> newAttributes = new HashMap<String, String>(internal.getAttributes());
        newAttributes.put("user_encryption_method", PasswordEncoderFactory.ATLASSIAN_SECURITY_ENCODER);
        internal.setAttributes(newAttributes);

        return internalDirectoryInstanceLoader.getRawDirectory(directory.getId(), CachingDirectory.class.getName(), newAttributes);
    }

    /**
     * This method will NOT wire up the internal backed directory.
     * <p/>
     * So no local groups or custom attributes.
     *
     * @param id Directory ID
     * @param className class name of directory.
     * @param attributes the configuration attributes to pass to the RemoteDirectory
     * @return directory without monitoring/caching and without backing internal directory.
     * @throws DirectoryInstantiationException
     */
    public RemoteDirectory getRawDirectory(Long id, String className, Map<String, String> attributes) throws DirectoryInstantiationException
    {
        return remoteDirectoryInstanceLoader.getRawDirectory(id, className, attributes);
    }

    public boolean canLoad(String className)
    {
        return remoteDirectoryInstanceLoader.canLoad(className);
    }
}
