package com.atlassian.jira.security.login;

import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.seraph.auth.DefaultAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;

/**
 * Common code for putting a Principal into the users session.  This then indicates "loggedinedness" to Serpah so that
 * it can re-establish the user in play on the next request.  We use a new stand alone Principal because function tests
 * can run our of memory because the PICO backed user object keeps the whole world around
 *
 * @since v4.4
 */
class PrincipalInSessionPlacer
{
    /**
     * Adaptive strategy to place user objects in the session.
     *
     * @param httpServletRequest the HTTP request in play
     * @param principal be careful the principal can be null
     */
    void putPrincipalInSessionContext(final HttpServletRequest httpServletRequest, final Principal principal)
    {
        /**
         * Eventually we want to get rid of this whole class but there we concerns that the object returned
         * to plugins should stay the same (eg an editable OSUSer) for JIRA 4.4.  But since we know Studio is in a controlled
         * environment, we can use feature manager to get this in play now.
         *
         * In JIRA 5.0 we should remove this whole class and return ImmutableUser representations from the Authenticator.getUser() call
         * and hence this class is redundant.
         */
        final Principal desiredPrincipal;
        if (useImmutableUser(principal))
        {
            desiredPrincipal = ImmutableUser.newUser().name(principal.getName()).toUser();
        }
        else
        {
            desiredPrincipal = principal;
        }

        final HttpSession httpSession = httpServletRequest.getSession();
        httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, desiredPrincipal);
        httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
    }

    private boolean useImmutableUser(Principal principal)
    {
        return principal != null && getFeatureManager().isEnabled(CoreFeatures.ON_DEMAND);
    }

    private FeatureManager getFeatureManager()
    {
        return ComponentAccessor.getComponent(FeatureManager.class);
    }
}
