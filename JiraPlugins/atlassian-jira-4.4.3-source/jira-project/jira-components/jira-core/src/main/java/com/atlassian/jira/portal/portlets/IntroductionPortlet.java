package com.atlassian.jira.portal.portlets;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;

import java.util.Map;

/**
 * Portlet implementation that shows an introduction to this installation of JIRA.
 *
 * @since v3.13
 */
public class IntroductionPortlet extends PortletImpl
{
    public IntroductionPortlet(final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager, final ApplicationProperties applicationProperties)
    {
        super(authenticationContext, permissionManager, applicationProperties);
    }

    protected Map /* <String, Object> */getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        final Map params = super.getVelocityParams(portletConfiguration);
        params.put("introduction", applicationProperties.getText(APKeys.JIRA_INTRODUCTION));
        return params;
    }
}
