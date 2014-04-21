/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * The purpose of this class is to provide a temporary access-all-areas pass
 * and is a (partial) implementation of PermissionManager (subverting the
 * stored permissions). Operations that attempt to specify a change to stored
 * permissions like adding or removing permissions and the getAllGroups() method
 * throw an UnsupportedOperationException.
 */
public class SubvertedPermissionManager implements PermissionManager
{
    /**
     * Not implemented.
     */
    public void addPermission(int permissionsId, GenericValue scheme, String group, String securityType)
    {
        throw new UnsupportedOperationException("addPermission() not implemented in " + this.getClass().getName());
    }

    public Collection<Project> getProjectObjects(final int permissionId, final User user)
    {
        return ManagerFactory.getProjectManager().getProjectObjects();
    }

    /**
     * Returns all the projects in the given category, or if category is null,
     * all projects in no category.
     * @param permissionId ignored.
     * @param user ignored.
     * @param category the category for which to get projects.
     * @return the projects.
     */
    public Collection<GenericValue> getProjects(int permissionId, User user, GenericValue category)
    {
        if (category == null)
        {
            return ManagerFactory.getProjectManager().getProjectsWithNoCategory();
        }
        else
        {
            return ManagerFactory.getProjectManager().getProjectsFromProjectCategory(category);
        }
    }

    /**
     * Returns true if there are any projects at all.
     * @param permissionId ignored.
     * @param user ignored.
     * @return true if there are any projects.
     */
    public boolean hasProjects(int permissionId, User user)
    {
        return !getProjects(permissionId, null).isEmpty();
    }

    /**
     * Not implemented.
     */
    public void removeGroupPermissions(String group)
    {
        throw new UnsupportedOperationException("removeGroupPermissions() not implemented in " + this.getClass().getName());
    }

    /**
     * Not implemented.
     */
    public void removeUserPermissions(String group)
    {
        throw new UnsupportedOperationException("removeUserPermissions() not implemented in " + this.getClass().getName());
    }

    /**
     * Not implemented.
     */
    public Collection<Group> getAllGroups(int permType, Project project)
    {
        throw new UnsupportedOperationException("getAllGroups() not implemented in " + this.getClass().getName());
    }

    /**
     * Not implemented.
     */
    public Collection<com.opensymphony.user.Group> getAllGroups(int permType, GenericValue entity)
    {
        throw new UnsupportedOperationException("getAllGroups() not implemented in " + this.getClass().getName());
    }

    /**
     * Always returns true.
     *
     * @param permissionType ignored
     * @param u              ignored
     * @return               true
     */
    public boolean hasPermission(int permissionType, User u)
    {
        return true;
    }

    /**
     * Always returns true.
     *
     * @param permissionsId ignored
     * @param entity        ignored
     * @param u             ignored
     * @return              true
     */
    public boolean hasPermission(int permissionsId, GenericValue entity, User u)
    {
        return true;
    }

    /**
     * Always returns true.
     *
     * @param permissionsId ignored
     * @param issue         ignored
     * @param u             ignored
     * @return              true
     */
    public boolean hasPermission(int permissionsId, Issue issue, User u)
    {
        return true;
    }

    /**
     * Always return true.
     *
     * @param permissionsId ignored
     * @param project       ignored
     * @param user          ignored
     * @return              true
     */
    public boolean hasPermission(int permissionsId, Project project, User user)
    {
        return true;
    }

    /**
     * Always return true.
     *
     * @param permissionsId ignored
     * @param project       ignored
     * @param user          ignored
     * @param issueCreation ignored
     * @return              true
     */
    public boolean hasPermission(int permissionsId, Project project, User user, boolean issueCreation)
    {
        return true;
    }

    /**
     * Always return true.
     *
     * @param permissionsId ignored
     * @param project       ignored
     * @param u             ignored
     * @param issueCreation ignored
     * @return              true
     */
    public boolean hasPermission(int permissionsId, GenericValue project, User u, boolean issueCreation)
    {
        return true;
    }

    public final boolean hasPermission(final int permissionsId, final com.opensymphony.user.User user)
    {
        return hasPermission(permissionsId, (User) user);
    }

    public final boolean hasPermission(final int permissionsId, final GenericValue entity, final com.opensymphony.user.User u)
    {
        return hasPermission(permissionsId, entity, (User) u);
    }

    public final boolean hasPermission(final int permissionsId, final Issue entity, final com.opensymphony.user.User u)
    {
        return hasPermission(permissionsId, entity, (User) u);
    }

    public final boolean hasPermission(final int permissionsId, final Project project, final com.opensymphony.user.User user)
    {
        return hasPermission(permissionsId, project, (User) user);
    }

    public final boolean hasPermission(final int permissionsId, final Project project, final com.opensymphony.user.User user, final boolean issueCreation)
    {
        return hasPermission(permissionsId, project, (User) user, issueCreation);
    }

    public final boolean hasPermission(final int permissionsId, final GenericValue project, final com.opensymphony.user.User u, final boolean issueCreation)
    {
        return hasPermission(permissionsId, project, (User) u, issueCreation);
    }

    public final boolean hasProjects(final int permissionId, final com.opensymphony.user.User user)
    {
        return hasProjects(permissionId, (User) user);
    }

    /**
     * Simply returns all projects.
     * @param permissionId unused.
     * @param user unused.
     * @return all the projects.
     */
    public final Collection<GenericValue> getProjects(final int permissionId, final com.opensymphony.user.User user)
    {
        return ManagerFactory.getProjectManager().getProjects();
    }

    public final Collection<GenericValue> getProjects(final int permissionId, final User user)
    {
        return ManagerFactory.getProjectManager().getProjects();
    }

    public final Collection<Project> getProjectObjects(final int permissionId, final com.opensymphony.user.User user)
    {
        return getProjectObjects(permissionId, (User) user);
    }

    public final Collection<GenericValue> getProjects(final int permissionId, final com.opensymphony.user.User user, final GenericValue category)
    {
        return getProjects(permissionId, (User) user, category);
    }
}
