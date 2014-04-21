package com.atlassian.crowd.manager.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.List;


public class CacheManagerEhcache implements CacheManager
{
    private static final Logger logger = Logger.getLogger(CacheManagerEhcache.class);

    private String DEFAULT_CACHE = CacheManagerEhcache.class.getName();

    private final net.sf.ehcache.CacheManager cacheManager;

    public CacheManagerEhcache(net.sf.ehcache.CacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
    }

    protected void configureCaches()
    {
        configureCache(DEFAULT_CACHE);
    }

    /**
     * Creates a cache if it doesn't already exist - if the cache has been defined in the configuration xml, then it
     * will not be recreated.
     *
     * @param cacheName Name of the cache to ensure exists
     */
    protected void configureCache(String cacheName)
    {
        try
        {
            if (!cacheManager.cacheExists(cacheName))
            {
                cacheManager.addCache(cacheName);
            }
        }
        catch (Exception e)
        {
            logger.fatal(e.getMessage(), e);
        }
    }

    protected Cache getCache(String cacheName)
    {
        configureCache(cacheName);
        return cacheManager.getCache(cacheName);
    }

    public Object get(String key) throws CacheManagerException, NotInCacheException
    {
        return get(DEFAULT_CACHE, key);
    }

    public void put(String key, Serializable obj) throws CacheManagerException
    {
        put(DEFAULT_CACHE, key, obj);
    }

    public boolean remove(String key) throws CacheManagerException
    {
        return remove(DEFAULT_CACHE, key);
    }

    public void removeAll() throws CacheManagerException
    {
        removeAll(DEFAULT_CACHE);
    }

    public List getAllKeys() throws CacheManagerException
    {
        return getAllKeys(DEFAULT_CACHE);
    }


    public void put(String cacheName, Serializable key, Serializable obj) throws CacheManagerException
    {
        Element element = new Element(key, obj);

        try
        {
            getCache(cacheName).put(element);
        }
        catch (CacheException e)
        {
            throw new CacheManagerException(e);
        }
    }

    public Object get(String cacheName, Serializable key) throws CacheManagerException, NotInCacheException
    {
        try
        {
            Element element = getCache(cacheName).get(key);

            if (element == null)
            {
                throw new NotInCacheException();
            }

            return element.getValue();
        }
        catch (CacheException e)
        {
            throw new CacheManagerException(e);
        }
    }

    public boolean remove(String cacheName, Serializable key) throws CacheManagerException
    {
        return getCache(cacheName).remove(key);
    }

    public void removeAll(String cacheName) throws CacheManagerException
    {
        try
        {
            getCache(cacheName).removeAll();
        }
        // left in place because version 1.1 of the library throws IOException
        catch (Exception e)
        {
            throw new CacheManagerException(e);
        }
    }

    public List getAllKeys(String cacheName) throws CacheManagerException
    {
        List keys;
        try
        {
            keys = getCache(cacheName).getKeys();

        }
        catch (CacheException e)
        {
            throw new CacheManagerException("Failed to load keys from cache", e);
        }
        return keys;
    }

    public void afterPropertiesSet() throws Exception
    {
        configureCaches();
    }
}