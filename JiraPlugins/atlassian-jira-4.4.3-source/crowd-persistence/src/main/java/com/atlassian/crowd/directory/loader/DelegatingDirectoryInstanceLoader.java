package com.atlassian.crowd.directory.loader;

import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import static com.google.common.base.Preconditions.checkNotNull;

import com.atlassian.util.concurrent.*;
import com.google.common.base.*;
import com.google.common.base.Function;
import com.google.common.collect.*;
import org.slf4j.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Iterative directory instance loader.
 */
public class DelegatingDirectoryInstanceLoader implements DirectoryInstanceLoader
{
    private static final Logger logger = LoggerFactory.getLogger(DelegatingDirectoryInstanceLoader.class);

    private final List<DirectoryInstanceLoader> directoryInstanceLoaders;
    private final ConcurrentMap<String, DirectoryInstanceLoader> classFactoryCache;

    /**
     * Creates a new instance of DelegatingDirectoryInstanceLoader. Spring-friendly.
     *
     * @param loaders list of delegate directory instance loaders.
     */
    public DelegatingDirectoryInstanceLoader(final List<DirectoryInstanceLoader> loaders)
    {
        directoryInstanceLoaders = ImmutableList.copyOf(checkNotNull(loaders));
        classFactoryCache = CopyOnWriteMap.<String, DirectoryInstanceLoader>builder().newHashMap();
    }

    /**
     * Pico-friendly constructor. Because Pico can not accept list arguments in its constructor it instead hard-codes
     * the two delegate loaders that JIRA needs. This constructor <i>must</i> have more arguments than the spring-friendly
     * constructor for Pico to find it.
     *
     * @param internalDirectoryInstanceLoader loads an internal directory instance
     * @param ldapInternalHybridDirectoryInstanceLoader loads a directory with caching capabilities (using an internal directory)
     * @param delegatedAuthenticationDirectoryInstanceLoader loads a DelegatedAuthenticationDirectory instance
     */
    public DelegatingDirectoryInstanceLoader(final InternalDirectoryInstanceLoader internalDirectoryInstanceLoader, final InternalHybridDirectoryInstanceLoader ldapInternalHybridDirectoryInstanceLoader, final DelegatedAuthenticationDirectoryInstanceLoader delegatedAuthenticationDirectoryInstanceLoader)
    {
        this(Arrays.asList(internalDirectoryInstanceLoader, ldapInternalHybridDirectoryInstanceLoader, delegatedAuthenticationDirectoryInstanceLoader));
    }

    /**
     * Pico-friendly constructor. Because Pico can not accept list arguments in its constructor it instead hard-codes
     * the two delegate loaders that JIRA needs. This constructor <i>must</i> have more arguments than the spring-friendly
     * constructor for Pico to find it.
     *
     * @param internalDirectoryInstanceLoader the internal directory in which to do the caching
     * @param ldapInternalHybridDirectoryInstanceLoader loads a directory with caching capabilities (using an internal directory)
     */
    public DelegatingDirectoryInstanceLoader(final InternalDirectoryInstanceLoader internalDirectoryInstanceLoader, final InternalHybridDirectoryInstanceLoader ldapInternalHybridDirectoryInstanceLoader)
    {
        this(Arrays.asList(internalDirectoryInstanceLoader, ldapInternalHybridDirectoryInstanceLoader));
    }

    public RemoteDirectory getDirectory(final Directory directory) throws DirectoryInstantiationException
    {
        final DirectoryInstanceLoader loader = getFactoryForClass(directory.getImplementationClass(), true);
        if (loader != null)
        {
            return loader.getDirectory(directory);
        }
        else
        {
            throw new DirectoryInstantiationException("Could not find a directory instance loader for directory <" + directory.getImplementationClass() + ">");
        }
    }

    public RemoteDirectory getRawDirectory(Long id, String className, Map<String, String> attributes) throws DirectoryInstantiationException
    {
        final DirectoryInstanceLoader loader = getFactoryForClass(className, true);
        if (loader != null)
        {
            return loader.getRawDirectory(id, className, attributes);
        }
        else
        {
            throw new DirectoryInstantiationException("Could not find a directory instance loader for directory <" + className + ">");
        }
    }

    public boolean canLoad(final String className)
    {
        return getFactoryForClass(className, false) != null;
    }

    private DirectoryInstanceLoader getFactoryForClass(String className, boolean logError)
    {
        if (className == null)
        {
            return null;
        }

        DirectoryInstanceLoader cachedLoader = classFactoryCache.get(className);
        if (cachedLoader != null)
        {
            return cachedLoader; // cache hit
        }

        for (DirectoryInstanceLoader loader : directoryInstanceLoaders)
        {
            if (loader.canLoad(className))
            {
                DirectoryInstanceLoader existingLoader = classFactoryCache.putIfAbsent(className, loader);
                return (existingLoader != null ? existingLoader : loader);
            }
        }

        if (logError)
        {
            logger.error("Could not find DirectoryInstanceLoader for {}", className);
        }
        return null;
    }
}
