package com.atlassian.modzdetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Provides a service to determine whether resource content has been modified compared to a registry.
 * The registry contains a map of names to hashes (implemented by the {@link HashAlgorithm}).
 * Resources are loaded using a strategy defined in the registry on a per-resource basis. The two main
 * strategies are filesystem loading and classpath loading. The {@link ResourceAccessor} implementation
 * provides the concrete mechanism for acquiring the {@link InputStream} in the case of filesystem loading and
 * the {@link ClassLoader} in the case of classpath loading.
 */
public class ModzDetector
{
    private static final Logger log = LoggerFactory.getLogger(ModzDetector.class);
    private ResourceAccessor resourceAccessor;
    private HashAlgorithm hashAlgorithm;
    private StreamMapper streamMapper;
    
    public static final FileFilter OPEN_FILTER = new FileFilter() {
        public boolean accept(File file)
        {
            return true;
        }
    };

    public ModzDetector(final ResourceAccessor resourceAccessor)
    {
        this(resourceAccessor, new MD5HashAlgorithm(), new DefaultStreamMapper(resourceAccessor));
    }

    public ModzDetector(ResourceAccessor resourceAccessor, HashAlgorithm hashAlgorithm)
    {
        this(resourceAccessor, hashAlgorithm, new DefaultStreamMapper(resourceAccessor));
    }

    public ModzDetector(ResourceAccessor resourceAccessor, HashAlgorithm hashAlgorithm, StreamMapper streamMapper)
    {
        this.resourceAccessor = resourceAccessor;
        this.hashAlgorithm = hashAlgorithm;
        this.streamMapper = streamMapper;
    }

    /**
     * Gets a report of what files were modified using the default registry file, loaded off the classpath.
     *
     * @return the modifications, let's call it a report.
     * @throws ModzRegistryException
     */
    public Modifications getModifiedFiles() throws ModzRegistryException
    {
        final InputStream registryStream = getDefaultRegistryStream();
        return getModifiedFiles(registryStream);
    }

    /**
     * Gets a report of what files were modified using the registry in the given registryStream.
     *
     * @param registryStream a properties file, additionally a properly formed registry.
     * @return the modifications.
     * @throws ModzRegistryException
     */
    public Modifications getModifiedFiles(final InputStream registryStream) throws ModzRegistryException
    {
        if (registryStream == null)
        {
            throw new ModzRegistryException("No registry provided.");
        }
        else
        {
            try
            {
                Properties registry = new Properties();
                registry.load(registryStream);
                final Modifications mods = new Modifications();
                long start = System.currentTimeMillis();
                checkRegistry(mods, registry);
                log.info("Time taken (ms) to check registry: " + (System.currentTimeMillis() - start));
                return mods;
            }
            catch (IOException e)
            {
                throw new ModzRegistryException("Unable to load hash registry: ", e);
            }
            finally
            {
                IOUtils.closeQuietly(registryStream);
            }
        }
    }

    /**
     * Gets a report of what files were added using the default registry file, loaded off the classpath. Note that only
     * files within a filesystem can in practice be traversed to find added files
     *
     * @return the modifications, let's call it a report.
     * @throws ModzRegistryException
     */
    public List<String> getAddedFiles(File rootDirectory) throws ModzRegistryException
    {
        final InputStream registryStream = getDefaultRegistryStream();
        return getAddedFiles(registryStream, rootDirectory, OPEN_FILTER);
    }

    private InputStream getDefaultRegistryStream()
    {
        if (log.isDebugEnabled())
        {
            log.debug("registry loading from resource " + HashRegistry.FILE_NAME_HASH_REGISTRY_PROPERTIES +
                    " using provider " + resourceAccessor.getClass().getName());
        }

        return resourceAccessor.getResourceFromClasspath(HashRegistry.FILE_NAME_HASH_REGISTRY_PROPERTIES);
    }

    /**
     * Searches the given root directory for all file paths beneath it that are not registered.
     *
     * @param registryStream the registry.
     * @param root           the root of the file path to search for additional resources.
     * @return the list of subpaths of root that are not registered in the given registry.
     * @throws ModzRegistryException if something untoward occurs.
     */
    public List<String> getAddedFiles(final InputStream registryStream, final File root, FileFilter filter) throws ModzRegistryException
    {
        if (!root.canRead() || !root.isDirectory())
        {
            throw new IllegalArgumentException("root is not a readable directory: " + root.getPath());
        }
        try
        {
            Properties registry = new Properties();
            registry.load(registryStream);
            return getAddedFiles(registry, root, filter);
        }
        catch (IOException e)
        {
            throw new ModzRegistryException("Unable to load hash registry: ", e);
        }
        finally
        {
            IOUtils.closeQuietly(registryStream);
        }
    }

