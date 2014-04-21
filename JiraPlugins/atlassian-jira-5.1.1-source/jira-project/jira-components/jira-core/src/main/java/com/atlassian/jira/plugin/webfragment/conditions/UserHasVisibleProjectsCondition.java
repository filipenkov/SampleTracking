package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;

/**
 * Checks if the user can see atleast one project with this {@link AbstractJiraPermissionCondition#permission}
 */
public class UserHasVisibleProjectsCondition extends AbstractJiraPermissionCondition
{
    public UserHasVisibleProjectsCondition(PermissionManager permissionManager)
    {
        super(permissionManager);
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        try
        {
            return permissionManager.hasProjects(permission, user);
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
