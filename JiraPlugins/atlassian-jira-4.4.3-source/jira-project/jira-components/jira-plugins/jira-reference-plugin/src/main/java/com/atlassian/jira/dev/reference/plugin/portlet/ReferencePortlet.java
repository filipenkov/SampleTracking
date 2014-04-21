package com.atlassian.jira.dev.reference.plugin.portlet;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;

import java.util.Map;

public class ReferencePortlet extends PortletImpl
{
    public ReferencePortlet(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager,
            ApplicationProperties applicationProperties)
    {
        super(authenticationContext, permissionManager, applicationProperties);
    }

    protected Map<String, Object> getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        Map<String,Object> result = super.getVelocityParams(portletConfiguration);
        result.put("i18n", authenticationContext.getI18nHelper());
        result.put("username", username());
        return result;
    }

    private String username()
    {
        User user = authenticationContext.getLoggedInUser();
        return user != null ? user.getDisplayName() : "<no user>";
    }

}
