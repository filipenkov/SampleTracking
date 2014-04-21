package com.atlassian.crowd.integration.http;

import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.service.AuthenticatorUserCache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CacheAwareCrowdHttpAuthenticator ensures that a user exists in the cache when a user is retrieved from the server or
 * is authenticated.
 */
public class CacheAwareCrowdHttpAuthenticator implements CrowdHttpAuthenticator
{
    private final CrowdHttpAuthenticator delegate;
    private final AuthenticatorUserCache userCache;

    public CacheAwareCrowdHttpAuthenticator(CrowdHttpAuthenticator delegate, AuthenticatorUserCache userCache)
    {
        this.delegate = delegate;
        this.userCache = userCache;
    }

    public User getUser(HttpServletRequest request)
            throws InvalidTokenException, InvalidAuthenticationException, ApplicationPermissionException, OperationFailedException
    {
        final User user = delegate.getUser(request);
        ensureUserExistsInCache(user.getName());
        return user;
    }

    public User authenticate(HttpServletRequest request, HttpServletResponse response, String username, String password)
            throws InvalidTokenException, ApplicationAccessDeniedException, InvalidAuthenticationException, ExpiredCredentialException, ApplicationPermissionException, InactiveAccountException, OperationFailedException
    {
        final User user = delegate.authenticate(request, response, username, password);
        ensureUserExistsInCache(user.getName());
        return user;
    }

    public User authenticateWithoutValidatingPassword(HttpServletRequest request, HttpServletResponse response, String username)
            throws InvalidAuthenticationException, OperationFailedException, InvalidTokenException, ApplicationAccessDeniedException, ApplicationPermissionException, InactiveAccountException
    {
        final User user = delegate.authenticateWithoutValidatingPassword(request, response, username);
        ensureUserExistsInCache(user.getName());
        return user;
    }

    /**
     * Fetch the user from the UserManager to ensure that the user will exist in the cache.
     * This is needed so JIRA knows about the user when using delegated authentication - see: CWD-1972
     *
     * @param name username of the user
     * @throws InvalidAuthenticationException if the application has not been authenticated
     * @throws OperationFailedException if the operation has failed for an unknown reason
     */
    private void ensureUserExistsInCache(String name) throws InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            userCache.fetchInCache(name);
        }
        catch (UserNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response)
            throws OperationFailedException
    {
        return delegate.isAuthenticated(request, response);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response)
            throws InvalidAuthenticationException, ApplicationPermissionException, OperationFailedException
    {
        delegate.logout(request, response);
    }

    public String getToken(HttpServletRequest request)
    {
        return delegate.getToken(request);
    }
}
