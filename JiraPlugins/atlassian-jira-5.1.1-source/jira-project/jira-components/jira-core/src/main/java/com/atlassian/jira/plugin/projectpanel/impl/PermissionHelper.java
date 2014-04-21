package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

/**
 * This class is a helper class that at the moment has only one method that is used by Components and Versions project
 * tab panel classes ({@link ComponentsProjectTabPanel}, {@link VersionsProjectTabPanel}).
 */
class PermissionHelper
{
    private final PermissionManager permissionManager;

    public PermissionHelper(PermissionManager permissionManager)
    {
        this.permissionManager = permissionManager;
    }

    /**
     * This is a helper method that returns {@link Boolean#TRUE} if the given user is granted global administration
     * rights ({@link com.atlassian.jira.security.Permissions#ADMINISTER}) or administration rights for given project
     * ({@link com.atlassian.jira.security.Permissions#PROJECT_ADMIN}), otherwise returns {@link Boolean#FALSE}.
     *
     * @param user    user
     * @param project project
     * @return true if user is granted permission, false otherwise
     */
    public Boolean hasProjectAdminPermission(User user, Project project)
    {
        return Boolean.valueOf(permissionManager.hasPermission(Permissions.ADMINISTER, user)
                || permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user));
    }

}
