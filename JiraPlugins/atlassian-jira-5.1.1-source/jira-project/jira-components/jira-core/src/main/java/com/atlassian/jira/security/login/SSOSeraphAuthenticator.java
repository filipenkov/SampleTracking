package com.atlassian.jira.security.login;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.integration.http.CrowdHttpAuthenticator;
import com.atlassian.crowd.integration.rest.service.factory.RestCrowdHttpAuthenticationFactory;
import com.atlassian.crowd.integration.seraph.v25.CrowdAuthenticator;
import com.atlassian.jira.ComponentManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;


/**
 * Seraph Authenticator for providing single signon with Crowd.
 *
 * @since v4.3
 */

public class SSOSeraphAuthenticator extends CrowdAuthenticator
{
    private static final String JIRA_USER_DASHBOARD_CURRENT_PAGE = "jira.user.dashboard.current.page";

    public SSOSeraphAuthenticator()
    {
        this(RestCrowdHttpAuthenticationFactory.getAuthenticator());
    }

    public SSOSeraphAuthenticator(CrowdHttpAuthenticator crowdHttpAuthenticator)
    {
        super(crowdHttpAuthenticator);
    }

    protected void logoutUser(HttpServletRequest request)
    {
        HttpSession session = request.getSession();

        // We need to remove this single attribute from the JIRA session since
        // it is used to display the default dashboard or a configured custom user dashboard
        // When a user is logged out we should always just show the default dashboard and not a configured one.
        session.removeAttribute(JIRA_USER_DASHBOARD_CURRENT_PAGE);
    }

    protected Principal getUser(String username)
    {
        return getCrowdService().getUser(username);
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
