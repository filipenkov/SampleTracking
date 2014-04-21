package com.atlassian.crowd.integration.seraph.v25;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.integration.http.CacheAwareCrowdHttpAuthenticator;
import com.atlassian.crowd.integration.http.CrowdHttpAuthenticator;
import com.atlassian.crowd.service.AuthenticatorUserCache;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.auth.LoginReason;
import com.atlassian.seraph.elevatedsecurity.ElevatedSecurityGuard;
import com.atlassian.seraph.filter.BaseLoginFilter;
import com.atlassian.seraph.util.RedirectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

import static com.atlassian.seraph.auth.LoginReason.OK;

public abstract class CrowdAuthenticator extends DefaultAuthenticator
{
    private static final String SESSION_TOKEN_KEY = CrowdAuthenticator.class.getName() + "#SESSION_TOKEN_KEY";

    protected static final Logger logger = LoggerFactory.getLogger(CrowdAuthenticator.class);

    private static final String CORRECT_PASSWORD = "c";
    private static final String INCORRECT_PASSWORD = "i";

    private final CrowdHttpAuthenticator crowdHttpAuthenticator;

    public CrowdAuthenticator(CrowdHttpAuthenticator crowdHttpAuthenticator)
    {
        this.crowdHttpAuthenticator = new CacheAwareCrowdHttpAuthenticator(crowdHttpAuthenticator, new AuthenticatorUserCache()
        {
            // CacheAwareCrowdHttpAuthenticator needs to know how to fetch users
            public void fetchInCache(String username)
                    throws UserNotFoundException, InvalidAuthenticationException, OperationFailedException
            {
               fetchUserInCache(username);
            }
        });
    }

    /**
     * Fetches a user with the given username in the cache, in case the user
     * exists, but cannot be found from the cache yet.
     *
     * By default this method will call {@link #getUser(String)}, but JIRA needs
     * to override it, because {@link com.atlassian.seraph.auth.DefaultAuthenticator#getUser(String)}
     * only checks the local cache when retrieving users.
     *
     * @param username username of the user to be fetched
     * @throws com.atlassian.crowd.exception.InvalidAuthenticationException if the application or user authentication was not successful.
     * @throws com.atlassian.crowd.exception.OperationFailedException       if the operation has failed for an unknown reason
     */
    protected void fetchUserInCache(String username)
            throws UserNotFoundException, InvalidAuthenticationException, OperationFailedException
    {
        getUser(username);
    }

    /**
     * Override the super method, always return true so that authentication is not called twice when a user logs in.
     *
     * More info: this is because we subclass login() to perform the authentication, but also call super.login(),
     * which then calls this authenticate() method. We also can't just implement the authenticate() method as it
     * does not provide the HttpServletRequest nor the HttpServletResponse, which are both required for generating
     * and setting the Crowd SSO token.
     */
    protected boolean authenticate(Principal user, String password)
    {
        return CORRECT_PASSWORD.equals(password);
    }

    /**
     * We must override the login() method as it gives us access to the HttpServletRequest and HttpServletResponse,
     * which Crowd needs in order to generate and set the Crowd SSO token.
     *
     * However, super.login() does some magic, including elevated security checks, so we still need to call
     * super.login() - which in turn calls authenticate().
     *
     * Problem is, we can't put our actual authentication login in their as authenticate() doesn't pass the
     * HttpServletRequest or HttpServletResponse into the method.
     *
     * Perhaps in a later version of Seraph, we can change authenticate to take the HttpServletRequest and
     * HttpServletResponse as parameters. But for now, we have a hacky solution that piggybacks the password
     * parameter so authenticate() knows whether to return true or false.
     *
     * @param request HttpServletRequest obtain validation factors.
     * @param response HttpServletResponse SSO cookie is set on response.
     * @param username name of user to authenticate.
     * @param password credential to authenticate.
     * @param cookie whether to set a remember-me cookie or not.
     * @return <code>true</code> if and only if authentication was succe
     * @throws AuthenticatorException
     */
    public boolean login(HttpServletRequest request, HttpServletResponse response,
                         String username, String password, boolean cookie) throws AuthenticatorException
    {
        // as this is an explicit call to log in, we should ignore any previous authentication
        // if all we wanted to do was to check authentication, then we should call isAuthenticated() instead

        boolean authenticated;

        try
        {
            // clear anything that resembles a previous authentication
            this.logout(request, response);

            // unfortunately that will stamp OUT as the LoginReason, so we need to reset it
            request.setAttribute(LoginReason.REQUEST_ATTR_NAME, null);

            // run a clean new authentication
            logger.debug("Authenticating user with Crowd");
            crowdHttpAuthenticator.authenticate(request, response, username, password);

            authenticated = true;
        }
        catch (Exception e)
        {
            logger.info(e.getMessage(), e);
            authenticated = false;
        }

        // set password to true so that when we call super.login(), the authentication is treated as successful
        // perform session/cookie authentication stuff for seraph
        String fakePassword = authenticated ? CORRECT_PASSWORD : INCORRECT_PASSWORD;

        logger.debug("Updating user session for Seraph");

        authenticated = super.login(request, response, username, fakePassword, cookie);                           

        return authenticated;
    }

