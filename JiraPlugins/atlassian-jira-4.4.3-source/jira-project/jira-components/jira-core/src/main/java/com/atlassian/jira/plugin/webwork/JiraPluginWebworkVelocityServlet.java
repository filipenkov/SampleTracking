package com.atlassian.jira.plugin.webwork;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.action.ActionContextKit;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.velocity.VelocityManager;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;
import webwork.view.velocity.VelocityHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * A servlet that displays a velocity view, but also allows you to load that view from the classpath, using Atlassian's
 * normal velocity loading.
 */
public class JiraPluginWebworkVelocityServlet extends HttpServlet
{
    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException
    {
        ActionContextKit.setContext(httpServletRequest, httpServletResponse, this.getServletContext());
        try
        {

            String servletPath = (String) httpServletRequest.getAttribute("javax.servlet.include.servlet_path");
            if (servletPath == null)
            {
                servletPath = httpServletRequest.getServletPath();
            }

            if (servletPath != null && servletPath.indexOf("/") == 0)
            {
                servletPath = servletPath.substring(1); // trim leading slash
            }

            ApplicationProperties applicationProperties = ComponentManager.getComponent(ApplicationProperties.class);

            Map velocityParams = JiraVelocityUtils.getDefaultVelocityParams(ComponentAccessor.getJiraAuthenticationContext());
            velocityParams.put("i18n", ComponentAccessor.getJiraAuthenticationContext().getI18nHelper());
            final Context context = VelocityHelper.getContextWithoutInit(httpServletRequest, httpServletResponse, velocityParams);
            final PrintWriter writer = httpServletResponse.getWriter();
            try
            {
                httpServletResponse.setContentType(applicationProperties.getContentType());
                VelocityManager velocityManager = ComponentAccessor.getVelocityManager();
                String body = velocityManager.getEncodedBody("", servletPath, null, applicationProperties.getEncoding(), context);
                writer.write(body);
            }
            catch (VelocityException e)
            {
                writer.write("Exception rendering velocity file " + servletPath);
                writer.write("<br><pre>");
                e.printStackTrace(writer);
                writer.write("</pre>");
            }
        }
        finally
        {
            //
            // Ideally we would cleanup the ActionContext however we cant tell if we have been invoked as an full outer request
            // or as a a redirected inner request that must run yet in a decorator and so on.  So until we can tell the difference
            // we cant clean up this ActionContext here.  See JRA-8009 for the effects of bad ActionContexts.
            //
            //ActionContextKit.resetContext();
        }

    }
}
