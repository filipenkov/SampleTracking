package com.atlassian.jira.portal.portlets;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;

import java.util.Map;

/**
 * This portlet displays links to user's reported issues, watched and voted issues.
 *
 * @since v3.13
 */
public class UserIssuesPortlet extends PortletImpl
{
    public UserIssuesPortlet(final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager, final ApplicationProperties applicationProperties)
    {
        super(authenticationContext, permissionManager, applicationProperties);
    }

    protected Map /* <String, Object> */getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        final Map velocityParams = super.getVelocityParams(portletConfiguration);
        velocityParams.put("voting", Boolean.valueOf(applicationProperties.getOption("jira.option.voting")));
        velocityParams.put("watching", Boolean.valueOf(applicationProperties.getOption("jira.option.watching")));
        return velocityParams;
    }
}
