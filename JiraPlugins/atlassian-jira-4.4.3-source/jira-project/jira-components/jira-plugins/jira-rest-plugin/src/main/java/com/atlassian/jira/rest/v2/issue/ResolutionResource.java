package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
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
 * @since 4.2
 */
@Path ("resolution")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class ResolutionResource
{
    private ConstantsManager constantsManager;
    private ContextI18n i18n;

    private ResolutionResource()
    {
        // this constructor used by tooling
    }

    public ResolutionResource(final ConstantsManager constantsManager, ContextI18n i18n)
    {
        this.constantsManager = constantsManager;
        this.i18n = i18n;
    }

    /**
     * Returns a resolution.
     *
     * @param id a String containing the resolution id
     * @param uriInfo a UriInfo
     *
     * @return a JIRA issue resolution
     *
     * @response.representation.200.qname
     *      resolution
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the resolution exists and the user has permission to view it. Contains a full representation of
     *      the resolution in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.ResolutionBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the resolution does not exist or the user does not have permission to view it.
     */
    @GET
    @Path ("{id}")
    public Response getResolution(@PathParam ("id") final String id, @Context UriInfo uriInfo)
    {
        final Resolution resolution = constantsManager.getResolutionObject(id);
        if (resolution == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.resolution.error.not.found", id)));
        }

        return Response.ok(ResolutionBean.fullBean(resolution, uriInfo)).cacheControl(never()).build();
    }
}