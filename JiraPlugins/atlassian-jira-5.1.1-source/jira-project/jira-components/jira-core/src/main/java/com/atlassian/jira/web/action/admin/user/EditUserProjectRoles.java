package com.atlassian.jira.web.action.admin.user;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

/**
 * This is the bulk edit for project roles given a user.
 */
@WebSudoRequired
public class EditUserProjectRoles extends ViewUserProjectRoles
{

    public EditUserProjectRoles(ProjectManager projectManager, ProjectRoleService projectRoleService, ProjectFactory projectFactory,
                final CrowdService crowdService)
    {
        super(projectManager, projectRoleService, projectFactory, crowdService);
    }

    @RequiresXsrfCheck
    public String doExecute() throws Exception
    {
        Map parameters = ActionContext.getParameters();
        Set projectIds = getShownProjectIds();
        Collection<Project> projectsToUpdate = getProjectsFromIds(projectIds);

        // Iterate through the projects
        for (Project project : projectsToUpdate)
        {
            // Iterate through all project roles
            for (ProjectRole projectRole : getAllProjectRoles())
            {
                updateRoleActorsForProjectRole(project, projectRole, parameters);
            }
        }
        //JRA-12528: We need to return errors to the user.
        if(hasAnyErrors())
        {
            return ERROR;
        }

        return forceRedirect("ViewUserProjectRoles.jspa?name=" + JiraUrlCodec.encode(name));
    }

    public String doRefresh()
    {
        Set projectIds = getShownProjectIds();

        String[] projectsToAdd = (String[]) ActionContext.getParameters().get("projects_to_add");
        if (projectsToAdd != null)
        {
            String projectIdsToAddValue = projectsToAdd[0];
            String[] projectIdsToAdd = projectIdsToAddValue.split(",");
            List addIds = Arrays.asList(projectIdsToAdd);
            if (addIds != null)
            {
                projectIds.addAll(addIds);
            }
        }
        // From this method we setup the visbile projects as an aggregation of what was visible on the previous page
        // and what the user is asking to have visible now. We do not need to go to the database for this information
        currentVisibleProjects = getProjectsFromIds(projectIds);

        return SUCCESS;
    }

    public boolean isAllProjectsInCategoryVisible(GenericValue projectCategory)
    {
        Collection projects = new ArrayList(getAllProjectsForCategory(projectCategory));
        Collection visibleProjects = getCurrentVisibleProjects();

        // Get the intersection of the two lists and if empty then true, else false
        projects.removeAll(visibleProjects);
        return projects.isEmpty();
    }

    public Collection<Project> getAllProjectsWithoutCategory()
    {
        return projectFactory.getProjects(projectManager.getProjectsWithNoCategory());
    }

    private Set getShownProjectIds()
    {
        Set projectIds = new HashSet();

        // Get all the projects that were viewable
        String[] shownProjectIds = (String[]) ActionContext.getParameters().get("project_shown");

        List shownIds = (shownProjectIds == null) ? new ArrayList() : Arrays.asList(shownProjectIds);
        if (shownIds != null)
        {
            projectIds.addAll(shownIds);
        }
        return projectIds;
    }

    private Collection<Project> getProjectsFromIds(Set shownProjects)
    {
        List projectIds = new ArrayList();
        for (Iterator iterator = shownProjects.iterator(); iterator.hasNext();)
        {
            String projectIdString = (String) iterator.next();
            projectIds.add(new Long(projectIdString));
        }

        ArrayList<Project> projects = newArrayList();
        List<GenericValue> projectGVs = projectManager.convertToProjects(projectIds);
        if (projectGVs != null)
        {
            List<Project> projectObjects = projectFactory.getProjects(projectGVs);
            if (projectObjects != null)
            {
                projects.addAll(projectObjects);
            }
        }

        return projects;
    }

    private void updateRoleActorsForProjectRole(Project project, ProjectRole projectRole, Map parameters)
    {
        // Look for the value of the checkbox and the original value
        String key = project.getId() + "_" + projectRole.getId();
        String[] origParam = (String[]) parameters.get(key + "_orig");
        boolean origValue = Boolean.valueOf(origParam[0]);
        String[] newValue = (String[]) parameters.get(key);

        // This means we are removing the user from the tang
        if (newValue == null && origValue)
        {
            projectRoleService.removeActorsFromProjectRole(getLoggedInUser(), EasyList.build(name), projectRole, project, UserRoleActorFactory.TYPE, this);
        }
        // This means we are adding the user to the tang
        else if (newValue != null && !origValue)
        {
            projectRoleService.addActorsToProjectRole(getLoggedInUser(), EasyList.build(name), projectRole, project, UserRoleActorFactory.TYPE, this);
        }
    }

}
