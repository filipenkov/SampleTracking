package com.atlassian.modzdetector;

import java.io.File;
import java.io.InputStream;

/**
 * An abstraction of the logic that chooses how to acquire a resource's {@link InputStream} by name and resource type
 * prefix. This only needs to be implemented by a user of this library if reading a registry in a different way than was
 * expected when it was written. For example, loading a registry via a product's installer to check if there are any
 * modifications to files would need to load all resources off the filesystem as opposed to what a running web
 * application would do, i.e. load some things off the classpath.
 */
public interface StreamMapper
{

    /**
     * Gets the input stream for the resource registered with the given name and prefix. Caller should close
     * the stream when finished.
     *
     * @param prefix
     * @param resourceName
     * @return null on failure to map.
     */
    InputStream mapStream(String prefix, String resourceName);

    /**
     * Given a resource name returns a file path to this resource. The root of the path is defined by the implementation. 
     * @param resourceKey
     * @return
     */
    public String getResourcePath(String resourceKey);

	/**
	 * Given a file creates a resource key that would correspond to this file
	 * @param file
	 * @return
	 */
	String getResourceKey(File file);
}
