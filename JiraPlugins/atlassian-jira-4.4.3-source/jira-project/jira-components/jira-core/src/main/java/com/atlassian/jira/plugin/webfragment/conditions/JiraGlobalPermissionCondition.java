package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;
import com.opensymphony.user.User;

/**
 * Checks if the user has the global permission: {@link AbstractJiraPermissionCondition#permission}
 */
public class JiraGlobalPermissionCondition extends AbstractJiraPermissionCondition
{
    public JiraGlobalPermissionCondition(PermissionManager permissionManager)
    {
        super(permissionManager);
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return permissionManager.hasPermission(permission, user);
    }
}
