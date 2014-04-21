package com.atlassian.crowd.cache;

import com.atlassian.crowd.manager.cache.CacheManager;
import com.atlassian.crowd.manager.cache.NotInCacheException;
import com.google.common.collect.ImmutableList;

import java.io.Serializable;

/**
 * Default implementation of UserAuthorisationCache in Crowd.
 *
 * @since v2.2
 */
public class UserAuthorisationCacheImpl implements UserAuthorisationCache
{
    private final static String CACHE_NAME = "com.atlassian.crowd.userauthorisationcache";

    private final CacheManager cacheManager;
    
    public UserAuthorisationCacheImpl(final CacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
    }

    public void setPermitted(final String userName, final String applicationName, final boolean permitted)
    {
        cacheManager.put(CACHE_NAME, getCacheKey(userName, applicationName), permitted);
    }

    public Boolean isPermitted(final String userName, final String applicationName)
    {
        try
        {
            return (Boolean) cacheManager.get(CACHE_NAME, getCacheKey(userName, applicationName));
        }
        catch (NotInCacheException e)
        {
            return null;
        }
    }

    public void clear()
    {
        cacheManager.removeAll(CACHE_NAME);
    }

    /**
     * Returns the cache key based on the username and application name.
     *
     * @param userName username
     * @param applicationName name of the application
     * @return cache key
     */
    private Serializable getCacheKey(final String userName, final String applicationName)
    {
        return ImmutableList.of(userName, applicationName);
    }
}
