package com.atlassian.jira.security.login;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.exception.AccountNotFoundException;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.crowd.exception.runtime.CommunicationException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.seraph.auth.AuthenticationContextAwareAuthenticator;
import com.atlassian.seraph.auth.AuthenticationErrorType;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * JIRA's standard implementation of Seraph's Authenticator interface.
 * <p/>
 * It uses Crowd Embedded to implement the abstract methods of Seraph's default base implementation.
 *
 * @since v4.3
 */
@AuthenticationContextAwareAuthenticator
public class JiraSeraphAuthenticator extends DefaultAuthenticator
{
    private static final Logger log = Logger.getLogger(JiraSeraphAuthenticator.class);

    private final PrincipalInSessionPlacer principalInSession = new PrincipalInSessionPlacer();

    @Override
    protected Principal getUser(final String username)
    {
        // This is horrid, but consumers of this down cast to an OSUser. 
        return OSUserConverter.convertToOSUser(getCrowdService().getUser(username));
    }

    @Override
    protected boolean authenticate(final Principal user, final String password) throws AuthenticatorException
    {
        try
        {
            crowdServiceAuthenticate(user, password);
            return true;
        }
        catch (AccountNotFoundException e)
        {
            log.debug("authenticate : '" + user.getName() + "' does not exist and cannot be authenticated.");
            return false;
        }
        catch (FailedAuthenticationException e)
        {
            return false;
        }
        catch (CommunicationException ex)
        {
            throw new AuthenticatorException(AuthenticationErrorType.CommunicationError);
        }
        catch (OperationFailedException ex)
        {
            // Unexpected error - log the stacktrace.
            log.error("Error occurred while trying to authenticate user '" + user.getName() + "'.", ex);
            throw new AuthenticatorException(AuthenticationErrorType.UnknownError);
        }
    }

    private void crowdServiceAuthenticate(Principal user, String password) throws FailedAuthenticationException
    {
        // set the context class loader to this one, so that sun LDAP classes use the right classloader
        // (the same classloader as Crowd embedded itself).
        // Fixes JRADEV-6087/JRA-23998.
        // A better fix would be within crowd embedded itself, CWD-2414/CWD-2244
        final Thread currentThread = Thread.currentThread();
        final ClassLoader origCCL = currentThread.getContextClassLoader();
        try
        {
            currentThread.setContextClassLoader(this.getClass().getClassLoader());
            getCrowdService().authenticate(user.getName(), password);
        }
        finally
        {
            currentThread.setContextClassLoader(origCCL);
        }
    }

    protected void putPrincipalInSessionContext(final HttpServletRequest httpServletRequest, final Principal principal)
    {
        principalInSession.putPrincipalInSessionContext(httpServletRequest, principal);
    }

    /**
     * Get a fresh version of the Crowd Read Write service from Pico Container.
     *
     * @return fresh version of the Crowd Read Write service from Pico Container.
     */
    private CrowdService getCrowdService()
    {
        return ComponentManager.getComponent(CrowdService.class);
    }
}

