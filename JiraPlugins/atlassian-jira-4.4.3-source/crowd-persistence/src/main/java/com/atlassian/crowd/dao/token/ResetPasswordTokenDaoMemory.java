package com.atlassian.crowd.dao.token;

import com.atlassian.crowd.exception.ObjectNotFoundException;
import com.atlassian.crowd.manager.cache.CacheManager;
import com.atlassian.crowd.manager.cache.CacheManagerException;
import com.atlassian.crowd.manager.cache.NotInCacheException;
import com.atlassian.crowd.model.token.ResetPasswordToken;
import org.apache.log4j.Logger;

/**
 * Stores ResetPasswordToken in memory.
 */
public class ResetPasswordTokenDaoMemory implements ResetPasswordTokenDao
{
    private static final Logger logger = Logger.getLogger(ResetPasswordTokenDaoMemory.class);
    public static final String CACHE_NAME = ResetPasswordToken.class.getName();

    private final CacheManager cacheManager;

    public ResetPasswordTokenDaoMemory(final CacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
    }

    public ResetPasswordToken addToken(final ResetPasswordToken token)
    {
        try
        {
            cacheManager.put(CACHE_NAME, token.getUsername(), token);
        }
        catch (CacheManagerException e)
        {
            logger.error(e.getMessage(), e);
        }
        return token;
    }

    public ResetPasswordToken findTokenByUsername(final String username) throws ObjectNotFoundException
    {
        try
        {
            return (ResetPasswordToken)cacheManager.get(CACHE_NAME, username);
        }
        catch (NotInCacheException e)
        {
            throw new ObjectNotFoundException(ResetPasswordToken.class, username);
        }
    }

    public void removeTokenByUsername(String username)
    {
        cacheManager.remove(CACHE_NAME, username);
    }
}
