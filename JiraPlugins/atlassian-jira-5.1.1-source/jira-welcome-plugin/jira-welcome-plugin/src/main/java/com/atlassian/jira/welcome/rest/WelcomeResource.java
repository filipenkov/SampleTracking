package com.atlassian.jira.welcome.rest;

import com.atlassian.jira.rest.api.http.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.welcome.WelcomeUserPreferenceManager;
import com.atlassian.jira.welcome.conditions.ShowWelcomeCondition;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * REST Resource to set if a user should see the welcome dialog.
 *
 * @since v1.0
 */
@AnonymousAllowed
@Path("show")
public class WelcomeResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final WelcomeUserPreferenceManager welcomeManager;

    public WelcomeResource(final JiraAuthenticationContext authenticationContext,
                           final WelcomeUserPreferenceManager welcomeManager)
    {
        this.authenticationContext = authenticationContext;
        this.welcomeManager = welcomeManager;
    }

    @DELETE
    public Response dontShownForUser()
    {
        welcomeManager.setShownForUser(authenticationContext.getLoggedInUser(), false);
        return Response.ok().cacheControl(CacheControl.never()).build();
    }
}
