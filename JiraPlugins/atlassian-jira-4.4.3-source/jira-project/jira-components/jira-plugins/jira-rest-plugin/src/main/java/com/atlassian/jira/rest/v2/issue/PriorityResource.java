package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
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
@Path ("priority")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class PriorityResource
{
    private ConstantsManager constantsManager;
    private VelocityRequestContextFactory velocityRequestContextFactory;
    private ContextI18n i18n;

    private PriorityResource()
    {
        // this constructor used by tooling
    }

    public PriorityResource(final ConstantsManager constantsManager, final VelocityRequestContextFactory velocityRequestContextFactory, ContextI18n i18n)
    {
        this.constantsManager = constantsManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.i18n = i18n;
    }

    /**
     * Returns an issue priority.
     *
     * @param id a String containing the priority id
     * @param request a Request
     * @param uriInfo a UriInfo
     *
     * @return a response containing the requested issue priority
     *
     * @response.representation.200.qname
     *      issuePriority
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the issue priority exists and is visible by the calling user. Contains a full representation of
     *      the issue priority in JSON.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.PriorityBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the issue priority does not exist or is not visible to the calling user.
     */
    @GET
    @Path ("{id}")
    public Response getPriority(@PathParam ("id") final String id, @Context Request request, @Context UriInfo uriInfo)
    {
        final Priority priority = constantsManager.getPriorityObject(id);
        if (priority == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.priority.error.not.found", id)));
        }

        return Response.ok(PriorityBean.fullBean(priority, uriInfo, velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl())).cacheControl(never()).build();
    }
}