package com.atlassian.jira.plugin.webwork;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.action.ActionContextKit;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.google.common.annotations.VisibleForTesting;
import com.opensymphony.util.TextUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.VelocityException;
import webwork.view.velocity.VelocityHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static com.atlassian.jira.component.ComponentAccessor.getComponent;
import static com.atlassian.jira.template.TemplateSources.file;

/**
 * A servlet that displays a velocity view, but also allows you to load that view from the classpath, using Atlassian's
 * normal velocity loading.
 */
public class JiraPluginWebworkVelocityServlet extends HttpServlet
{
    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException
    {
        ActionContextKit.setContext(httpServletRequest, httpServletResponse, getServletContext());
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

            final Map<String, Object> velocityParams = getDefaultVelocityParams();
            velocityParams.put("i18n", getAuthenticationContext().getI18nHelper());
            final VelocityContext context = WebWorkVelocityContext.create(httpServletRequest, httpServletResponse, velocityParams);
            final PrintWriter writer = httpServletResponse.getWriter();
            try
            {
                httpServletResponse.setContentType(getApplicationProperties().getContentType());
                final String body = getTemplatingEngine().render(file(servletPath)).applying(context).asHtml();
                writer.write(body);
            }
            catch (VelocityException e)
            {
                writer.write("Exception rendering velocity file " + TextUtils.htmlEncode(servletPath));
                writer.write("<br><pre>");
                StringWriter stringWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(stringWriter));
                writer.write(TextUtils.htmlEncode(stringWriter.toString()));
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

    private static class WebWorkVelocityContext
    {
        private static VelocityContext create(final HttpServletRequest request, final HttpServletResponse response,
                final Map<String, Object> parameters)
        {
            return (VelocityContext) VelocityHelper.getContextWithoutInit(request, response, parameters);
        }
    }

    @VisibleForTesting
    Map<String, Object> getDefaultVelocityParams()
    {
        return JiraVelocityUtils.getDefaultVelocityParams(getAuthenticationContext());
    }

    @VisibleForTesting
    JiraAuthenticationContext getAuthenticationContext()
    {
        return ComponentAccessor.getJiraAuthenticationContext();
    }

    @VisibleForTesting
    ApplicationProperties getApplicationProperties()
    {
        return ComponentManager.getComponent(ApplicationProperties.class);
    }
    
    @VisibleForTesting
    VelocityTemplatingEngine getTemplatingEngine()
    {
        return getComponent(VelocityTemplatingEngine.class);
    }
}
