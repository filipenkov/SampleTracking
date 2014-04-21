package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since 4.2
 */
@Path ("status")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class StatusResource
{
    private ConstantsService constantsService;
    private VelocityRequestContextFactory velocityRequestContextFactory;
    private ResourceUriBuilder uriBuilder;
    private JiraAuthenticationContext authContext;

    private StatusResource()
    {
        // needed for tools that work using reflection
    }

    public StatusResource(JiraAuthenticationContext authContext, ConstantsService constantsService, VelocityRequestContextFactory velocityRequestContextFactory, ResourceUriBuilder uriBuilder)
    {
        this.authContext = authContext;
        this.constantsService = constantsService;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.uriBuilder = uriBuilder;
    }

    /**
     * Returns a full representation of the Status having the given id.
     * 
     * @param statusId a numeric Status id
     * @param request a Request
     * @param uriInfo a UriInfo
     * @return a full representation of the Status
     *
     * @response.representation.200.qname
     *      status
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA issue status in JSON format.
     *
     * @response.representation.200.example
     *      {@link StatusBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if the requested issue status is not found, or the user does not have permission to view it.

     */
    @GET
    @Path ("{id}")
    public Response getStatus(@PathParam ("id") final String statusId, @Context Request request, @Context UriInfo uriInfo)
    {
        ServiceOutcome<Status> statusOutcome = constantsService.getStatusById(authContext.getLoggedInUser(), statusId);
        if (!statusOutcome.isValid())
        {
            throw new NotFoundWebException(ErrorCollection.of(statusOutcome.getErrorCollection()));
        }

        return Response.ok(createStatusBean(statusOutcome.getReturnedValue(), uriInfo)).cacheControl(never()).build();
    }

    private StatusBean createStatusBean(Status status, UriInfo uriInfo)
    {
        return StatusBean.fullBean(
                status.getName(),
                uriBuilder.build(uriInfo, StatusResource.class, status.getId()),
                velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl() + status.getIconUrl(),
                status.getDescTranslation()
        );
    }
}
