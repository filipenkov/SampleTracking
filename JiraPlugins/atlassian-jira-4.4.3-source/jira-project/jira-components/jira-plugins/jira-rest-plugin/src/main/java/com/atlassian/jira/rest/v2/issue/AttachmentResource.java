package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.builder.BeanBuilderFactory;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since v4.2
 */
@Path ("attachment")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class AttachmentResource
{
    private AttachmentManager attachmentManager;
    private AttachmentService attachmentService;
    private JiraAuthenticationContext authContext;
    private BeanBuilderFactory beanBuilderFactory;
    private ContextI18n i18n;
    private ContextUriInfo uriInfo;

    private AttachmentResource()
    {
        // this constructor used by tooling
    }

    public AttachmentResource(final AttachmentService attachmentService, final AttachmentManager attachmentManager, final JiraAuthenticationContext authContext, BeanBuilderFactory beanBuilderFactory, ContextI18n i18n, ContextUriInfo uriInfo)
    {
        this.attachmentManager = attachmentManager;
        this.attachmentService = attachmentService;
        this.authContext = authContext;
        this.beanBuilderFactory = beanBuilderFactory;
        this.i18n = i18n;
        this.uriInfo = uriInfo;
    }

    /**
     * Returns the meta-data for an attachment, including the URI of the actual attached file.
     *
     * @param id the attachment id
     * @return a JSON representation of an attachment
     *
     * @response.representation.200.qname
     *      attachment
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.AttachmentBean#DOC_EXAMPLE}
     *
     * @response.representation.200.doc
     *      Returns a JSON representation of the attachment meta-data. The representation does not contain the
     *      attachment itself, but contains a URI that can be used to download the actual attached file.
     *
     * @response.representation.404.doc
     *      Returned if the attachment with the given id does not exist, or is not accessible by the calling user.
     */
    @GET
    @Path ("{id}")
    public Response getAttachment(@PathParam ("id") final String id)
    {
        if (!attachmentManager.attachmentsEnabled())
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(never()).build();
        }

        final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(authContext.getLoggedInUser());
        try
        {
            final Attachment attachment = attachmentService.getAttachment(serviceContext, Long.parseLong(id));
            if (serviceContext.getErrorCollection().hasAnyErrors())
            {
                final ErrorCollection errors = ErrorCollection.builder()
                        .addErrorCollection(serviceContext.getErrorCollection())
                        .build();

                return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
            }
            else
            {
                AttachmentBean bean = beanBuilderFactory.attachmentBean(attachment).context(uriInfo).build();
                return Response.ok(bean).cacheControl(never()).build();
            }
        }
        catch (AttachmentNotFoundException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.attachment.error.not.found", id)));
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.attachment.error.not.found", id)));
        }
    }
}