    public boolean logout(HttpServletRequest request, HttpServletResponse response) throws AuthenticatorException
    {
        try
        {
            logger.debug("Logging off from Crowd");

            // Invalidate the user in Crowd.
            crowdHttpAuthenticator.logout(request, response);

            // Logout the user, removing Crowd specific attributes
            logger.debug("Invalidating user in Crowd-Seraph specific session variables");
            logoutUser(request);
        }
        catch (Exception e)
        {
            logger.info(e.getMessage(), e);
        }

        // Logout via Seraph now.
        logger.debug("Invalidating user in Seraph specific session variables");
        return super.logout(request, response);
    }

    /**
     * Checks to see if the request can be authenticated. This method checks (in order):
     * <ol>
     * <li>
     * Trusted Apps: it is possible that an earlier filter authenticated the request,
     * so check to see if this is the case.
     * </li>
     * <li>
     * Crowd Authenticator: if a valid Crowd session-cookie (token) exists,
     * the HttpAuthenticator will authenticate the request as "valid". This will not
     * place the user into the session. See getUser() to see exactly when the user
     * gets placed into session.
     * </li>
     * <li>
     * Seraph-Remember Me: sees if the request is authenticated via a remember me cookie.
     * If it is, then the user will be automatically logged into session and a Crowd SSO
     * token will be generated and put on the response.
     * </li>
     * <li>
     * Basic Authentication: determines if the request has Basic Auth username/password headers
     * and proceeds to authenticate the user with Crowd if they are present. The user will be
     * automatically logged into session and a Crowd SSO token will be generated and put on the response.
     * </li>
     * </ol>
     * If all checks fail authentication, the isAuthenticated method returns false, and the user is logged out.
     *
     * @param request  servlet request.
     * @param response servlet response.
     * @return true if request can be authenticated.
     */
    protected boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response)
    {
        boolean authenticated;
        
        authenticated = isTrustedAppsRequest(request);

        if (!authenticated)
        {
            try
            {
                // try Crowd's HttpAuthenticator
                authenticated = crowdHttpAuthenticator.isAuthenticated(request, response);
                if (authenticated && logger.isDebugEnabled())
                {
                    logger.debug("User IS authenticated via the Crowd session-token");
                }
                else if (logger.isDebugEnabled())
                {
                    logger.debug("User is NOT authenticated via the Crowd session-token");
                }
            }
            catch (Exception e)
            {
                logger.info("Error while attempting to check if user isAuthenticated with Crowd", e);
            }
        }

        // if Crowd's HttpAuthenticator failed try AutoLogin
        // i.e. try authenticating the user using credentials from the auto-login cookie
        if (!authenticated)
        {
            authenticated = rememberMeLoginToCrowd(request, response);
            if (authenticated && logger.isDebugEnabled())
            {
                logger.debug("Authenticated via remember-me cookie");
            }
            else if (logger.isDebugEnabled())
            {
                logger.debug("Failed to authenticate via remember-me cookie");
            }
        }

        // Attempt Basic-Auth
        if (!authenticated)
        {
            if (RedirectUtils.isBasicAuthentication(request, getAuthType()))
            {
                // this will try logging in using basic authentication (this will send a 401 + auth header
                // if there is no auth header in the request)
                Principal basicAuthUser = getUserFromBasicAuthentication(request, response);
                if (basicAuthUser != null)
                {
                    authenticated = true;
                }
            }
        }

        if (!authenticated)
        {
            if (request.getSession(false) != null)
            {
                logger.debug("Request is not authenticated, logging out the user");
                try
                {
                    logoutUser(request);
                    if (response != null)
                    {
                        super.logout(request, response);
                    }
                }
                catch (AuthenticatorException e)
                {
                    logger.error(e.getMessage(), e);
                }
            }
            else
            {
                logger.debug("Request is not authenticated and has no session.");
            }

            authenticated = false;
        }

        return authenticated;
    }

    /**
     * Attempts to authenticate the request based on the auto-login cookie (if set).
     * This will only authenticate to Crowd via HttpAuthenticator. This will not set
     * any session variables and the like.
     *
     * @param request  servlet request.
     * @param response servlet response.
     * @return true if authentication via HttpAuthenticator using auto-login credentials successful.
     */
    protected boolean rememberMeLoginToCrowd(HttpServletRequest request, HttpServletResponse response)
    {
        // this method puts the user in session if they are auth'd
        Principal cookieUser = getUserFromCookie(request, response);

        if (cookieUser == null)
        {
            // cookie verification failed
            return false;
        }
        else
        {
            logger.debug("User successfully authenticated via remember-me cookie verification");

            // well, the cookie verification says that the user is logged in so we'll trust it
            try
            {
                crowdHttpAuthenticator.authenticateWithoutValidatingPassword(request, response, cookieUser.getName());

                return true;
            }
            catch (Exception e)
            {
                logger.debug("Could not register remember-me cookie authenticated user with Crowd SSO: " + cookieUser.getName() + ", reason: " + e.getMessage(), e);

                // maybe because crowd is down or because the user has been removed from crowd / marked as inactive, ie. shouldn't let him in
                removePrincipalFromSessionContext(request);
                
                return false;
            }
        }
    }

    /**
     * This method will allow you to remove all session information about the user and force them to re-authenticate
     * If you wish to remove specific application attributes for the user, e.g.
     * <code>org.acegisecurity.context.SecurityContextHolder.clearContext();</code> from Bamboo
     *
     * @param request the current request
     */
    protected abstract void logoutUser(HttpServletRequest request);

    public Principal getUser(HttpServletRequest request, HttpServletResponse response)
    {
        ElevatedSecurityGuard securityGuard = getElevatedSecurityGuard();

        Principal user = null;

        if (isTrustedAppsRequest(request))
        {
            return getUserFromSession(request);
        }

        // isAuthenticated performs Crowd cookie verification, remember-me cookie auth and basic auth,
        // see the isAuthenticated() javadoc / impl for more information
        if (isAuthenticated(request, response))
        {
            final String cookieToken = crowdHttpAuthenticator.getToken(request);
            if (cookieToken == null)
            {
                // log the error, and allow the method to return a null user
                logger.error("Could not find cookieToken from authenticated request");
                return null;
            }
            final Object sessionToken = request.getSession().getAttribute(SESSION_TOKEN_KEY);
            if (cookieToken.equals(sessionToken))
            {
                user = getUserFromSession(request);
            }

            // if user does not already exist in the session (or is different to the user logged in via SSO), we put the auth'd user in there
            if (user == null)
            {
                try
                {
                    // find our existing token
                    User crowdUser = crowdHttpAuthenticator.getUser(request);

                    user = getUser(crowdUser.getName());
                }
                catch (Exception e)
                {
                    logger.info(e.getMessage(), e);
                }

                if (user != null)
                {
                    // JRA-20210 Ensure the user is allowed to log in
                    if (authoriseUserAndEstablishSession(request, response, user))
                    {
                        OK.stampRequestResponse(request, response);
                        securityGuard.onSuccessfulLoginAttempt(request, user.getName());
                        request.getSession().setAttribute(SESSION_TOKEN_KEY, cookieToken);
                    }
                    else
                    {
                        return null;
                    }
                }
            }
            else
            {
                // user was obtained from the session and the crowd cookie hasn't changed since, so we're good to stamp him in
                OK.stampRequestResponse(request, response);
            }
        }

        return user;
    }

    private boolean isTrustedAppsRequest(HttpServletRequest request)
    {
        if (BaseLoginFilter.LOGIN_SUCCESS.equals(request.getAttribute(BaseLoginFilter.OS_AUTHSTATUS_KEY)))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("User IS authenticated via previous filter/trusted apps");
            }
            return true;
        }

        return false;
    }
}