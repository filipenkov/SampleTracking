/*
 * Copyright (c) 2006 Atlassian Software Systems. All Rights Reserved.
 */
package com.atlassian.crowd.manager.cache;

import java.io.Serializable;
import java.util.List;

public interface CacheManager
{
    // TODO - all put for permanent attribute into session
    // TODO - change over property manager to put into session perm

    /**
     * Saves object to the cache.
     *
     * @param cacheName cache name
     * @param key key of the object
     * @param obj actual object to be cached
     *
     * @throws CacheManagerException if any problems arise during the operation
     */
    void put(String cacheName, Serializable key, Serializable obj) throws CacheManagerException;

    /**
     * Retrieves cached object.
     *
     * @param cacheName cache name
     * @param key key of the object
     * @return cached object
     * @throws CacheManagerException if any problems arise during the operation
     * @throws NotInCacheException if the key does not exist
     */
    Object get(String cacheName, Serializable key) throws CacheManagerException, NotInCacheException;

    /**
     * Removes cached object from the cache.
     *
     * @param cacheName cache name
     * @param key key of the object
     * @return if an element was removed as a result of this call
     * @throws CacheManagerException if any problems arise during the operation
     */
    boolean remove(String cacheName, Serializable key) throws CacheManagerException;

    /**
     * Removes all the entries from the given cache.
     *
     * @param cacheName cache name.
     * @throws CacheManagerException if any problems arise during the operation
     */
    void removeAll(String cacheName) throws CacheManagerException;

    /**
     * Retrieve all keys from the cache.
     *
     * @param cacheName cache name.
     * @return list of keys.
     * @throws CacheManagerException
     */
    List getAllKeys(String cacheName) throws CacheManagerException;

    /**
     * Saves object to the default cache
     *
     * @param key key of the object
     * @param obj actual object to be cached
     *
     * @throws CacheManagerException if any problems arise during the operation
     */
    void put(String key, Serializable obj) throws CacheManagerException;

    /**
     * Retrieves cached object from the default cache.
     *
     * @param key key of the object
     * @return cached object
     * @throws CacheManagerException if any problems arise during the operation
     * @throws NotInCacheException if the key does not exist
     */
    Object get(String key) throws CacheManagerException, NotInCacheException;

    /**
     * Removes cached object from the default cache
     *
     * @param key key of the object
     * @return true if the key exists
     * @throws CacheManagerException if any problems arise during the operation
     */
    boolean remove(String key) throws CacheManagerException;

    /**
     * Remove all the entries from the default cache.
     *
     * @throws CacheManagerException if any problems arise during the operation
     */
    void removeAll() throws CacheManagerException;

    /**
     * Retrieves all keys from the default cache.
     *
     * @return list of keys
     * @throws CacheManagerException if any problems arise during the operation
     */
    List getAllKeys() throws CacheManagerException;
}