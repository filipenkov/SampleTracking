package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.SessionKeys;
import com.opensymphony.user.User;

import javax.servlet.http.HttpServletRequest;

/**
 * Checks that the current user is a project admin for atleast one project
 */
public class UserIsProjectAdminCondition extends AbstractJiraCondition
{
    private final PermissionManager permissionManager;

    public UserIsProjectAdminCondition(PermissionManager permissionManager)
    {
        this.permissionManager = permissionManager;
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        HttpServletRequest request = jiraHelper.getRequest();
        if (request == null)
        {
            try
            {
                return permissionManager.hasProjects(Permissions.PROJECT_ADMIN, user);
            }
            catch (Exception e)
            {
                return false;
            }
        }

        try
        {
            Boolean isProjectAdmin = (Boolean) request.getSession().getAttribute(SessionKeys.USER_PROJECT_ADMIN);
            if (isProjectAdmin == null)
            {
                if (user != null)
                {
                    isProjectAdmin = permissionManager.hasProjects(Permissions.PROJECT_ADMIN, user) ? Boolean.TRUE : Boolean.FALSE;
                    request.getSession().setAttribute(SessionKeys.USER_PROJECT_ADMIN, isProjectAdmin);
                }
            }

            return isProjectAdmin != null && isProjectAdmin.booleanValue();
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
