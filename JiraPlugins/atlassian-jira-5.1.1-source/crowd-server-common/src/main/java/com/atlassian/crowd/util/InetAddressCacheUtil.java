package com.atlassian.crowd.util;

import com.atlassian.crowd.manager.cache.CacheManager;
import com.atlassian.crowd.manager.cache.NotInCacheException;
import com.atlassian.crowd.model.application.Application;

import java.net.InetAddress;

/**
 * Utility class to store in a cache whether the InetAddress is permitted or forbidden to make a request to the Crowd server.
 */
public class InetAddressCacheUtil
{
    public static final String INET_ADDRESS_CACHE_NAME = Application.class.toString() + InetAddress.class.toString() + "cache";

    private final CacheManager cacheManager;

    public InetAddressCacheUtil(final CacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
    }

    /**
     * Sets in the cache that <code>address</code> is permitted/forbidden from making a request to the Crowd server.
     *
     * @param application application that is requesting the connection.
     * @param address InetAddress to set
     * @param permitted whether <code>address</code> is permitted or forbidden to make a request.
     */
    public void setPermitted(Application application, InetAddress address, boolean permitted)
    {
        String key = getKeyName(application, address);
        cacheManager.put(INET_ADDRESS_CACHE_NAME, key, permitted);
    }

    /**
     * Gets from cache whether the <code>application</code> with <code>address</code> is permitted to make a request
     * to the Crowd server.
     *
     * @param application Application making the request.
     * @param address address of the client making the request.
     * @return true if <code>application</code> with <code>address</code> is permitted to make a request
     * @throws NotInCacheException if the address is not in the cache
     */
    public boolean getPermitted(Application application, InetAddress address) throws NotInCacheException
    {
        String key = getKeyName(application, address);
        Boolean permitted = ((Boolean) cacheManager.get(INET_ADDRESS_CACHE_NAME, key));
        return permitted != null && permitted;
    }

    /**
     * Clears the entire cache storing the address permissions.
     */
    public void clearCache()
    {
        cacheManager.removeAll(INET_ADDRESS_CACHE_NAME);
    }

    /**
     * Returns the name of the cache key, given the application and the address.
     *
     * @param application Application
     * @param address InetAddress
     * @return name of the key
     */
    private static String getKeyName(Application application, InetAddress address)
    {
        return application.getName() + "#" + address.getHostAddress();
    }
}
