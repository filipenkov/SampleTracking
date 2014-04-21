package com.atlassian.jira.portal.portlets;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;

/**
 * Portlets should extend this portlet if they should not be displayed to anonymous users.
 * This is usually because the portlet displays information that depends on a remote user.
 */
public abstract class AbstractRequiresUserPortlet extends PortletImpl
{
    public AbstractRequiresUserPortlet(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager, ApplicationProperties applicationProperties)
    {
        super(authenticationContext, permissionManager, applicationProperties);
    }

    public String getViewHtml(PortletConfiguration portletConfiguration)
    {
        if (authenticationContext.getUser() == null)
            return "";
        else
            return super.getViewHtml(portletConfiguration);
    }
}
