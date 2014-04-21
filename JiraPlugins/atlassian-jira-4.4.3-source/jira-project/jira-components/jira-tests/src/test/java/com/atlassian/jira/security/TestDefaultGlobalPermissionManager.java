/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.crowd.embedded.core.util.StaticCrowdServiceFactory;
import com.mockobjects.dynamic.Mock;
import com.opensymphony.user.DuplicateEntityException;
import com.opensymphony.user.ImmutableException;
import org.easymock.MockControl;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class TestDefaultGlobalPermissionManager extends AbstractUsersTestCase
{
    private DefaultGlobalPermissionManager dgpm;
    private User bob;
    private User joe;
    private CrowdService crowdService;

    public TestDefaultGlobalPermissionManager(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        dgpm = new DefaultGlobalPermissionManager(null, StaticCrowdServiceFactory.getCrowdService());

        crowdService = ComponentManager.getComponentInstanceOfType(CrowdService.class);

        bob = new MockUser("bob");
        crowdService.addUser(bob, "");
        joe = new MockUser("joe");
        crowdService.addUser(joe, "");
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testHasUserGroupPermission()
            throws CreateException, DuplicateEntityException, ImmutableException, RemoveException, GenericEntityException, OperationNotPermittedException, InvalidGroupException
    {
        //This call is required to create a Component Entity, even the variable is not used.
        GenericValue component = UtilsForTests.getTestEntity("Component", EasyMap.build("id", new Long(1),"project","2"));

        Group group1 = new MockGroup("group1");
        crowdService.addGroup(group1);
        crowdService.addUserToGroup(bob, group1);
        Group group2 = new MockGroup("group2");
        crowdService.addGroup(group2);
        crowdService.addUserToGroup(joe, group2);

        // Anonymous Permission Global  No group - Anyone
        try
        {
            dgpm.addPermission(1, null);
            fail("You can no longer add USE permission to Anyone group");
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().indexOf("The group Anyone cannot be added to the global permission JIRA Users") != -1);
        }

        // global permission for the group. Therefore for any project the user will have this permission
        dgpm.addPermission(Permissions.ADMINISTER, "group1");
        hasUserGroupAndUserGroup(Permissions.ADMINISTER, bob);
        hasUserGroupAndUserGroup(Permissions.ADMINISTER, bob);
        hasntUserGroupAndUserGroup(Permissions.ADMINISTER, joe);

        dgpm.addPermission(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, "group2");
        hasUserGroupAndUserGroup(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, joe);
        hasntUserGroupAndUserGroup(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, bob);
    }

    public void testSysAdminImpliesAdminHack()
            throws CreateException, DuplicateEntityException, ImmutableException, RemoveException, GenericEntityException, OperationNotPermittedException, InvalidGroupException
    {
        Group group1 = new MockGroup("group1");
        crowdService.addGroup(group1);
        crowdService.addUserToGroup(bob, group1);
        Group group2 = new MockGroup("group2");
        crowdService.addGroup(group2);
        crowdService.addUserToGroup(joe, group2);

        // Add bob's group to the SYSTEM_ADMIN global permission
        dgpm.addPermission(Permissions.SYSTEM_ADMIN, "group1");
        // Add joe's group to the ADMIN global permission
        dgpm.addPermission(Permissions.ADMINISTER, "group2");

        // Verify that asking for bob in the ADMIN global role returns true
        assertTrue(dgpm.hasPermission(Permissions.ADMINISTER, bob));
        // Make sure that the explict call for SYS_ADMIN is correct as well
        assertTrue(dgpm.hasPermission(Permissions.SYSTEM_ADMIN, bob));

        // Verify that bob is an Admin
        assertTrue(dgpm.hasPermission(Permissions.ADMINISTER, joe));
        // Verify that bob is not a Sys Admin
        assertFalse(dgpm.hasPermission(Permissions.SYSTEM_ADMIN, joe));
    }

    public void testAddPermissionWithUserLimitedLicense() throws CreateException
    {
        final Mock mockUserUtil = new Mock(UserUtil.class);
        mockUserUtil.setStrict(true);
        mockUserUtil.expectVoid("clearActiveUserCount");
        setupGlobalPermissionManager((UserUtil) mockUserUtil.proxy());
        dgpm.addPermission(Permissions.SYSTEM_ADMIN, "group1");
        mockUserUtil.verify();
    }

    public void testAddNonUsePermissionWithUserLimitedLicense() throws CreateException
    {
        final Mock mockUserUtil = new Mock(UserUtil.class);
        mockUserUtil.setStrict(true);
        //mock ensure that clearActiveUserCount is *not* called.
        setupGlobalPermissionManager((UserUtil) mockUserUtil.proxy());
        dgpm.addPermission(Permissions.BULK_CHANGE, "group1");
        mockUserUtil.verify();
    }

    public void testRemovePermissionWithUserLimit() throws RemoveException, CreateException
    {
        //let's create a permission first.
        dgpm.addPermission(Permissions.ADMINISTER, "group1");

        final Mock mockUserUtil = new Mock(UserUtil.class);
        mockUserUtil.setStrict(true);
        mockUserUtil.expectVoid("clearActiveUserCount");
        setupGlobalPermissionManager((UserUtil) mockUserUtil.proxy());
        dgpm.removePermission(Permissions.ADMINISTER, "group1");
        mockUserUtil.verify();
    }

    public void testRemoveNonUsePermissionWithUserLimit() throws RemoveException, CreateException
    {
        //let's create a permission first.
        dgpm.addPermission(Permissions.BULK_CHANGE, "group1");

        final Mock mockUserUtil = new Mock(UserUtil.class);
        mockUserUtil.setStrict(true);
        //mock ensure that clearActiveUserCount is *not* called.;
        setupGlobalPermissionManager((UserUtil) mockUserUtil.proxy());
        dgpm.removePermission(Permissions.BULK_CHANGE, "group1");
        mockUserUtil.verify();
    }

    public void testRemovePermissionsWithLimitedLicense()
            throws CreateException, RemoveException, DuplicateEntityException, ImmutableException, OperationNotPermittedException, InvalidGroupException
    {
        Group group1 = new MockGroup("group1");
        crowdService.addGroup(group1);

        //let's create a permission first.
        dgpm.addPermission(Permissions.ADMINISTER, "group1");
        dgpm.addPermission(Permissions.SYSTEM_ADMIN, "group1");
        dgpm.addPermission(Permissions.BULK_CHANGE, "group1");

        final MockControl mockUserUtilControl = MockControl.createControl(UserUtil.class);
        final UserUtil mockUserUtil = (UserUtil) mockUserUtilControl.getMock();
        mockUserUtil.clearActiveUserCount();
        mockUserUtilControl.setVoidCallable(2);
        mockUserUtilControl.replay();
        setupGlobalPermissionManager(mockUserUtil);
        dgpm.removePermissions("group1");
        mockUserUtilControl.verify();
    }

    private void hasUserGroupAndUserGroup(int permtype, User user)
    {
        userGroupAndUserGroup(permtype, user, true);
    }

    private void hasntUserGroupAndUserGroup(int permtype, User user)
    {
        userGroupAndUserGroup(permtype, user, false);
    }

    private void userGroupAndUserGroup(int permtype, User user, boolean assertTrue)
    {
        if (user == null)
        {
            assertEquals(assertTrue, dgpm.hasPermission(permtype));
        }
        else
        {
            assertEquals(assertTrue, dgpm.hasPermission(permtype, user));
        }
    }

    private void setupGlobalPermissionManager(final UserUtil mockUserUtil)
    {
        dgpm = new DefaultGlobalPermissionManager(null, StaticCrowdServiceFactory.getCrowdService())
        {
            UserUtil getUserUtil()
            {
                return mockUserUtil;
            }
        };
    }

}
