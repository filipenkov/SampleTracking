/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.servlet;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.util.BrowserUtils;
import com.atlassian.jira.util.JiraUrlCodec;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class ViewAttachmentServlet extends AbstractViewFileServlet
{
    private MimeSniffingKit mimeSniffingKit;
    
    public void init(ServletConfig servletConfig) throws ServletException
    {
        super.init(servletConfig);
    }

    MimeSniffingKit getMimeSniffingKit()
    {
        if (mimeSniffingKit == null)
        {
            mimeSniffingKit = ComponentAccessor.getComponent(MimeSniffingKit.class);
        }
        return mimeSniffingKit;
    }

    ApplicationProperties getApplicationProperties()
    {
        return ComponentManager.getComponentInstanceOfType(ApplicationProperties.class);
    }

    /**
     * Returns the file of the attachment.
     *
     * @param request  HTTP request
     * @param response HTTP response
     * @return attachment file
     * @throws DataAccessException if attachment or user cannot be retrieved due to some kind of db access problem.
     * @throws PermissionException if user is denied permission to see the attachment
     */
    protected File getFileName(HttpServletRequest request, HttpServletResponse response) throws DataAccessException, PermissionException
    {
        Attachment attachment = getAttachment(attachmentPath(request));

        if (!hasPermissionToViewAttachment(getUserName(), attachment))
        {
            throw new PermissionException("You do not have permissions to view this issue");
        }

        return AttachmentUtils.getAttachmentFile(attachment);
    }

    /**
     * Looks up the attachment by reading the id from the query string.
     *
     * @param query eg. '/10000/foo.txt'
     * @return attachment found
     */
    protected Attachment getAttachment(String query)
    {
        int x = query.indexOf('/', 1);
        final String idStr = query.substring(1, x);
        Long id;
        try
        {
            id = new Long(idStr);
        }
        catch (NumberFormatException e)
        {
            throw new AttachmentNotFoundException(idStr);
        }
        if (query.indexOf('/', x+1) != -1)
        {
            // JRA-14580. only one slash is allowed to prevent infinite recursion by web crawlers.
            throw new AttachmentNotFoundException(idStr);
        }

        return ComponentAccessor.getAttachmentManager().getAttachment(id);
    }

    /**
     * Sets the content type, content length and "Content-Disposition" header
     * of the response based on the values of the attachement found.
     *
     * @param request  HTTP request
     * @param response HTTP response
     */
    protected void setResponseHeaders(HttpServletRequest request, HttpServletResponse response) throws AttachmentNotFoundException, IOException
    {
        Attachment attachment = getAttachment(attachmentPath(request));
        response.setContentType(attachment.getMimetype());
        response.setContentLength(attachment.getFilesize().intValue());

        String userAgent = request.getHeader(BrowserUtils.USER_AGENT_HEADER);
        final ApplicationProperties ap = getApplicationProperties();
        String disposition = getContentDisposition(attachment, userAgent);

        final String codedName = JiraUrlCodec.encode(attachment.getFilename(), true);
        final String jiraEncoding = ap.getEncoding();
        // note the special *= syntax is used for embedding the encoding that the filename is in as per RFC 2231
        // http://www.faqs.org/rfcs/rfc2231.html
        response.setHeader("Content-Disposition", disposition + "; filename*=" + jiraEncoding + "''" + codedName + ";");

        HttpResponseHeaders.cachePrivatelyForAboutOneYear(response);
    }

    String getContentDisposition(final Attachment attachment, final String userAgent) throws IOException {
        return getMimeSniffingKit().getContentDisposition(attachment, userAgent);
    }


    /**
     * Checks if the given user had permission to see the attachemnt.
     *
     * @param username   username of the user who wants to see the attachment
     * @param attachment attachment to be checked
     * @return true if user can see the attachment, false otherwise
     * @throws DataAccessException if no such user exists.
     */
    protected boolean hasPermissionToViewAttachment(String username, Attachment attachment) throws DataAccessException
    {
        Issue issue = attachment.getIssueObject();
        if (username == null)
        {
            return (ComponentAccessor.getPermissionManager().hasPermission(Permissions.BROWSE, issue, null));
        }
        User user = UserUtils.getUser(username);
        if (user == null)
        {
            throw new DataAccessException("User '"+ username + "' not found");
        }
        return (ComponentAccessor.getPermissionManager().hasPermission(Permissions.BROWSE, issue, user));
    }
}

