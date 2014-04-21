package com.atlassian.jira.bc.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

/**
 * Represents the different actions a users wants to perform on a project.
 */
public enum ProjectAction
{
    /**
     * The user is able to see the project. This does not mean the user can edit the project or even view its issues.
     */
    VIEW_PROJECT(new int[]{Permissions.ADMINISTER, Permissions.BROWSE, Permissions.PROJECT_ADMIN},
            "admin.errors.project.no.view.permission"),

    /**
     * Able to view the issues for the passed project.
     */
    VIEW_ISSUES(new int[]{Permissions.BROWSE},
            "admin.errors.project.no.browse.permission"),

    /**
     * Able to configure the project project specific configuration.
     */
    EDIT_PROJECT_CONFIG(new int[]{Permissions.ADMINISTER, Permissions.PROJECT_ADMIN},
            "admin.errors.project.no.config.permission");

    private final int[] permissions;
    private final String errorKey;

    private ProjectAction(int[] permissions, String errorKey)
    {
        this.permissions = permissions;
        this.errorKey = errorKey;
    }

    public int[] getPermissions()
    {
        return permissions;
    }

    public String getErrorKey()
    {
        return errorKey;
    }

    public boolean hasPermission(PermissionManager manager, User user, Project project)
    {
        for (int permission : permissions)
        {
            if (Permissions.isGlobalPermission(permission))
            {
                if (manager.hasPermission(permission, user))
                {
                    return true;
                }
            }
            else
            {
                if (manager.hasPermission(permission, project, user))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
