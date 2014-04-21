package com.atlassian.modzdetector;

import java.io.InputStream;

/**
 * Allows the detector to find the actual items for comparison, either
 * via the filesystem or the classloader.
 */
public interface ResourceAccessor
{
    /**
     * Gets an InputStream registered by a filesystem path. Caller to close.
     *
     * @param resourceName the name of the resource to load.
     * @return the stream.
     */
    public InputStream getResourceByPath(String resourceName);

    /**
     * Gets an InputStream from the classpath. The implementer can choose
     * how this is implemented (without exposing a Classloader instance to the caller).
     * Caller to close.
     *
     * @param resourceName the name of the resource to load.
     * @return the stream.
     */
    public InputStream getResourceFromClasspath(String resourceName);
}
