package com.atlassian.jira.plugin.issuenav.rest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.issuenav.SuppressedTipsManager;
import com.atlassian.jira.rest.api.http.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserPropertyManager;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * A REST resource to manage suppressed tips on a per-user basis.
 */
@Path("suppressedTips")
public class SuppressedTipsResource
{
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final SuppressedTipsManager suppressedTipsManager;

    public SuppressedTipsResource(JiraAuthenticationContext jiraAuthenticationContext, UserPropertyManager userPropertyManager)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;

        // TODO: This really should be injected, but conditions are created in a DI container that doesn't contain
        // plugin components. The underlying cause of this problem is fixed in 5.1, so we can remove this hack then.
        this.suppressedTipsManager = new SuppressedTipsManager(userPropertyManager);
    }

    /**
     * Suppress a tip for the authenticated user.
     *
     * @param tipKey The key of the tip that is to be suppressed.
     * @return {@code 200 OK} if the request executed successfully,
     *         {@code 400 Bad Request} if {@code key} is not valid tip key.
     */
    @POST
    public Response add(@FormParam("tipKey") String tipKey)
    {
        try
        {
            User user = jiraAuthenticationContext.getLoggedInUser();
            suppressedTipsManager.setSuppressed(tipKey, user, true);
            return Response.ok().cacheControl(CacheControl.never()).build();
        }
        catch (IllegalArgumentException e)
        {
            return Response.status(Response.Status.BAD_REQUEST).cacheControl(CacheControl.never()).build();
        }
    }
}