    /**
     * Returns the list of relative paths under the given root given a list of absolute paths.
     *
     * @param root          the root for all paths to be relative to (must actually be a root of all absolute paths)
     * @param absolutePaths the list of absolute paths that all shre the common root as a root.
     * @return the list of relative paths that could be used to compose the absolute paths by prefixing with the root.
     */
    private List<String> getRelativePaths(File root, List<String> absolutePaths)
    {
        String rootPath = root.getAbsolutePath() + "/";
        int rootPathLength = rootPath.length();
        List<String> relativePaths = new ArrayList<String>(absolutePaths.size());
        for (String absolutePath : absolutePaths)
        {
            relativePaths.add(absolutePath.substring(rootPathLength));
        }
        return relativePaths;
    }

    List<String> getAddedFiles(Properties registry, File root, FileFilter filter)
    {
        List<String> addedFiles = new ArrayList<String>();
        File[] files = root.listFiles(filter);
        for (File file : files)
        {
            if (file.isDirectory())
            {
                addedFiles.addAll(getAddedFiles(registry, file, filter));
            }
            else
            {
            	String resourceKey = streamMapper.getResourceKey(file);
            	if (!registry.containsKey(resourceKey))
            	{
            		addedFiles.add(streamMapper.getResourcePath(resourceKey));
            	}
            }
        }
        // need to report these as relative to be consistent
        return addedFiles;
    }

    void checkRegistry(final Modifications mods, final Properties registry)
    {
        int failureCount = 0;
        for (Map.Entry prop : registry.entrySet())
        {
            final String propertyKey = (String) prop.getKey();
            try
            {
                ResourceType checkResult = checkResource(propertyKey, (String) prop.getValue());
                checkResult.handle(mods);
            }
            catch (CannotCheckResource cannotCheckResource)
            {
                failureCount++;
            }
        }
        if (failureCount > 0)
        {
            log.warn("Failed to check " + failureCount + " files.");
        }
    }

    abstract static class ResourceType
    {
        private String resourceName;

        protected ResourceType(final String resourceName)
        {
            this.resourceName = resourceName;
        }

        static ResourceType createModified(final String resourcePath)
        {
            return new ResourceType(resourcePath)
            {
                void handle(final Modifications mods)
                {
                    mods.modifiedFiles.add(resourcePath);
                }
            };
        }

        static ResourceType createRemoved(final String resourcePath)
        {
            return new ResourceType(resourcePath)
            {
                void handle(final Modifications mods)
                {
                    mods.removedFiles.add(resourcePath);
                }
            };
        }

        static ResourceType createUnchanged(final String resourcePath)
        {
            return new ResourceType(resourcePath)
            {
                void handle(final Modifications mods)
                {
                }
            };
        }

        abstract void handle(Modifications mods);
    }

    ResourceType checkResource(final String propertyKey, final String resourceName, final String hash, final InputStream resource)
            throws CannotCheckResource
    {
        try
        {
            if (resourceName == null)
            {
                throw new IllegalArgumentException("resourceName cannot be null");
            }
            if (hash == null)
            {
                throw new CannotCheckResource("Expected hash is null");
            }
            if (resource == null)
            {
                return ResourceType.createRemoved(streamMapper.getResourcePath(propertyKey));
            }

            final String actualHash = hashAlgorithm.getHash(resource);
            if (hash.equals(actualHash))
            {
                return ResourceType.createUnchanged(streamMapper.getResourcePath(propertyKey));
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("detected modified resource '" + resourceName + "' expected hash " + hash + " got " + actualHash);
                }
                return ResourceType.createModified(streamMapper.getResourcePath(propertyKey));
            }
        }
        finally
        {
            IOUtils.closeQuietly(resource);
        }
    }

    /**
     * Performs the modified-or-removed check on the resource indicated by the given key by comparing the
     * equality of the computed hash with the given hash.
     *
     * @param propertyKey the registry key of the resource.
     * @param hash        the registered hash of the resource contents.
     * @return the ResourceType representing the result of the check.
     * @throws CannotCheckResource in the event of a registry integrity or IO failure.
     */
    ResourceType checkResource(final String propertyKey, final String hash) throws CannotCheckResource
    {
        final String prefix = propertyKey.substring(0, HashRegistry.PREFIX_CLASSPATH.length());
        final String resourceName = propertyKey.substring(prefix.length(), propertyKey.length());

        if (hash == null)
        {
            throw new CannotCheckResource("unable to interpret registered file with key: " + propertyKey);
        }
        InputStream resource = streamMapper.mapStream(prefix, resourceName);
        if (resource == null)
        {
            return ResourceType.createRemoved(streamMapper.getResourcePath(propertyKey));
        }
        return checkResource(propertyKey, resourceName, hash, resource);
    }

}
