package com.atlassian.jira.web.servlet;

import com.atlassian.jira.avatar.TemporaryAvatar;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.jira.web.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Streams out an avatar image that has just been uploaded so that cropping/scaling operations can be performed.
 *
 * @since v4.0
 */
public class ViewTemporaryAvatarServlet extends ViewProjectAvatarServlet
{
    private static final int BUFFER_SIZE = 8192;

    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        final TemporaryAvatar temporaryAvatar = (TemporaryAvatar) request.getSession().getAttribute(SessionKeys.TEMP_AVATAR);
        if (temporaryAvatar == null || !temporaryAvatar.getFile().exists())
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setHeader("Expires", "Fri, 01 Jan 1990 00:00:00 GMT");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-control", "no-cache, must-revalidate");
        response.setContentType(temporaryAvatar.getContentType());
        final OutputStream out = response.getOutputStream();
        boolean bytesWritten = false;
        try
        {
            IOUtil.copy(new FileInputStream(temporaryAvatar.getFile()), out, BUFFER_SIZE);
            bytesWritten = true;
        }
        catch (IOException e)
        {
            handleOutputStreamingException(response, bytesWritten, e);
        }
    }
}
