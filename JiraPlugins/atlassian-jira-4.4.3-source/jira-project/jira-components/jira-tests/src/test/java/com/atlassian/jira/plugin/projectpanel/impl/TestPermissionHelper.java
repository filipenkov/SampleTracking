package com.atlassian.jira.plugin.projectpanel.impl;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.AbstractPermissionManager;
import com.atlassian.jira.local.ListeningTestCase;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

public class TestPermissionHelper extends ListeningTestCase
{
    @Test
    public void testHasProjectAdminPermission()
    {
        MockProject project = new MockProject();

        // no permissions
        PermissionHelper helper = new PermissionHelper(new MockPermissionManager(false, false));
        assertEquals(Boolean.FALSE, helper.hasProjectAdminPermission(null, project));

        // global admin
        helper = new PermissionHelper(new MockPermissionManager(true, false));
        assertEquals(Boolean.TRUE, helper.hasProjectAdminPermission(null, project));

        // project admin
        helper = new PermissionHelper(new MockPermissionManager(false, true));
        assertEquals(Boolean.TRUE, helper.hasProjectAdminPermission(null, project));

        // global and project admin
        helper = new PermissionHelper(new MockPermissionManager(true, true));
        assertEquals(Boolean.TRUE, helper.hasProjectAdminPermission(null, project));
    }

    private static final class MockPermissionManager extends AbstractPermissionManager
    {

        private final boolean isGlobalAdmin;
        private final boolean isProjectAdmin;

        public MockPermissionManager(boolean globalAdmin, boolean projectAdmin)
        {
            isGlobalAdmin = globalAdmin;
            isProjectAdmin = projectAdmin;
        }

        public void addPermission(int permissionsId, GenericValue scheme, String parameter, String securityType) throws CreateException
        {
        }

        public Collection<Group> getAllGroups(int permissionId, Project project)
        {
            return null;
        }

        public Collection<com.opensymphony.user.Group> getAllGroups(int permissionId, GenericValue project)
        {
            return null;
        }

        public Collection<Project> getProjectObjects(final int permissionId, final User user)
        {
            return null;
        }

        public Collection getProjects(int permissionId, User user, GenericValue category) 
        {
            return null;
        }

        public boolean hasPermission(int permissionsId, GenericValue entity, User u)
        {
            return isProjectAdmin;
        }

        public boolean hasPermission(int permissionsId, Issue entity, User u)
        {
            return false;
        }

        public boolean hasPermission(int permissionsId, GenericValue project, User u, boolean issueCreation)
        {
            return false;
        }

        public boolean hasPermission(int permissionsId, Project project, User user)
        {
            return false;
        }

        public boolean hasPermission(int permissionsId, Project project, User user, boolean issueCreation)
        {
            return false;
        }

        public boolean hasPermission(int permissionsId, User user)
        {
            return isGlobalAdmin;
        }

        public boolean hasProjects(int permissionId, User user) 
        {
            return false;
        }

        public void removeGroupPermissions(String group) throws RemoveException
        {
        }

        public void removeUserPermissions(String username) throws RemoveException
        {
        }
    }

}
