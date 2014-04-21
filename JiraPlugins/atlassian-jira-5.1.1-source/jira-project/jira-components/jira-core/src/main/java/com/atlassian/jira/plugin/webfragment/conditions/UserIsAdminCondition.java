package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

/**
 * Checks if this user has the global admin permission  
 */
public class UserIsAdminCondition extends AbstractJiraCondition
{
    private final PermissionManager permissionManager;

    public UserIsAdminCondition(PermissionManager permissionManager)
    {
        this.permissionManager = permissionManager;
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, user);
    }
}
