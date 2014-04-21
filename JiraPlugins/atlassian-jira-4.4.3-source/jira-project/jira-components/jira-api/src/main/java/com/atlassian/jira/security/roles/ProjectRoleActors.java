package com.atlassian.jira.security.roles;

/**
 * This interface defines the association between a ProjectRole and a collection of Actors for a project.
 */
public interface ProjectRoleActors extends DefaultRoleActors
{
    Long getProjectId();
}