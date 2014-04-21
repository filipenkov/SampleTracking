package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static com.atlassian.jira.rest.v2.issue.VelocityRequestContextFactories.getBaseURI;

/**
 * @since 4.2
 */
@Path ("issueType")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class IssueTypeResource
{
    private JiraAuthenticationContext authContext;
    private ConstantsService constantsService;
    private VelocityRequestContextFactory velocityRequestContextFactory;
    private ContextUriInfo contextUriInfo;

    private IssueTypeResource()
    {
        // this constructor used by tooling
    }

    public IssueTypeResource(JiraAuthenticationContext authContext, final ConstantsService constantsService, final VelocityRequestContextFactory velocityRequestContextFactory, ContextUriInfo contextUriInfo)
    {
        this.authContext = authContext;
        this.constantsService = constantsService;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.contextUriInfo = contextUriInfo;
    }

    /**
     * Returns a full representation of the issue type that has the given id.
     *
     * @param issueTypeId a String containing an issue type id
     * @return a full representation of the issue type with the given id
     *
     * @response.representation.200.qname
     *      issueType
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the issue type exists and is visible by the calling user.
     *
     * @response.representation.404.doc
     *      Returned if the issue type does not exist, or is not visible to the calling user.
     */
    @GET
    @Path ("{id}")
    public Response getIssueType(@PathParam ("id") final String issueTypeId)
    {
        ServiceOutcome<IssueType> outcome = constantsService.getIssueTypeById(authContext.getUser(), issueTypeId);
        if (!outcome.isValid())
        {
            throw new NotFoundWebException(ErrorCollection.of(outcome.getErrorCollection()));
        }

        Response.ResponseBuilder rb = Response.ok(new IssueTypeBeanBuilder()
                .baseURI(getBaseURI(velocityRequestContextFactory))
                .context(contextUriInfo)
                .issueType(outcome.getReturnedValue())
                .build()
        );

        return rb.cacheControl(never()).build();
    }
}
