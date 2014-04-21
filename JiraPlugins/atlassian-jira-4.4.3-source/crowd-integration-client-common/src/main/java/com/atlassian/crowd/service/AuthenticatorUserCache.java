package com.atlassian.crowd.service;

import com.atlassian.crowd.exception.*;

/**
 * Interface that {@link com.atlassian.crowd.integration.http.CacheAwareCrowdHttpAuthenticator}
 * calls when it want's to ensure a user exists in the cache.
 */
public interface AuthenticatorUserCache
{
    /**
     * Fetches a user with the given username in the cache, in case the user
     * exists, but cannot be found from the cache yet.
     *
     * @param username username of the user to be fetched
     * @throws com.atlassian.crowd.exception.InvalidAuthenticationException if the application or user authentication was not successful.
     * @throws com.atlassian.crowd.exception.OperationFailedException       if the operation has failed for an unknown reason
     */
    void fetchInCache(String username)
            throws UserNotFoundException, InvalidAuthenticationException, OperationFailedException;
}