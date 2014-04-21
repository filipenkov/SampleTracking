/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.permission;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.ProjectPermissionSchemeHelper;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.List;
import java.util.Map;

/**
 * This class is used to display all permissions for a particular permission scheme.
 * It is used for the Edit Permissions page
 */
@WebSudoRequired
public class EditPermissions extends SchemeAwarePermissionAction
{

    private final SchemePermissions schemePermissions;
    private final ProjectPermissionSchemeHelper helper;
    private String usersGroupsRolesHeaderText;
    private List<Project> projects;

    public EditPermissions(final SchemePermissions schemePermissions, final ProjectPermissionSchemeHelper helper)
    {
        this.schemePermissions = schemePermissions;
        this.helper = helper;
    }

    /**
     * Get a map of the permission events that can be part of a permission scheme. This map contains the permission id and the permission name
     * @return Map containing the permissions
     * @see SchemePermissions
     */
    public Map getSchemePermissions()
    {
        return schemePermissions.getSchemePermissions();
    }

    public Map getProjectPermissions()
    {
        return schemePermissions.getProjectPermissions();
    }

    public Map getIssuePermissions()
    {
        return schemePermissions.getIssuePermissions();
    }

    public Map getVotersAndWatchersPermissions()
    {
        return schemePermissions.getVotersAndWatchersPermissions();
    }

    public Map getTimeTrackingPermissions()
    {
        return schemePermissions.getTimeTrackingPermissions();
    }

    public Map getCommentsPermissions()
    {
        return schemePermissions.getCommentsPermissions();
    }

    public Map getAttachmentsPermissions()
    {
        return schemePermissions.getAttachmentsPermissions();
    }

    public String getI18nUsersGroupsRolesHeader()
    {
        if (usersGroupsRolesHeaderText == null)
        {
            usersGroupsRolesHeaderText = getText("admin.common.words.users.groups.roles");
        }
        return usersGroupsRolesHeaderText;
    }

    /**
     * Get all Generic Value permission records for a particular scheme and permission Id
     * @param permission The Id of the permission
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     * @see PermissionSchemeManager
     */
    public List getPermissions(Long permission) throws GenericEntityException
    {
        return getSchemeManager().getEntities(getScheme(), permission);
    }

    /**
     * Gets the description for the permission
     * @param id Id of the permission that you want to get the description for
     * @return String containing the description
     * @see SchemePermissions
     */
    public String getPermissionDescription(int id)
    {
        return schemePermissions.getPermissionDescription(id);
    }

    public SchemeManager getSchemeManager()
    {
        return ManagerFactory.getPermissionSchemeManager();
    }

    public String getRedirectURL()
    {
        return null;
    }

    public List<Project> getUsedIn()
    {
        if (projects == null)
        {
            final Scheme permissionScheme = getSchemeObject();
            projects = helper.getSharedProjects(permissionScheme);
        }
        return projects;
    }

}
