/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.servlet;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.util.AttachmentUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

public class ViewThumbnailServlet extends ViewAttachmentServlet
{
    protected File getFileName(HttpServletRequest request, HttpServletResponse response) throws DataAccessException, PermissionException
    {
        Attachment attachment = getAttachment(attachmentPath(request));

        if (!hasPermissionToViewAttachment(getUserName(), attachment))
        {
            throw new PermissionException("You do not have permissions to view this attachment");
        }

        return AttachmentUtils.getThumbnailFile(attachment);
    }

    protected void setResponseHeaders(HttpServletRequest request, HttpServletResponse response)
    {
        Attachment attachment = getAttachment(attachmentPath(request));
        File thumbnailFile = AttachmentUtils.getThumbnailFile(attachment);
        // All thumbnail images are stored in JPEG format.
        response.setContentType(ThumbnailManager.MIME_TYPE.toString());
        response.setContentLength((int) thumbnailFile.length());
        response.setHeader("Content-Disposition", "inline; filename=" + thumbnailFile.getName() + ";");

        HttpResponseHeaders.cachePrivatelyForAboutOneYear(response);
    }
}
