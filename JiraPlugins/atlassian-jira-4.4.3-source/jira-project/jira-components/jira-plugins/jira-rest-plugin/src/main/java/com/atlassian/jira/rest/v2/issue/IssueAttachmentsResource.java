package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.plugins.rest.common.multipart.FilePart;
import com.atlassian.plugins.rest.common.multipart.MultipartConfig;
import com.atlassian.plugins.rest.common.multipart.MultipartConfigClass;
import com.atlassian.plugins.rest.common.multipart.MultipartFormParam;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.RequiresXsrfCheck;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;
import webwork.config.Configuration;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Issue attachments
 *
 * @since 4.3
 */
@Path ("issue/{issueKey}/attachments")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class IssueAttachmentsResource
{
    private static final Logger log = Logger.getLogger(IssueAttachmentsResource.class);

    private final JiraAuthenticationContext authContext;
    private final IssueManager issueManager;
    private final AttachmentService attachmentService;
    private final AttachmentManager attachmentManager;
    private final IssueUpdater issueUpdater;
    private final PermissionManager permissionManager;
    private final ContextI18n i18n;

    public IssueAttachmentsResource(JiraAuthenticationContext authContext, IssueManager issueManager, AttachmentService attachmentService, AttachmentManager attachmentManager, IssueUpdater issueUpdater, PermissionManager permissionManager, ContextI18n i18n)
    {
        this.authContext = authContext;
        this.issueManager = issueManager;
        this.attachmentService = attachmentService;
        this.attachmentManager = attachmentManager;
        this.issueUpdater = issueUpdater;
        this.permissionManager = permissionManager;
        this.i18n = i18n;
    }

    @Context
    private ServletContext servletContext;

    /**
     * Add one or more attachments to an issue.
     * <p>
     * This resource expects a multipart post. The media-type multipart/form-data is defined in RFC 1867. Most client
     * libraries have classes that make dealing with multipart posts simple. For instance, in Java the Apache HTTP Components
     * library provides a
     * <a href="http://hc.apache.org/httpcomponents-client-ga/httpmime/apidocs/org/apache/http/entity/mime/MultipartEntity.html">MultiPartEntity</a>
     * that makes it simple to submit a multipart POST.
     * <p>
     * In order to protect against XSRF attacks, because this method accepts multipart/form-data, it has XSRF protection
     * on it.  This means you must submit a header of X-Atlassian-Token: nocheck with the request, otherwise it will be
     * blocked.
     *
     * @param issueKey the issue that you want to add the attachments to
     * @param fileParts the files to add
     * @return no content
     *
     * @response.representation.204.doc
     *      Returned if successful.
     *
     * @response.representation.404.doc
     *      Returned if the requested issue is not found, the user does not have permission to view it, or if the
     *      attachments exceeds the maximum configured attachment size.
     *
     * @response.representation.403.doc
     *      Returned if attachments is disabled or if you don't have permission to add attachments to this issue.
     */
    @POST
    @Consumes (MediaType.MULTIPART_FORM_DATA)
    @MultipartConfigClass(JiraAttachmentMultipartConfig.class)
    @RequiresXsrfCheck
    public Response addAttachment(@PathParam ("issueKey") String issueKey, @MultipartFormParam ("file") Collection<FilePart> fileParts)
    {
        JiraServiceContext context = new JiraServiceContextImpl(authContext.getLoggedInUser());
        Issue issue = getIssueObject(issueKey);
        GenericValue issueGv;
        try
        {
            issueGv = issueManager.getIssue(issueKey);
        }
        catch (GenericEntityException gee)
        {
            // Ignore, it would have already been handled above
            return null;
        }

        if (!attachmentService.canCreateAttachments(context, issue))
        {
            ErrorCollection.Builder errorBuilder = ErrorCollection.Builder.newBuilder().addErrorMessage(
                    i18n.getText("attachment.service.error.create.no.permission"));
            throw new RESTException(Response.Status.FORBIDDEN, errorBuilder.build());
        }
        try
        {
            Collection<ChangeItemBean> beans = new ArrayList<ChangeItemBean>();
            for (FilePart filePart : fileParts)
            {
                ChangeItemBean bean = attachmentManager.createAttachment(getFileFromFilePart(filePart), filePart.getName(),
                        filePart.getContentType(), authContext.getLoggedInUser(), issueGv, Collections.<String, Object>emptyMap(),
                        UtilDateTime.nowTimestamp());
                beans.add(bean);
            }
            IssueUpdateBean issueUpdateBean = new IssueUpdateBean(issueGv, issueGv, EventType.ISSUE_UPDATED_ID,
                    authContext.getLoggedInUser());
            issueUpdateBean.setDispatchEvent(true);
            issueUpdateBean.setChangeItems(beans);
            issueUpdater.doUpdate(issueUpdateBean, true);
            return Response.noContent().build();
        }
        catch (Exception e)
        {
            log.error("Error saving attachment", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    private File getFileFromFilePart(FilePart filePart) throws IOException
    {
        File file = File.createTempFile("attachment-", ".tmp");
        file.deleteOnExit();
        filePart.write(file);
        return file;
    }

    private Issue getIssueObject(final String issueKey) throws NotFoundWebException
    {
        final User user = authContext.getUser();

        final MutableIssue issue = issueManager.getIssueObject(issueKey);
        if (issue == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("issue.does.not.exist.title")));
        }
        if (!permissionManager.hasPermission(Permissions.BROWSE, issue, user))
        {
            ErrorCollection.Builder errorBuilder = ErrorCollection.Builder.newBuilder().addErrorMessage(i18n.getText("admin.errors.issues.no.permission.to.see"));
            if (user == null)
            {
                errorBuilder.addErrorMessage(i18n.getText("login.required.title"));
                throw new RESTException(Response.Status.UNAUTHORIZED, errorBuilder.build());
            }
            else
            {
                throw new RESTException(Response.Status.FORBIDDEN, errorBuilder.build());
            }
        }

        return issue;
    }

    public static class JiraAttachmentMultipartConfig implements MultipartConfig
    {
        public long getMaxFileSize()
        {
            return getMaxAttachmentSize();
        }

        public long getMaxSize()
        {
            return getMaxAttachmentSize() * 10;
        }
    }

    private static Integer getMaxAttachmentSize()
    {
        Integer maxSize;
        try
        {
            String maxSizeStr = Configuration.getString(APKeys.JIRA_ATTACHMENT_SIZE);
            if (maxSizeStr != null)
            {
                try
                {
                    maxSize = new Integer(maxSizeStr);
                }
                catch (NumberFormatException e)
                {
                    maxSize = Integer.MAX_VALUE;
                    log.warn("Property '" + APKeys.JIRA_ATTACHMENT_SIZE + "' with value '" + maxSizeStr + "' is not a number. Defaulting to Integer.MAX_VALUE");
                }
            }
            else
            {
                maxSize = Integer.MAX_VALUE;
                log.warn("Property '" + APKeys.JIRA_ATTACHMENT_SIZE + "' is not set. Defaulting to Integer.MAX_VALUE");
            }
        }
        catch (IllegalArgumentException e1)
        {
            maxSize = Integer.MAX_VALUE;
            log.warn("Failed getting string from Configuration for '" + APKeys.JIRA_ATTACHMENT_SIZE + "' property. Defaulting to Integer.MAX_VALUE", e1);
        }
        return maxSize;
    }

}
