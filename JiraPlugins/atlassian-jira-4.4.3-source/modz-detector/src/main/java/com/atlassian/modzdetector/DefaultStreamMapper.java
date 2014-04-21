package com.atlassian.modzdetector;

import java.io.File;
import java.io.InputStream;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * A {@link StreamMapper} that respects the as-registered method of acquiring input streams, that is that classpath
 * registered resources are loaded from the {@link ResourceAccessor ResourceAccessor's} {@link ClassLoader} and
 * filesystem registered resources are furnished as FileInputStreams using the configured root.
 */
class DefaultStreamMapper implements StreamMapper
{

    private ResourceAccessor resourceAccessor;

    public DefaultStreamMapper(ResourceAccessor resourceAccessor)
    {
        this.resourceAccessor = resourceAccessor;
    }

    public InputStream mapStream(String prefix, String resourceName)
    {

        if (HashRegistry.PREFIX_CLASSPATH.equals(prefix))
        {
            return resourceAccessor.getResourceFromClasspath(resourceName);
        }
        else if (HashRegistry.PREFIX_FILESYSTEM.equals(prefix))
        {

            return resourceAccessor.getResourceByPath(resourceName);
        }

        return null;

    }

	public String getResourcePath(String resourceKey) {
        if (resourceKey.startsWith(HashRegistry.PREFIX_CLASSPATH)) 
        {
            return resourceKey.substring(HashRegistry.PREFIX_CLASSPATH.length());
        }
        else if (resourceKey.startsWith(HashRegistry.PREFIX_FILESYSTEM)) 
        {
            return resourceKey.substring(HashRegistry.PREFIX_FILESYSTEM.length());
        }
        else
        {
        	throw new IllegalArgumentException("Resource key '" + resourceKey + "' is illegal.");
        }
	}

	public String getResourceKey(File file) {
		// this implementation only deals with classes and resources on classpath and knows nothing about filesystem
        throw new NotImplementedException();
	}
}
