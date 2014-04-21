package com.atlassian.jira.web.servlet.rpc;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.action.ActionContextKit;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class XmlRpcServlet extends HttpServlet
{
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        final ApplicationProperties applicationProperties = ManagerFactory.getApplicationProperties();
        if (applicationProperties.getOption(APKeys.JIRA_OPTION_RPC_ALLOW))
        {
            if (applicationProperties.getString(APKeys.JIRA_SETUP) != null)
            {
                try
                {
                    ActionContextKit.resetContext();
                    executeRequest(request, response);
                }
                finally
                {
                    ActionContextKit.resetContext();
                }
            }
        }
        else
        {
            throw new ServletException("Remote API not activated for this JIRA installation.");
        }
    }

    /**
     * Called to actually execute the XMLRPC request.
     *
     * @param request  the request in play
     * @param response the response in play
     *
     * @throws IOException if stuff goes wrong
     */
    private void executeRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException
    {
        byte[] result =  ComponentManager.getComponentInstanceOfType(XmlRpcRequestProcessor.class).process(request.getInputStream());
        response.setContentType("text/xml");
        response.setContentLength(result.length);

        OutputStream out = response.getOutputStream();
        out.write(result);
        out.flush();
    }

}
