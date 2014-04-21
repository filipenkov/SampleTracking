package com.atlassian.plugins.helptips.rest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rest.api.http.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugins.helptips.HelpTipManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * Manages a list of help tips a specific user has dismissed.
 *
 * @since v5.1
 */
@Path("/tips")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class HelpTipResource
{
    private static final Logger log = Logger.getLogger(HelpTipResource.class);

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final HelpTipManager helpTipManager;

    public HelpTipResource(JiraAuthenticationContext jiraAuthenticationContext, HelpTipManager helpTipManager)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.helpTipManager = helpTipManager;
    }

    @GET
    public Response index()
    {
        final User user = jiraAuthenticationContext.getLoggedInUser();
        Collection<String> dismissedTips = helpTipManager.getDismissedTips(user);
        return Response.ok(dismissedTips).cacheControl(CacheControl.never()).build();
    }

    @POST
    public Response dismiss(Tooltip tooltip)
    {
        if (tooltip == null || tooltip.id == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).cacheControl(CacheControl.never()).build();
        }

        try
        {
            final User user = jiraAuthenticationContext.getLoggedInUser();
            helpTipManager.dismissTip(user, tooltip.id);
            return Response.noContent().cacheControl(CacheControl.never()).build();
        }
        catch (IllegalArgumentException e)
        {
            log.debug("dismissal of help tip failed", e);
            return Response.status(Response.Status.BAD_REQUEST).cacheControl(CacheControl.never()).build();
        }
    }

    @DELETE
    public Response undismiss(Tooltip tooltip)
    {
        if (tooltip == null || tooltip.id == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).cacheControl(CacheControl.never()).build();
        }

        try
        {
            final User user = jiraAuthenticationContext.getLoggedInUser();
            helpTipManager.undismissTip(user, tooltip.id);
            return Response.noContent().cacheControl(CacheControl.never()).build();
        }
        catch (IllegalArgumentException e)
        {
            log.debug("undismissal of help tip failed", e);
            return Response.status(Response.Status.BAD_REQUEST).cacheControl(CacheControl.never()).build();
        }
    }

    @JsonAutoDetect
    public static class Tooltip
    {
        @JsonProperty
        public String id;
    }
}
