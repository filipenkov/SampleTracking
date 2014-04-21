package com.atlassian.crowd.integration.http;

import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.integration.http.util.CrowdHttpTokenHelper;
import com.atlassian.crowd.model.authentication.CookieConfiguration;
import com.atlassian.crowd.model.authentication.UserAuthenticationContext;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.CrowdClient;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 * Implementation of CrowdHttpAuthenticator.
 */
public class CrowdHttpAuthenticatorImpl implements CrowdHttpAuthenticator
{
    private static final int MILLIS_IN_MINUTE = 60000;

    private static final Logger LOGGER = Logger.getLogger(CrowdHttpAuthenticator.class);
    private final CrowdClient client;
    private final ClientProperties clientProperties;
    private final CrowdHttpTokenHelper tokenHelper;

    public CrowdHttpAuthenticatorImpl(final CrowdClient client, final ClientProperties clientProperties, final CrowdHttpTokenHelper tokenHelper)
    {
        this.client = client;
        this.clientProperties = clientProperties;
        this.tokenHelper = tokenHelper;
    }

    public User getUser(final HttpServletRequest request)
            throws InvalidTokenException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        String ssoToken = tokenHelper.getCrowdToken(request, getCookieTokenKey());
        if (ssoToken != null)
        {
            return client.findUserFromSSOToken(ssoToken);
        }
        else
        {
            LOGGER.debug("Could not find user from token.");
            return null;
        }
    }

    public User authenticate(final HttpServletRequest request, final HttpServletResponse response, final String username, final String password)
            throws InvalidTokenException, ApplicationAccessDeniedException, ExpiredCredentialException, InactiveAccountException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        UserAuthenticationContext userAuthenticationContext = tokenHelper.getUserAuthenticationContext(request, username, password, clientProperties);
        CookieConfiguration cookieConfig = client.getCookieConfiguration();
        String ssoToken = null;
        try
        {
            ssoToken = client.authenticateSSOUser(userAuthenticationContext);
            tokenHelper.setCrowdToken(request, response, ssoToken, clientProperties, cookieConfig);
        }
        finally
        {
            // clean up the session information if the authentication shouldn't be allowed
            if (ssoToken == null)
            {
                tokenHelper.removeCrowdToken(request, response, clientProperties, cookieConfig);
            }
        }

        return client.findUserFromSSOToken(ssoToken);
    }

    public User authenticateWithoutValidatingPassword(final HttpServletRequest request, final HttpServletResponse response, final String username)
            throws InvalidTokenException, ApplicationAccessDeniedException, InactiveAccountException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        UserAuthenticationContext userAuthenticationContext = tokenHelper.getUserAuthenticationContext(request, username, null, clientProperties);
        CookieConfiguration cookieConfig = client.getCookieConfiguration();
        String ssoToken = null;
        try
        {
            ssoToken = client.authenticateSSOUserWithoutValidatingPassword(userAuthenticationContext);
            tokenHelper.setCrowdToken(request, response, ssoToken, clientProperties, cookieConfig);
        }
        finally
        {
            // clean up the session information if the authentication shouldn't be allowed
            if (ssoToken == null)
            {
                tokenHelper.removeCrowdToken(request, response, clientProperties, cookieConfig);
            }
        }

        return client.findUserFromSSOToken(ssoToken);
    }

    public boolean isAuthenticated(final HttpServletRequest request, final HttpServletResponse response)
            throws OperationFailedException
    {
        HttpSession session = request.getSession();

        // Check if we have a token, if it is not present, assume we are no longer authenticated
        String token = tokenHelper.getCrowdToken(request, clientProperties.getCookieTokenKey());
        if (token == null)
        {
            LOGGER.debug("Non authenticated request, unable to find a valid Crowd token.");
            return false;
        }

        // get the last validation from the session
        Date lastValidation = (Date) session.getAttribute(clientProperties.getSessionLastValidation());

        // only check if the last validation has occurred, and the validation is not required on every request
        if (lastValidation != null && clientProperties.getSessionValidationInterval() > 0)
        {

            // if the validation has previously been done, add the previous time plus the allowed interval
            long timeSpread = lastValidation.getTime() + (MILLIS_IN_MINUTE * clientProperties.getSessionValidationInterval());

            // if the interval has not been reached, allow the previous validation
            if (timeSpread > System.currentTimeMillis())
            {
                return true;
            }
        }

        try
        {
            client.validateSSOAuthentication(token, tokenHelper.getValidationFactorExtractor().getValidationFactors(request));
            CookieConfiguration cookieConfig = client.getCookieConfiguration();
            tokenHelper.setCrowdToken(request, response, token, clientProperties, cookieConfig);
            return true;
        }
        catch (ApplicationPermissionException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(e.getMessage(), e);
            }

            // don't invalidate the client unless asked to do so (authentication status fails, or logoff).

            return false;
        }
        catch (InvalidAuthenticationException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(e.getMessage(), e);
            }

            // don't invalidate the client unless asked to do so (authentication status fails, or logoff).

            return false;
        }
        catch (InvalidTokenException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(e.getMessage(), e);
            }

            // don't invalidate the client unless asked to do so (authentication status fails, or logoff).

            return false;
        }
    }

    public void logout(final HttpServletRequest request, final HttpServletResponse response)
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        String ssoToken = tokenHelper.getCrowdToken(request, getCookieTokenKey());
        if (ssoToken != null)
        {
            client.invalidateSSOToken(ssoToken);
        }

        CookieConfiguration cookieConfig = client.getCookieConfiguration();
        tokenHelper.removeCrowdToken(request, response, clientProperties, cookieConfig);
    }

    public String getToken(HttpServletRequest request)
    {
        return tokenHelper.getCrowdToken(request, getCookieTokenKey());
    }

    private String getCookieTokenKey()
    {
        return clientProperties.getCookieTokenKey();
    }
}
