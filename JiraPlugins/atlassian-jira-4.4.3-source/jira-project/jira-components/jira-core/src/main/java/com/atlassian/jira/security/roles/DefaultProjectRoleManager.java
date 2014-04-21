package com.atlassian.jira.security.roles;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.util.profiling.UtilTimerStack;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @see ProjectRoleManager
 */
public class DefaultProjectRoleManager implements ProjectRoleManager
{
    private ProjectRoleAndActorStore projectRoleAndActorStore;

    public DefaultProjectRoleManager(ProjectRoleAndActorStore projectRoleAndActorStore)
    {
        this.projectRoleAndActorStore = projectRoleAndActorStore;
    }

    public Collection<ProjectRole> getProjectRoles()
    {
        return projectRoleAndActorStore.getAllProjectRoles();
    }

    public Collection<ProjectRole> getProjectRoles(final User user, final Project project)
    {
        Collection<ProjectRole> associatedProjectRoles = new TreeSet<ProjectRole>(ProjectRoleComparator.COMPARATOR);
        Collection<ProjectRole> allProjectRoles = getProjectRoles();
        for (final ProjectRole projectRole : allProjectRoles)
        {
            final ProjectRoleActors projectRoleActors = getProjectRoleActors(projectRole, project);
            if (projectRoleActors.contains(user))
            {
                associatedProjectRoles.add(projectRole);
            }
        }
        return associatedProjectRoles;
    }

    public Collection<ProjectRole> getProjectRoles(com.opensymphony.user.User user, Project project)
    {
        return getProjectRoles((User) user, project);
    }

    public ProjectRole getProjectRole(Long id)
    {
        return projectRoleAndActorStore.getProjectRole(id);
    }

    public ProjectRole getProjectRole(String name)
    {
        if (StringUtils.isBlank(name))
        {
            throw new IllegalArgumentException("ProjectRole can not be found with a null name");
        }

        return projectRoleAndActorStore.getProjectRoleByName(name);
    }

    public ProjectRole createRole(ProjectRole projectRole)
    {
        if (projectRole == null || projectRole.getName() == null)
        {
            throw new IllegalArgumentException("ProjectRole can not be created with a null name");
        }

        if (isRoleNameUnique(projectRole.getName()))
        {
            return projectRoleAndActorStore.addProjectRole(projectRole);
        }
        else
        {
            throw new IllegalArgumentException("A project role with the provided name: " + projectRole.getName() + ", already exists in the system.");
        }
    }

    public boolean isRoleNameUnique(String name)
    {
        return projectRoleAndActorStore.getProjectRoleByName(name) == null;
    }

    public void deleteRole(ProjectRole projectRole)
    {
        if (projectRole == null)
        {
            throw new IllegalArgumentException("ProjectRole can not be null");
        }
        projectRoleAndActorStore.deleteProjectRole(projectRole);
    }

    public void updateRole(ProjectRole projectRole)
    {
        if (projectRole == null)
        {
            throw new IllegalArgumentException("ProjectRole can not be null");
        }
        if (projectRole.getName() == null)
        {
            throw new IllegalArgumentException("ProjectRole name can not be null");
        }
        projectRoleAndActorStore.updateProjectRole(projectRole);
    }

    public ProjectRoleActors getProjectRoleActors(ProjectRole projectRole, Project project)
    {
        if (projectRole == null)
        {
            throw new IllegalArgumentException("ProjectRole can not be null");
        }
        if (project == null)
        {
            throw new IllegalArgumentException("Project can not be null");
        }
        return projectRoleAndActorStore.getProjectRoleActors(projectRole.getId(), project.getId());
    }

    public void updateProjectRoleActors(ProjectRoleActors projectRoleActors)
    {
        if (projectRoleActors == null)
        {
            throw new IllegalArgumentException("ProjectRoleActors can not be null");
        }
        if (projectRoleActors.getProjectId() == null)
        {
            throw new IllegalArgumentException("ProjectRoleActors project can not be null");
        }
        if (projectRoleActors.getProjectRoleId() == null)
        {
            throw new IllegalArgumentException("ProjectRoleActors projectRole can not be null");
        }
        if (projectRoleActors.getRoleActors() == null)
        {
            throw new IllegalArgumentException("ProjectRoleActors roleActors set can not be null");
        }
        projectRoleAndActorStore.updateProjectRoleActors(projectRoleActors);
    }

