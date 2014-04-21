package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.dev.backdoor.util.CacheControl.never;

/**
 * Are you allowed to enable or disable issue links.
 *
 * @since v5.0.4
 */
@Path ("issueLinking")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class IssueLinkingBackdoorResource
{
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final ApplicationProperties applicationProperties;

    public IssueLinkingBackdoorResource(IssueLinkTypeManager issueLinkTypeManager, ApplicationProperties applicationProperties)
    {
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.applicationProperties = applicationProperties;
    }
    
    @GET
    public Response get()
    {
        return Response.ok(applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING)).cacheControl(never()).build();
    }

    @POST
    public Response set(Boolean enabled)
    {
        if (enabled != applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING))
        {
            applicationProperties.setOption(APKeys.JIRA_OPTION_ISSUELINKING, enabled);
        }
        return Response.ok(applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING)).cacheControl(never()).build();
    }

    @GET
    @AnonymousAllowed
    @Path("create")
    public Response addLink(
            @QueryParam ("name") String name,
            @QueryParam ("outward") String outward,
            @QueryParam ("inward") String inward,
            @QueryParam ("style") String style)
    {
        issueLinkTypeManager.createIssueLinkType(name, outward, inward, style);
        return Response.ok(null).build();
    }
}
