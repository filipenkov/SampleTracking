package com.atlassian.jira.security.roles;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.project.Project;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is an interface that defines the storage class for ProjectRoles and RoleActors.
 */
public interface ProjectRoleAndActorStore
{
    ProjectRole addProjectRole(ProjectRole projectRole) throws DataAccessException;

    void updateProjectRole(ProjectRole projectRole) throws DataAccessException;

    Collection<ProjectRole> getAllProjectRoles() throws DataAccessException;

    ProjectRole getProjectRole(Long id) throws DataAccessException;

    ProjectRole getProjectRoleByName(String name) throws DataAccessException;

    void deleteProjectRole(ProjectRole projectRole) throws DataAccessException;

    ProjectRoleActors getProjectRoleActors(Long projectRoleId, Long projectId) throws DataAccessException;

    void updateProjectRoleActors(ProjectRoleActors projectRoleActors) throws DataAccessException;

    void updateDefaultRoleActors(DefaultRoleActors defaultRoleActors) throws DataAccessException;

    DefaultRoleActors getDefaultRoleActors(Long projectRoleId) throws DataAccessException;

    void applyDefaultsRolesToProject(Project project) throws DataAccessException;

    void removeAllRoleActorsByNameAndType(String name, String type) throws DataAccessException;

    void removeAllRoleActorsByProject(Project project) throws DataAccessException;

    Collection<Long> getProjectIdsContainingRoleActorByNameAndType(String name, String type) throws DataAccessException;

    List<Long> roleActorOfTypeExistsForProjects(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter) throws DataAccessException;

    Map<Long, List<String>> getProjectIdsForUserInGroupsBecauseOfRole(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userName) throws DataAccessException;
}
