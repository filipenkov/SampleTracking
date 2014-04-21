package com.atlassian.jira.multitenant;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.multitenant.AuthorisationProvider;

import javax.servlet.http.HttpServletRequest;

/**
 * Checks authorisation for the current user
 *
 * @since 4.3
 */
public class JiraMultiTenantAuthorisationProvider implements AuthorisationProvider
{
    @Override
    public boolean canManageTenants(HttpServletRequest request)
    {
        // Only sysadmins can manage tenants
        User user = getAuthenticationContext().getLoggedInUser();
        return user != null && getPermissionManager().hasPermission(Permissions.SYSTEM_ADMIN, user);
    }

    private JiraAuthenticationContext getAuthenticationContext()
    {
        return ComponentAccessor.getJiraAuthenticationContext();
    }

    private PermissionManager getPermissionManager()
    {
        return ComponentAccessor.getPermissionManager();
    }
}
