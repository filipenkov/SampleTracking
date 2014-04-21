package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public class ProjectQuickSearchHandler extends SingleWordQuickSearchHandler
{
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;

    public ProjectQuickSearchHandler(ProjectManager projectManager, PermissionManager permissionManager, JiraAuthenticationContext authenticationContext)
    {
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
    }

    protected Map handleWord(String word, QuickSearchResult searchResult)
    {
        if (word == null)
            return null;

        GenericValue projectByKey = projectManager.getProjectByKey(word.toUpperCase());
        if (projectByKey != null && hasPermissionToViewProject(projectByKey))
        {
            return EasyMap.build("pid", projectByKey.getString("id"));
        }

        GenericValue projectByName = projectManager.getProjectByName(word);
        if (projectByName != null && hasPermissionToViewProject(projectByName))
        {
            return EasyMap.build("pid", projectByName.getString("id"));
        }

        return null;
    }

    private boolean hasPermissionToViewProject(GenericValue projectByKey)
    {
        return permissionManager.hasPermission(Permissions.BROWSE, projectByKey, authenticationContext.getUser());
    }

}