    public DefaultRoleActors getDefaultRoleActors(ProjectRole projectRole)
    {
        if (projectRole == null)
        {
            throw new IllegalArgumentException("ProjectRole can not be null");
        }
        return projectRoleAndActorStore.getDefaultRoleActors(projectRole.getId());
    }

    public void updateDefaultRoleActors(DefaultRoleActors defaultRoleActors)
    {
        if (defaultRoleActors == null)
        {
            throw new IllegalArgumentException("DefaultRoleActors can not be null");
        }
        if (defaultRoleActors.getProjectRoleId() == null)
        {
            throw new IllegalArgumentException("DefaultRoleActors projectRole can not be null");
        }
        if (defaultRoleActors.getRoleActors() == null)
        {
            throw new IllegalArgumentException("DefaultRoleActors roleActors set can not be null");
        }
        projectRoleAndActorStore.updateDefaultRoleActors(defaultRoleActors);
    }

    public void applyDefaultsRolesToProject(Project project)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project can not be null");
        }
        projectRoleAndActorStore.applyDefaultsRolesToProject(project);
    }

    public void removeAllRoleActorsByNameAndType(String name, String type)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("The role actor name can not be null");
        }
        if (type == null)
        {
            throw new IllegalArgumentException("The role type can not be null");
        }
        projectRoleAndActorStore.removeAllRoleActorsByNameAndType(name, type);
    }

    public void removeAllRoleActorsByProject(Project project)
    {
        if (project == null || project.getId() == null)
        {
            throw new IllegalArgumentException("The project id can not be null");
        }
        projectRoleAndActorStore.removeAllRoleActorsByProject(project);
    }

    public boolean isUserInProjectRole(User user, ProjectRole projectRole, Project project)
    {
        try
        {
            UtilTimerStack.push("DefaultProjectRoleManager.isUserInProjectRole");

            if (project == null || project.getId() == null)
            {
                throw new IllegalArgumentException("The project id can not be null");
            }
            if (projectRole == null)
            {
                throw new IllegalArgumentException("ProjectRole can not be null");
            }
            return getProjectRoleActors(projectRole, project).contains(user);
        }
        finally
        {
            UtilTimerStack.pop("DefaultProjectRoleManager.isUserInProjectRole");
        }
    }

    public boolean isUserInProjectRole(com.opensymphony.user.User user, ProjectRole projectRole, Project project)
    {
        return isUserInProjectRole((User) user, projectRole, project);
    }

    public Collection<Long> getProjectIdsContainingRoleActorByNameAndType(String name, String type)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("The role actor name can not be null");
        }
        if (type == null)
        {
            throw new IllegalArgumentException("The role type can not be null");
        }
        return projectRoleAndActorStore.getProjectIdsContainingRoleActorByNameAndType(name, type);
    }

    public List<Long> roleActorOfTypeExistsForProjects(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter)
    {
        return projectRoleAndActorStore.roleActorOfTypeExistsForProjects(projectsToLimitBy, projectRole, projectRoleType, projectRoleParameter);
    }

    public Map<Long, List<String>> getProjectIdsForUserInGroupsBecauseOfRole(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userName)
    {
        return projectRoleAndActorStore.getProjectIdsForUserInGroupsBecauseOfRole(projectsToLimitBy, projectRole, projectRoleType, userName);
    }

    public ProjectIdToProjectRoleIdsMap createProjectIdToProjectRolesMap(final User user, final Collection<Long> projectIds)
    {
        ProjectIdToProjectRoleIdsMap map = new ProjectIdToProjectRoleIdsMap();
        if (projectIds != null && !projectIds.isEmpty())
        {
            ProjectManager projectManager = ComponentAccessor.getProjectManager();
            for (final Long projectId : projectIds)
            {
                Collection<ProjectRole> projectRoles = getProjectRoles(user, projectManager.getProjectObj(projectId));
                for (final ProjectRole projectRole : projectRoles)
                {
                    map.add(projectId, projectRole.getId());
                }
            }
        }
        return map;
    }

    /**
     * Creates a {@link ProjectIdToProjectRoleIdsMap}. If given projectIds is
     * null or empty, an empty map is returned.
     *
     * @param user       user
     * @param projectIds project ids ({@link Collection} of {@link Long})
     * @return a {@link ProjectIdToProjectRoleIdsMap}
     */
    public ProjectIdToProjectRoleIdsMap createProjectIdToProjectRolesMap(com.opensymphony.user.User user, Collection projectIds)
    {
        return createProjectIdToProjectRolesMap((User) user, projectIds);
    }

}
