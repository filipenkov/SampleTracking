package com.atlassian.crowd.plugin.rest.filter;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.validation.ClientValidationException;
import com.atlassian.crowd.manager.validation.ClientValidationManager;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.plugin.rest.service.util.AuthenticatedApplicationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implementation of HTTP Basic Authentication such that all invocations
 * to the filter must be authenticated with a valid application name
 * and corresponding password.
 *
 * As a performance enhancement the application name is saved in the
 * session after a successful authentication. Password check is waived
 * with consequent requests when the application name in the request
 * matches the application name in the session. Clients wishing to take
 * advantage of this feature must support cookies.
 */
public class BasicApplicationAuthenticationFilter extends AbstractBasicAuthenticationFilter
{
    private static final String APPLICATION_AUTHENTICATION_ERROR_MSG = "Application failed to authenticate";
    private static final Logger LOG = LoggerFactory.getLogger(BasicApplicationAuthenticationFilter.class);
    private final ApplicationManager applicationManager;
    private final ClientValidationManager clientValidationManager;

    public BasicApplicationAuthenticationFilter(ApplicationManager applicationManager, ClientValidationManager clientValidationManager)
    {
        this.applicationManager = applicationManager;
        this.clientValidationManager = clientValidationManager;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException
    {
        ensureSeraphForwardsRequest(servletRequest);
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        final Credentials credentials = getBasicAuthCredentials(request);

        if (credentials == null)
        {
            LOG.debug("No basic auth credentials found in request, responding with authentication challenge");
            respondWithChallenge(response);
        }
        else
        {
            try
            {
                Application application = applicationManager.findByName(credentials.getName());
                clientValidationManager.validate(application, request);
                if (isAuthenticated(request, credentials))
                {
                    LOG.debug("Application '{}' is already authenticated", credentials.getName());
                    chain.doFilter(request, servletResponse);
                }
                else if (authenticate(application, credentials.getPassword()))
                {
                    LOG.debug("Application '{}' authenticated successfully", credentials.getName());
                    setAuthenticatedEntity(request, credentials.getName());
                    chain.doFilter(request, servletResponse);
                }
                else
                {
                    LOG.info("Application '{}' failed authentication", credentials.getName());
                    respondWithChallenge(response);
                }
            }
            catch (ApplicationNotFoundException e)
            {
                LOG.info("Application '{}' failed authentication", credentials.getName());
                respondWithChallenge(response);
            }
            catch (ClientValidationException e)
            {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            }
        }
    }

    private boolean authenticate(Application application, String password) throws ClientValidationException
    {
        try
        {
            return applicationManager.authenticate(application, PasswordCredential.unencrypted(password));
        }
        catch (ApplicationNotFoundException e)
        {
            LOG.info("Application with name '" + application.getName() + "' does not exist");
            return false;
        }
    }

    /**
     * Returns the authenticated entity from the <code>request</code>, or <tt>null</tt> if there is no authenticated entity.
     *
     * @param request Request
     * @return authenticated entity from the <code>request</code>, or <tt>null</tt> if there is no authenticated entity.
     */
    @Override
    protected String getAuthenticatedEntity(final HttpServletRequest request)
    {
        return AuthenticatedApplicationUtil.getAuthenticatedApplication(request);
    }

    /**
     * Sets the authenticated entity.
     *
     * @param request Request
     * @param name the name of the authenticated entity
     */
    @Override
    protected void setAuthenticatedEntity(final HttpServletRequest request, final String name)
    {
        AuthenticatedApplicationUtil.setAuthenticatedApplication(request, name);
    }

    @Override
    protected String getEntityAttributeKey()
    {
        return AuthenticatedApplicationUtil.APPLICATION_ATTRIBUTE_KEY;
    }

    @Override
    protected String getAuthenticationErrorMessage()
    {
        return APPLICATION_AUTHENTICATION_ERROR_MSG;
    }

    @Override
    protected String getBasicRealm()
    {
        return "Crowd REST Service";
    }
}
