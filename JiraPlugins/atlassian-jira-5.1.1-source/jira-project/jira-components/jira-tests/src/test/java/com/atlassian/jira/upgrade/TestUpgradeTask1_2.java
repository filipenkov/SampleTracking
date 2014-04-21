/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.upgrade.tasks.professional.UpgradeTask1_2_1;
import com.atlassian.jira.web.action.setup.AbstractSetupAction;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class TestUpgradeTask1_2 extends AbstractUsersTestCase
{
    public TestUpgradeTask1_2(String string)
    {
        super(string);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void testDefaultPermissionScheme() throws Exception
    {
        com.atlassian.jira.upgrade.tasks.UpgradeTask1_2 upgrade = new com.atlassian.jira.upgrade.tasks.UpgradeTask1_2();
        upgrade.doUpgrade(false);

        PermissionSchemeManager permissionSchemeManager = ManagerFactory.getPermissionSchemeManager();

        final GenericValue defaultScheme = permissionSchemeManager.getDefaultScheme();
        assertNotNull(defaultScheme);

        assertEquals("Default Permission Scheme", defaultScheme.getString("name"));
        assertEquals("This is the default Permission Scheme. Any new projects that are created will be assigned this scheme.", defaultScheme.getString("description"));
    }

    public void testDoPermissionsStandard()
            throws GenericEntityException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException, InvalidGroupException
    {
        User user = createMockUser("user");
        Group defaultGroupUsers = createMockGroup(AbstractSetupAction.DEFAULT_GROUP_USERS);
        addUserToGroup(user, defaultGroupUsers);

        User developer = createMockUser("developer");
        Group defaultGroupDevelopers = createMockGroup(AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS);
        addUserToGroup(developer, defaultGroupDevelopers);

        User admin = createMockUser("admin");
        Group defaultGroupAdmins = createMockGroup(AbstractSetupAction.DEFAULT_GROUP_ADMINS);
        addUserToGroup(admin, defaultGroupAdmins);

        com.atlassian.jira.upgrade.tasks.UpgradeTask1_2 upgrade = new com.atlassian.jira.upgrade.tasks.UpgradeTask1_2();
        upgrade.doUpgrade(false);
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.USE, user));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, developer));

        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "project"));
        PermissionSchemeManager permissionSchemeManager = ManagerFactory.getPermissionSchemeManager();
        GenericValue scheme = permissionSchemeManager.createDefaultScheme();
        permissionSchemeManager.addDefaultSchemeToProject(project);

        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.BROWSE, project, user));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.CREATE_ISSUE, project, user));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.COMMENT_ISSUE, project, user));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.ASSIGN_ISSUE, project, developer));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.ASSIGNABLE_USER, project, developer));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.RESOLVE_ISSUE, project, developer));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.LINK_ISSUE, project, developer));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.EDIT_ISSUE, project, developer));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.DELETE_ISSUE, project, admin));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.CLOSE_ISSUE, project, developer));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.COMMENT_EDIT_ALL, project, developer));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.COMMENT_EDIT_OWN, project, user));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.COMMENT_DELETE_ALL, project, admin));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.COMMENT_DELETE_OWN, project, user));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.ATTACHMENT_DELETE_ALL, project, admin));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.ATTACHMENT_DELETE_OWN, project, user));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.WORKLOG_EDIT_ALL, project, developer));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.WORKLOG_EDIT_OWN, project, user));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.WORKLOG_DELETE_ALL, project, admin));
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.WORKLOG_DELETE_OWN, project, user));
    }

    public void testDoPermissions()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException, InvalidGroupException
    {
        User user = createMockUser("user");
        Group defaultGroupUsers = createMockGroup(AbstractSetupAction.DEFAULT_GROUP_USERS);
        addUserToGroup(user, defaultGroupUsers);

        User developer = createMockUser("developer");
        Group defaultGroupDevelopers = createMockGroup(AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS);
        addUserToGroup(developer, defaultGroupDevelopers);

        User admin = createMockUser("admin");
        Group defaultGroupAdmins = createMockGroup(AbstractSetupAction.DEFAULT_GROUP_ADMINS);
        addUserToGroup(admin, defaultGroupAdmins);

        UpgradeTask1_2_1 upgrade = new UpgradeTask1_2_1();
        upgrade.doUpgrade(false);
        assertTrue(ManagerFactory.getPermissionManager().hasPermission(Permissions.CREATE_SHARED_OBJECTS, user));
    }
}
