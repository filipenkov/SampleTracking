package com.atlassian.jira.plugin.report.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * PermissionManager that is suitable for use in tests. Please either subclass or modify as necessary
 */
public class MockPermissionManager implements PermissionManager
{

    public void addPermission(int permissionsId, GenericValue scheme, String parameter, String securityType) throws CreateException
    {
        throw new UnsupportedOperationException();
    }

    public Collection<Group> getAllGroups(int permissionId, Project project)
    {
        throw new UnsupportedOperationException();
    }

    public Collection<com.opensymphony.user.Group> getAllGroups(int permissionId, GenericValue project)
    {
        throw new UnsupportedOperationException();
    }

    public Collection<Project> getProjectObjects(final int permissionId, final User user)
    {
        throw new UnsupportedOperationException();
    }

    public Collection<GenericValue> getProjects(int permissionId, User user, GenericValue category)
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasPermission(int permissionsId, GenericValue entity, User u)
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasPermission(int permissionsId, Issue entity, User u)
    {
        return true;
    }

    public boolean hasPermission(int permissionsId, GenericValue project, User u, boolean issueCreation)
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasPermission(int permissionsId, Project project, User user)
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasPermission(int permissionsId, Project project, User user, boolean issueCreation)
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasPermission(int permissionsId, User user)
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasProjects(int permissionId, User user)
    {
        throw new UnsupportedOperationException();
    }

    public void removeGroupPermissions(String group) throws RemoveException
    {
        throw new UnsupportedOperationException();
    }

    public void removeUserPermissions(String username) throws RemoveException
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasPermission(final int permissionsId, final com.opensymphony.user.User user)
    {
        return hasPermission(permissionsId, (User) user);
    }

    public boolean hasPermission(final int permissionsId, final GenericValue entity, final com.opensymphony.user.User u)
    {
        return hasPermission(permissionsId, entity, (User) u);
    }

    public boolean hasPermission(final int permissionsId, final Issue entity, final com.opensymphony.user.User u)
    {
        return hasPermission(permissionsId, entity, (User) u);
    }

    public boolean hasPermission(final int permissionsId, final Project project, final com.opensymphony.user.User user)
    {
        return hasPermission(permissionsId, project, (User) user);
    }

    public boolean hasPermission(final int permissionsId, final Project project, final com.opensymphony.user.User user, final boolean issueCreation)
    {
        return hasPermission(permissionsId, project, (User) user, issueCreation);
    }

    public boolean hasPermission(final int permissionsId, final GenericValue project, final com.opensymphony.user.User u, final boolean issueCreation)
    {
        return hasPermission(permissionsId, project, (User) u, issueCreation);
    }

    public boolean hasProjects(final int permissionId, final com.opensymphony.user.User user)
    {
        return hasProjects(permissionId, (User) user);
    }

    public Collection<GenericValue> getProjects(final int permissionId, final User user)
    {
        throw new UnsupportedOperationException();
    }

    public Collection<GenericValue> getProjects(final int permissionId, final com.opensymphony.user.User user)
    {
        throw new UnsupportedOperationException();
    }

    public Collection<Project> getProjectObjects(final int permissionId, final com.opensymphony.user.User user)
    {
        return getProjectObjects(permissionId, (User) user);
    }

    public Collection<GenericValue> getProjects(final int permissionId, final com.opensymphony.user.User user, final GenericValue category)
    {
        return getProjects(permissionId, (User) user, category);
    }
}
