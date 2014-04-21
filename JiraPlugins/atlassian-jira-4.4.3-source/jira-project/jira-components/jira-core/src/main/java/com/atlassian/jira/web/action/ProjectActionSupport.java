/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

public class ProjectActionSupport extends JiraWebActionSupport
{
    private Collection browseableProjects;

    protected final ProjectManager projectManager;
    private final PermissionManager permissionManager;


    public ProjectActionSupport(ProjectManager projectManager, PermissionManager permissionManager)
    {
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
    }

    //todo - remove this constructor as subclasses become pico aware
    public ProjectActionSupport()
    {
        this(ManagerFactory.getProjectManager(), ManagerFactory.getPermissionManager());
    }

    public Collection getBrowseableProjects()
    {
        if (browseableProjects == null)
        {
            browseableProjects = permissionManager.getProjects(Permissions.BROWSE, getRemoteUser());
        }
        return browseableProjects;
    }

    /**
     * Retrieves a list of projects belonging to the specified category that the user has permission to see
     *
     * @param category specify the category or null to retrieve a list of browseable projects that are not associated with any category
     * @return collection of project generic values
     * @throws GenericEntityException if cannot retrieve projects
     */
    public Collection<GenericValue> getBrowseableProjectsInCategory(GenericValue category) throws GenericEntityException
    {
        return permissionManager.getProjects(Permissions.BROWSE, getLoggedInUser(), category);
    }

    public Long getSelectedProjectId()
    {
        final Project project = getSelectedProjectObject();
        return project == null ? null : project.getId();
    }

    public void setSelectedProject(GenericValue project)
    {
        if (project == null)
        {
            setSelectedProjectId(null);
        }
        else
        {
            setSelectedProjectId(project.getLong("id"));
        }
    }

    public void setSelectedProject(Project project)
    {
        if (project == null)
        {
            setSelectedProjectId(null);
        }
        else
        {
            setSelectedProjectId(project.getId());
        }
    }
}
