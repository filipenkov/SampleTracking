package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since 4.2
 */
@Path ("comment")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class CommentResource
{
    private CommentService commentService;
    private JiraAuthenticationContext authContext;
    private ContextI18n i18n;

    @SuppressWarnings ({ "UnusedDeclaration" })
    private CommentResource()
    {
        // this constructor used by tooling
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    public CommentResource(final CommentService commentService, final JiraAuthenticationContext authContext, ContextI18n i18n)
    {
        this.authContext = authContext;
        this.commentService = commentService;
        this.i18n = i18n;
    }

    /**
     * Returns a single issue comment.
     *
     * @param id the ID of the comment to request
     * @param uriInfo used to build the self URI
     * @param render true if text fields should be rendered according to the renderer associated with them; false to return the raw, unrendered data
     * @return a Response containing a CommentBean
     *
     * @response.representation.200.qname
     *      comment
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA comment in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.CommentBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if the requested comment is not found, or the user does not have permission to view it.

     */
    @GET
    @Path ("{id}")
    public Response getComment(@PathParam ("id") final String id, @Context UriInfo uriInfo, @QueryParam ("render") @DefaultValue ("true") final boolean render)
    {
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        try
        {
            final Comment comment = commentService.getCommentById(authContext.getUser(), Long.parseLong(id), errorCollection);
            if (errorCollection.hasAnyErrors())
            {
                final ErrorCollection errors = ErrorCollection.builder()
                        .addErrorCollection(errorCollection)
                        .build();

                return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
            }
            else
            {
                return Response.ok(new CommentBean(comment, uriInfo)).cacheControl(never()).build();
            }
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("comment.service.error.no.comment.for.id", id)));
        }
    }
}