package com.atlassian.crowd.directory.loader;

import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.util.InstanceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Utility class to instantiate a RemoteDirectory from an {@link InstanceFactory}.
 *
 * @since v2.1
 */
class RemoteDirectoryInstanceFactoryUtil
{
    private static final Logger logger = LoggerFactory.getLogger(RemoteDirectoryInstanceFactoryUtil.class);

    private RemoteDirectoryInstanceFactoryUtil() {} // prevent instantiation

    /**
     * Creates a new instance of a RemoteDirectory using an {@link InstanceFactory}. The RemoteDirectory is cast to the
     * the specified class, <tt>clazz</tt>.
     *
     * @param clazz the RemoteDirectory class to cast to
     * @param instanceFactory InstanceFactory
     * @param directoryId directory ID
     * @param className Name of the <tt>RemoteDirectory</tt> implementation class
     * @param attributes attributes of the directory
     * @return new instance of a RemoteDirectory
     * @throws DirectoryInstantiationException if the RemoteDirectory could not be instantiated
     */
    public static <T extends RemoteDirectory> T newRemoteDirectory(Class<T> clazz, InstanceFactory instanceFactory, Long directoryId, String className, Map<String, String> attributes) throws DirectoryInstantiationException
    {
        try
        {
            final T remoteDirectory = clazz.cast(instanceFactory.getInstance(className));

            // set the configuration attributes
            if (directoryId != null) // id can be null if the directory hasn't been saved yet
            {
                remoteDirectory.setDirectoryId(directoryId);
            }
            remoteDirectory.setAttributes(attributes);

            return remoteDirectory;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new DirectoryInstantiationException(e);
        }
    }

    /**
     * Creates a new instance of a RemoteDirectory using an {@link InstanceFactory}.
     *
     * @param instanceFactory InstanceFactory
     * @param directoryId directory ID
     * @param className Name of the <tt>RemoteDirectory</tt> implementation class
     * @param attributes attributes of the directory
     * @return new instance of a RemoteDirectory
     * @throws DirectoryInstantiationException if the RemoteDirectory could not be instantiated
     * @see #newRemoteDirectory(Class, com.atlassian.crowd.util.InstanceFactory, Long, String, java.util.Map)  
     */
    public static RemoteDirectory newRemoteDirectory(InstanceFactory instanceFactory, Long directoryId, String className, Map<String, String> attributes) throws DirectoryInstantiationException
    {
        return newRemoteDirectory(RemoteDirectory.class, instanceFactory, directoryId, className, attributes);
    }
}
