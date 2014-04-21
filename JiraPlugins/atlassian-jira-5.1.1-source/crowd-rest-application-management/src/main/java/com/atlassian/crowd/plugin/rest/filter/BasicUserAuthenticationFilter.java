package com.atlassian.crowd.plugin.rest.filter;

import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implementation of HTTP Basic Authentication such that all invocations
 * to the filter must be authenticated with a valid admin name
 * and corresponding password.
 *
 * As a performance enhancement the user name is saved in the
 * session after a successful authentication. Password check is waived
 * with consequent requests when the user name in the request
 * matches the user name in the session. Clients wishing to take
 * advantage of this feature must support cookies.
 *
 * @since 2.2
 */
public class BasicUserAuthenticationFilter extends AbstractBasicAuthenticationFilter
{
    private static final String USER_ATTRIBUTE_KEY = "com.atlassian.crowd.authenticated.user.name";
    private static final String USER_AUTHENTICATION_ERROR_MSG = "User failed to authenticate";
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicUserAuthenticationFilter.class);

    private final UserManager userManager;

    public BasicUserAuthenticationFilter(final UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void init(final FilterConfig filterConfig) throws ServletException
    {
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain chain) throws IOException, ServletException
    {
        ensureSeraphForwardsRequest(servletRequest);
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        final Credentials credentials = getBasicAuthCredentials(request);

        if (credentials == null)
        {
            LOGGER.debug("No basic auth credentials found in request, responding with authentication challenge");
            respondWithChallenge(response);
        }
        else
        {
            if (isAuthenticated(request, credentials))
            {
                LOGGER.debug("User '{}' is already authenticated", credentials.getName());
                chain.doFilter(request, servletResponse);
            }
            else if (authenticate(credentials.getName(), credentials.getPassword()))
            {
                LOGGER.debug("User '{}' authenticated successfully", credentials.getName());
                setAuthenticatedEntity(request, credentials.getName());
                chain.doFilter(request, servletResponse);
            }
            else
            {
                LOGGER.info("User '{}' failed authentication", credentials.getName());
                respondWithChallenge(response);
            }
        }
    }

    /**
     * Returns <tt>true</tt> if the user is successfully authenticated and is an admin.
     *
     * @param username username
     * @param password password
     * @return <tt>true</tt> if the user is successfully authenticated and is an admin.
     */
    private boolean authenticate(final String username, final String password)
    {
        boolean success = userManager.authenticate(username, password) && userManager.isAdmin(username);
        if (success)
        {
            LOGGER.debug("User {} was successfully authenticated.", username);
        }
        else
        {
            LOGGER.debug("User {} was unsuccessfully authenticated", username);
        }
        return success;
    }

    @Override
    protected String getEntityAttributeKey()
    {
        return USER_ATTRIBUTE_KEY;
    }

    @Override
    protected String getAuthenticationErrorMessage()
    {
        return USER_AUTHENTICATION_ERROR_MSG;
    }

    @Override
    protected String getBasicRealm()
    {
        return "Crowd REST Application Management Service";
    }
}
