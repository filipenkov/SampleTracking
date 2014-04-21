package com.atlassian.jira.web.servlet;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.action.ActionContextKit;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.portal.Portlet;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletConfigurationManager;
import com.atlassian.jira.util.I18nHelper;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class LazyLoadingPortletServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(LazyLoadingPortletServlet.class);
    private static final String CONTACT_ADMIN = "We were unable to render this portlet due to an internal error. Please contact your JIRA Administrator. <br/>";

    protected final void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    {
        Long portletId = null;
        PortletConfiguration portletConfiguration = null;
        Portlet portletCC = null;
        final String portletIdString = httpServletRequest.getParameter("portletId");
        try
        {
            portletId = new Long(portletIdString);
            PortletConfigurationManager portletConfigurationManager = getPortletConfigurationManager();
            portletConfiguration = portletConfigurationManager.getByPortletId(portletId);
            if (portletConfiguration != null)
            {
                portletCC = portletConfiguration.getPortlet();
            }
            else
            {
                log.warn("LazyLoadingPortletServlet.service: Error while finding the portlet from the request with id " + portletId);
            }
        }
        catch (NumberFormatException e)
        {
            log.warn("LazyLoadingPortletServlet.service: Error while finding the portlet from the request with id '" + portletIdString + "': " + e, e);
        }
        try
        {
            // HACK!!! We need a context so any portlet code can access the webwork state (i.e. ActionContext,
            // ServletActionContext)
            //
            // Ensure we build a new ActionContext for this thread. The ServletActionContext.setContext() method does not
            // build a new ActionContext but simply populates an existing one with given values. If the 'existing' action
            // context has been built in the main application server thread, other HTTP processing threads could have
            // reference to the existing ActionContext object (as ActionContext uses an InheritableThreadLocal instead of a
            // regular ThreadLocal).
            //
            // The app server's main thread usually starts the pool of threads which will be used to
            // process HTTP requests. Therefore if the 'existing' ActionContext object has been built by the main thread
            // before it launches the HTTP processing threads, all the HTTP processing threads will have a reference to
            // the same instance of ActionContext object. (i.e. These threads have inherited a reference to the same
            // instance of ActionContext).
            //
            // Therefore, simply populating an ActionContext without building a new one can populate the ActionContext of
            // other threads with this thread's Request and Response object. We do not want to do this.
            //
            // To ensure that we will be populating an instance of ActionContext which only this thread has reference to,
            // build a new instance of ActionContext and then populate it. Fixes JRA-11859
            ActionContext.setContext(new ActionContext());
            ActionContextKit.setContext(httpServletRequest, httpServletResponse, this.getServletContext());
            httpServletResponse.setContentType("text/html");

            String page = portletCC != null ? portletCC.getViewHtml(portletConfiguration) : getErrorHtml(portletIdString);

            if (page != null && page.startsWith("An error occurred whilst rendering this message"))
            {
                String errorMsg = getErrorMessage(portletConfiguration);
                log.error(errorMsg);

                page =  CONTACT_ADMIN + errorMsg + "<br/>" + page;
            }

            httpServletResponse.addHeader("elementId", "portletcc-" + portletId);
            httpServletResponse.getWriter().print(page);
        }
        catch (Exception e)
        {
            log.error("LazyLoadingPortletServlet.service: Error while rendering velocity template" + e, e);
        }
        finally
        {
            // Create a new context to ANTI-HACK the HACK
            ActionContextKit.resetContext();
        }
    }

    private String getErrorMessage(PortletConfiguration config)
    {
        if (config == null)
        {
            return "Exception occured.  May be due to null porltet configuration";
        }
        final Portlet portlet = config.getPortlet();
        final String portletName = portlet != null ? portlet.getName() : null;

        return "Exception thrown while rendering portlet of type '" + portletName + "' with id of '" + config.getId() + "' on dashboard with id '" + config.getDashboardPageId() + "'";

    }

    PortletConfigurationManager getPortletConfigurationManager()
    {
        return ComponentManager.getComponentInstanceOfType(PortletConfigurationManager.class);
    }

    String getErrorHtml(String portletId)
    {
        StringBuffer sb = new StringBuffer();
        I18nHelper i18n = getI18nBean();
        return sb.append("<div class='errorbox'><b><span class='errLabel'>")
                .append(i18n.getText("admin.common.words.warning").toUpperCase())
                .append(":</span>")
                .append("</b><br/>")
                .append(i18n.getText("portlet.no.longer.exists", TextUtils.htmlEncode(portletId)))
                .append("</div>")
                .toString();
    }

    I18nHelper getI18nBean()
    {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
    }
}