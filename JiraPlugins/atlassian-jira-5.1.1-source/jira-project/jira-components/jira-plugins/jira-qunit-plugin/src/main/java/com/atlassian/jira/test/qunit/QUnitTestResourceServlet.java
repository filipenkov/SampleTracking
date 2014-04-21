package com.atlassian.jira.test.qunit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provides a means to download an arbitrary test javascript file from the plugin.
 * <p>
 * Possibly to be susceptible to directory traversal attacks with the right testName - but
 * this is for testing only.
 * <p>
 * No caching is performed, and resources can be found via the jvm -Dplugin.resource.directories
 * parameter for your development convenience.
 */
public class QUnitTestResourceServlet extends HttpServlet
{
    private ServletConfig config;

    @Override
    public void init(final ServletConfig config)
            throws ServletException
    {
        this.config = config;
        super.init(config);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException
    {
        String testName = StringUtils.removeStart(req.getPathInfo(), "/");
        InputStream resource = null;
        try
        {
            resource = config.getServletContext().getResourceAsStream("test/" + testName + ".js");
            byte[] content;
            if(resource == null)
            {
                content = ("<!-- ERROR: Unable to find test '" + testName + "' -->").getBytes("UTF-8");
            }
            else
            {
                content = IOUtils.toByteArray(resource);
            }
            resp.setContentType("text/javascript;charset=UTF-8");
            resp.getOutputStream().write(content);
        } finally {
            IOUtils.closeQuietly(resource);
        }
    }
}
