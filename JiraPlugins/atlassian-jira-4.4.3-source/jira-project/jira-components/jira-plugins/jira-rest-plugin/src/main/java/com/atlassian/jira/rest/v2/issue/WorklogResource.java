package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since v4.2
 */
@Path ("worklog")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class WorklogResource
{
    private WorklogService worklogService;
    private UserManager userManager;
    private JiraAuthenticationContext authenticationContext;
    private ContextI18n i18n;

    private WorklogResource(final UserManager userManager)
    {
        // this constructor used by tooling
        this.userManager = userManager;
    }

    public WorklogResource(final WorklogService worklogService, final JiraAuthenticationContext authenticationContext, final UserManager userManager, ContextI18n i18n)
    {

        this.worklogService = worklogService;
        this.authenticationContext = authenticationContext;
        this.userManager = userManager;
        this.i18n = i18n;
    }

    /**
     * Returns a work log.
     *
     * @param worklogId a String containing the work log id
     * @param uriInfo a UriInfo
     * @return a work log
     *
     * @response.representation.200.qname
     *      worklog
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the work log with the given id exists and the currently authenticated user has permission to
     *      view it. The returned response contains a full representation of the work log in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.WorklogBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the work log with the given id does not exist or if the currently authenticated user does not
     *      have permission to view it.
     */
    @GET
    @Path("{id}")
    public Response getWorklog(@PathParam ("id") final String worklogId, @Context UriInfo uriInfo)
    {
        try
        {
            final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(authenticationContext.getUser());
            final Worklog worklog = worklogService.getById(serviceContext, Long.parseLong(worklogId));
            if (worklog != null)
            {
                return Response.ok(WorklogBean.getWorklog(worklog, uriInfo, userManager)).cacheControl(never()).build();
            }
            else
            {
                final ErrorCollection errors = ErrorCollection.builder()
                        .addErrorCollection(serviceContext.getErrorCollection())
                        .build();
                return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
            }
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("worklog.service.error.no.worklog.for.id", worklogId)));
        }
    }
}
