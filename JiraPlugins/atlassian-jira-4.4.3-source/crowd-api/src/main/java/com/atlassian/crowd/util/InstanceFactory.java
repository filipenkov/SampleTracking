package com.atlassian.crowd.util;

/**
 * An interface to easily instantiate objects from classes.
 */
public interface InstanceFactory
{
    /**
     * Get an instance of a class.
     *
     * @param className name of class.
     * @return instance of class.
     * @throws ClassNotFoundException if the class does not exist.
     */
    Object getInstance(String className) throws ClassNotFoundException;

    /**
     * Get an instance of a class from a specific classloader.
     *
     * @param className name of class.
     * @param classLoader class loader.
     * @return instance of class.
     * @throws ClassNotFoundException if the class does not exist.
     */
    Object getInstance(String className, ClassLoader classLoader) throws ClassNotFoundException;

    /**
     * Gets an instance of a class.
     *
     * @param clazz class.
     * @param <T> type of class.
     * @return instance of class.
     */
    <T> T getInstance(Class<T> clazz);
}
