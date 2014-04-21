/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.servlet;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.NotFoundException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.util.http.JiraHttpUtils;
import com.atlassian.jira.web.exception.WebExceptionChecker;
import com.atlassian.seraph.auth.AuthenticationContextImpl;
import com.atlassian.seraph.util.RedirectUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractViewFileServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(ViewAttachmentServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            File attachmentFile;
            try
            {
                attachmentFile = getFileName(request, response);
            }
            catch (InvalidAttachmentPathException e)
            {
                response.sendError(400, "Invalid attachment path");
                return;
            }
            catch (AttachmentNotFoundException nfe)
            {
                send404(request, response);
                return;
            }
            catch (PermissionException e)
            {
                redirectForSecurityBreach(request, response);
                return;
            }
            // Should be obsolete:
            if (attachmentFile == null)
            {
                throw new NotFoundException("Attachment not found");
            }

            streamFileData(request, response, attachmentFile);
        }
        catch (Exception e)
        {
            if (WebExceptionChecker.canBeSafelyIgnored(e))
            {
                return;
            }
            log.error("Error serving file for path " + request.getPathInfo() + ": " + e.getMessage(), e);
            throw new ServletException(e);
        }
    }

    private void send404(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.sendError(404, String.format("Attachment %s was not found", request.getPathInfo()));
    }

    private void redirectForSecurityBreach(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        if (getUserName() != null)
        {
            RequestDispatcher rd = request.getRequestDispatcher("/secure/views/securitybreach.jsp");
            JiraHttpUtils.setNoCacheHeaders(response);
            rd.forward(request, response);
        }
        else
        {
            response.sendRedirect(RedirectUtils.getLoginUrl(request));
        }
    }

    private void streamFileData(final HttpServletRequest request, final HttpServletResponse response, final File attachmentFile)
            throws IOException
    {
        InputStream in = null;
        OutputStream out = null;
        try
        {
            try
            {
                in = new FileInputStream(attachmentFile);
            }
            catch (FileNotFoundException e)
            {
                log.error("Error finding " + request.getPathInfo() + " : " + e.getMessage());
                // the outcome of this will only be complete if nothing else has written to the OutputStream, so we must
                // do this before setResponseHeaders() is called
                send404(request, response);
                return;
            }
            out = response.getOutputStream();

            // can only set headers after knowing that we have the file - otherwise we can't do response.sendError()
            setResponseHeaders(request, response);

            try
            {
                IOUtils.copy(in, out);
                out.flush();
            }
            catch (IOException e)
            {
                // we suspect this to be a Broken Pipe exception, probably due to the user closing the connection by pressing
                // the stop button in their browser, which we don't really care about logging
                if (log.isDebugEnabled())
                {
                    log.debug("Error serving content to client", e);
                }
            }
        }
        finally
        {
            close(in);
            close(out);
        }
    }

    /**
     * Validates that path is valid attachment path.
     *
     * @param request HTTP request
     * @return attachment path
     */
    protected final String attachmentPath(final HttpServletRequest request)
    {
        String pi = request.getPathInfo();
        if (pi == null || pi.length() == 1 || pi.indexOf('/', 1) == -1)
        {
            throw new InvalidAttachmentPathException();
        }
        return pi;
    }

    /**
     * Gets the attachment file (not the file name) that corresponds to the requested attachment.
     *
     * @param request the http request.
     * @param response the http response.
     * @return the File resource for the attachment.
     * @throws DataAccessException If there is a problem looking up the data to support the attachment.
     * @throws IOException if there is a problem getting the File.
     * @throws PermissionException if the user has insufficient permission to see the attachment.
     * @throws InvalidAttachmentPathException if the path to the attachment was invalid in some way.
     */
    protected abstract File getFileName(HttpServletRequest request, HttpServletResponse response)
            throws InvalidAttachmentPathException, DataAccessException, IOException, PermissionException;

    /**
     * Sets any required headers on the http response.
     *
     * @param request HTTP request
     * @param response HTTP response
     */
    protected abstract void setResponseHeaders(HttpServletRequest request, HttpServletResponse response)
            throws InvalidAttachmentPathException, DataAccessException, IOException;

    /**
     * @return The logged-in user's name, or null (anonymous)
     */
    protected final String getUserName()
    {
        User user = (User) new AuthenticationContextImpl().getUser();
        return (user != null ? user.getName() : null);
    }

    private static void close(Closeable close)
    {
        if (close != null)
        {
            try
            {
                close.close();
            }
            catch (IOException e)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Error closing streams after serving content", e);
                }
            }
        }
    }
}
