package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.DefaultRoleActors;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.util.ErrorCollection;
import org.apache.commons.collections.MultiMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mock implmentation for testing that contains only do-nothing-return-null methods.
 */
public class MockProjectRoleService implements ProjectRoleService
{

    @Override
    public Collection getProjectRoles(com.opensymphony.user.User currentUser, ErrorCollection errorCollection)
    {
        // Old OSUser Object
        return null;
    }

    public Collection getProjectRoles(User currentUser, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public ProjectRole getProjectRole(com.opensymphony.user.User currentUser, Long id, ErrorCollection errorCollection)
    {
        return null;
    }

    public ProjectRole getProjectRole(User currentUser, Long id, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public ProjectRole getProjectRoleByName(com.opensymphony.user.User currentUser, String name, ErrorCollection errorCollection)
    {
        // Old OSUser Object
        return null;
    }

    public ProjectRole getProjectRoleByName(User currentUser, String name, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public ProjectRole createProjectRole(com.opensymphony.user.User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    public ProjectRole createProjectRole(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public boolean isProjectRoleNameUnique(com.opensymphony.user.User currentUser, String name, ErrorCollection errorCollection)
    {
        return false;
    }

    public boolean isProjectRoleNameUnique(User currentUser, String name, ErrorCollection errorCollection)
    {
        return false;
    }

    @Override
    public void deleteProjectRole(com.opensymphony.user.User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
    }

    public void deleteProjectRole(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
    }

    @Override
    public void addActorsToProjectRole(com.opensymphony.user.User currentUser, Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
    {
    }

    @Override
    public void removeActorsFromProjectRole(com.opensymphony.user.User currentUser, Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
    {
    }

    @Override
    public void setActorsForProjectRole(User currentUser, Map<String, Set<String>> newRoleActors, ProjectRole projectRole, Project project, ErrorCollection errorCollection)
    {
    }

    @Override
    public void updateProjectRole(com.opensymphony.user.User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
    }

    public void addActorsToProjectRole(User currentUser, Collection actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
    {
    }

    public void removeActorsFromProjectRole(User currentUser, Collection actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
    {
    }

    public void updateProjectRole(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
    }

    @Override
    public ProjectRoleActors getProjectRoleActors(com.opensymphony.user.User currentUser, ProjectRole projectRole, Project project, ErrorCollection errorCollection)
    {
        return null;
    }

    public ProjectRoleActors getProjectRoleActors(User currentUser, ProjectRole projectRole, Project project, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public DefaultRoleActors getDefaultRoleActors(com.opensymphony.user.User currentUser, ProjectRole projectRole, ErrorCollection collection)
    {
        return null;
    }

    public DefaultRoleActors getDefaultRoleActors(User currentUser, ProjectRole projectRole, ErrorCollection collection)
    {
        return null;
    }

    @Override
    public void addDefaultActorsToProjectRole(com.opensymphony.user.User currentUser, Collection<String> actors, ProjectRole projectRole, String type, ErrorCollection errorCollection)
    {
    }

    @Override
    public void removeDefaultActorsFromProjectRole(com.opensymphony.user.User currentUser, Collection<String> actors, ProjectRole projectRole, String actorType, ErrorCollection errorCollection)
    {
    }

    @Override
    public void removeAllRoleActorsByNameAndType(com.opensymphony.user.User currentUser, String name, String type, ErrorCollection errorCollection)
    {
    }

    public void addDefaultActorsToProjectRole(User currentUser, Collection actorNames, ProjectRole projectRole, String type, ErrorCollection errorCollection)
    {
    }

    public void removeDefaultActorsFromProjectRole(User currentUser, Collection actors, ProjectRole projectRole, String actorType, ErrorCollection errorCollection)
    {
    }

    public void removeAllRoleActorsByNameAndType(User currentUser, String name, String type, ErrorCollection errorCollection)
    {
    }

    @Override
    public ErrorCollection validateRemoveAllRoleActorsByNameAndType(com.opensymphony.user.User currentUser, String name, String type)
    {
        return null;
    }

    public ErrorCollection validateRemoveAllRoleActorsByNameAndType(final User currentUser, final String name, final String type)
    {
        return null;
    }

    public void removeAllRoleActorsByNameAndType(final String name, final String type)
    {
    }

    @Override
    public void removeAllRoleActorsByProject(com.opensymphony.user.User currentUser, Project project, ErrorCollection errorCollection)
    {
    }

    public void removeAllRoleActorsByProject(User currentUser, Project project, ErrorCollection errorCollection)
    {
    }

    @Override
    public Collection getAssociatedNotificationSchemes(com.opensymphony.user.User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    public Collection getAssociatedNotificationSchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public Collection getAssociatedPermissionSchemes(com.opensymphony.user.User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    public Collection getAssociatedPermissionSchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public Collection getAssociatedIssueSecuritySchemes(com.opensymphony.user.User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    public MultiMap getAssociatedWorkflows(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public Collection<Project> getProjectsContainingRoleActorByNameAndType(com.opensymphony.user.User currentUser, String name, String type, ErrorCollection errorCollection)
    {
        return null;
    }

    public Collection getProjectsContainingRoleActorByNameAndType(User currentUser, String name, String type, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public List<Long> roleActorOfTypeExistsForProjects(com.opensymphony.user.User currentUser, List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public Map<Long, List<String>> getProjectIdsForUserInGroupsBecauseOfRole(com.opensymphony.user.User currentUser, List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userName, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public boolean hasProjectRolePermission(com.opensymphony.user.User currentUser, Project project)
    {
        return false;
    }

    public List roleActorOfTypeExistsForProjects(User currentUser, List projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter, ErrorCollection errorCollection)
    {
        return null;
    }

    public Map getProjectIdsForUserInGroupsBecauseOfRole(User currentUser, List projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userName, ErrorCollection errorCollection)
    {
        return null;
    }

    public boolean hasProjectRolePermission(final User currentUser, final Project project)
    {
        return false;
    }

    public Collection getAssociatedIssueSecuritySchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }

    @Override
    public MultiMap getAssociatedWorkflows(com.opensymphony.user.User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return null;
    }
}