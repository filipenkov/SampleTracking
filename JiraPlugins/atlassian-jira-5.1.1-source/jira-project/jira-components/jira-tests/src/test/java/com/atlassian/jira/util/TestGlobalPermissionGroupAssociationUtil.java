package com.atlassian.jira.util;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.MockGroupManager;
import com.atlassian.jira.user.MockUser;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import java.util.Collection;
import java.util.List;

/**
 * Tests GlobalPermissionGroupAssociationUtil
 *
 * @since v3.12
 */
public class TestGlobalPermissionGroupAssociationUtil extends LegacyJiraMockTestCase
{
    public void testGetMemberGroupNames()
    {
        final User testUser = new MockUser("testGetMemberGroupNames");

        final List permGroups = EasyList.build("group1", "group2");
        final Mock mockGlobalPermManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermManager.expectAndReturn("getGroupNames", P.ANY_ARGS, permGroups);

        MockGroupManager mockGroupManager = new MockGroupManager();
        mockGroupManager.addMember("group1", "testGetMemberGroupNames");

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            (GlobalPermissionManager) mockGlobalPermManager.proxy(), mockGroupManager);
        final Collection memberGroups = groupAssociationUtil.getMemberGroupNames(testUser, Permissions.SYSTEM_ADMIN);
        assertNotNull(memberGroups);
        assertEquals(1, memberGroups.size());
        assertEquals("group1", memberGroups.iterator().next());
    }

    public void testGetMemberGroupNamesNoGroups()
    {
        final User testUser = new MockUser("testGetMemberGroupNamesNoGroups");

        final List permGroups = EasyList.build("group1", "group2");
        final Mock mockGlobalPermManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermManager.expectAndReturn("getGroupNames", P.ANY_ARGS, permGroups);

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            (GlobalPermissionManager) mockGlobalPermManager.proxy(), new MockGroupManager());
        final Collection memberGroups = groupAssociationUtil.getMemberGroupNames(testUser, Permissions.SYSTEM_ADMIN);
        assertNotNull(memberGroups);
        assertEquals(0, memberGroups.size());
    }

    public void testGetAdminMemberGroups()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User testUser = createMockUser("testGetAdminMemberGroups");

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null, null)
        {
            @Override
            Collection getMemberGroupNames(final User user, final int permissionType)
            {
                assertEquals(Permissions.ADMINISTER, permissionType);
                return null;
            }
        };

        groupAssociationUtil.getAdminMemberGroups(testUser);
    }

    public void testGetSysAdminMemberGroups()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User testUser = createMockUser("testGetSysAdminMemberGroups");

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null, null)
        {
            @Override
            Collection getMemberGroupNames(final User user, final int permissionType)
            {
                assertEquals(Permissions.SYSTEM_ADMIN, permissionType);
                return null;
            }
        };

        groupAssociationUtil.getSysAdminMemberGroups(testUser);
    }

    public void testIsRemovingAlMyAdminGroupsAreRemoving()
    {
        final List groupsToLeave = EasyList.build("group1", "group2");
        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null, null)
        {

            @Override
            public Collection getAdminMemberGroups(final User user)
            {
                return EasyList.build("group1", "group2");
            }
        };

        assertTrue(groupAssociationUtil.isRemovingAllMyAdminGroups(groupsToLeave, null));
    }

    public void testIsRemovingAlMyAdminGroupsAreNotRemoving()
    {
        final List groupsToLeave = EasyList.build("group1", "group2");
        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null, null)
        {

            @Override
            public Collection getAdminMemberGroups(final User user)
            {
                return EasyList.build("group1", "group2", "group3");
            }
        };

        assertFalse(groupAssociationUtil.isRemovingAllMyAdminGroups(groupsToLeave, null));
    }

    public void testIsRemovingAlMySysAdminGroupsAreRemoving()
    {
        final List groupsToLeave = EasyList.build("group1", "group2");
        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null, null)
        {

            @Override
            public Collection getSysAdminMemberGroups(final User user)
            {
                return EasyList.build("group1", "group2");
            }
        };

        assertTrue(groupAssociationUtil.isRemovingAllMySysAdminGroups(groupsToLeave, null));
    }

    public void testIsRemovingAlMySysAdminGroupsAreNotRemoving()
    {
        final List groupsToLeave = EasyList.build("group1", "group2");
        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null, null)
        {

            @Override
            public Collection getSysAdminMemberGroups(final User user)
            {
                return EasyList.build("group1", "group2", "group3");
            }
        };

        assertFalse(groupAssociationUtil.isRemovingAllMySysAdminGroups(groupsToLeave, null));
    }

    public void testIsUserAbleToDeleteGroupHasGlobalAdminPerm()
    {
        final Mock mockGlobalPermissionManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            (GlobalPermissionManager) mockGlobalPermissionManager.proxy(), null);
        assertTrue(groupAssociationUtil.isUserAbleToDeleteGroup(null, "testgroup"));
    }

    public void testIsUserAbleToDeleteGroupGroupNotInSysAdmins()
    {
        final Mock mockGlobalPermissionManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.FALSE);
        mockGlobalPermissionManager.expectAndReturn("getGroupNames", P.ANY_ARGS, EasyList.build("othergroup"));

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            (GlobalPermissionManager) mockGlobalPermissionManager.proxy(), null);
        assertTrue(groupAssociationUtil.isUserAbleToDeleteGroup(null, "testgroup"));
    }

    public void testIsUserAbleToDeleteGroupNotSysAdminWithSysAdminGroup()
    {
        final Mock mockGlobalPermissionManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.FALSE);
        mockGlobalPermissionManager.expectAndReturn("getGroupNames", P.ANY_ARGS, EasyList.build("testgroup"));

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            (GlobalPermissionManager) mockGlobalPermissionManager.proxy(), null);
        assertFalse(groupAssociationUtil.isUserAbleToDeleteGroup(null, "testgroup"));
    }

    public void testGetGroupNamesModifiableByCurrentUserHasPerm()
    {
        final Mock mockGlobalPermissionManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            (GlobalPermissionManager) mockGlobalPermissionManager.proxy(), null);
        final Collection visibleGroups = groupAssociationUtil.getGroupNamesModifiableByCurrentUser(null, EasyList.build("testgroup1", "testgroup2"));
        assertEquals(2, visibleGroups.size());
        assertTrue(visibleGroups.contains("testgroup1"));
        assertTrue(visibleGroups.contains("testgroup2"));
    }

    public void testGetGroupNamesModifiableByCurrentUserHasNoPerm()
    {
        final Mock mockGlobalPermissionManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.FALSE);
        mockGlobalPermissionManager.expectAndReturn("getGroupNames", P.ANY_ARGS, EasyList.build("testgroup2"));

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            (GlobalPermissionManager) mockGlobalPermissionManager.proxy(), null);
        final Collection visibleGroups = groupAssociationUtil.getGroupNamesModifiableByCurrentUser(null, EasyList.build("testgroup1", "testgroup2"));
        assertEquals(1, visibleGroups.size());
        assertTrue(visibleGroups.contains("testgroup1"));
        assertFalse(visibleGroups.contains("testgroup2"));
    }

    public void testGetGroupsModifiableByCurrentUserHasPerm()
            throws OperationNotPermittedException, InvalidGroupException
    {
        final Mock mockGlobalPermissionManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        final Group group1 = createMockGroup("testgroup1");
        final Group group2 = createMockGroup("testgroup2");

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            (GlobalPermissionManager) mockGlobalPermissionManager.proxy(), null);
        final Collection visibleGroups = groupAssociationUtil.getGroupsModifiableByCurrentUser(null, EasyList.build(group1, group2));
        assertEquals(2, visibleGroups.size());
        assertTrue(visibleGroups.contains(group1));
        assertTrue(visibleGroups.contains(group2));
    }

    public void testGetGroupsModifiableByCurrentUserHasNoPerm()
            throws OperationNotPermittedException, InvalidGroupException
    {
        final Group group1 = createMockGroup("testgroup1");
        final Group group2 = createMockGroup("testgroup2");

        final Mock mockGlobalPermissionManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.FALSE);
        mockGlobalPermissionManager.expectAndReturn("getGroupsWithPermission", P.ANY_ARGS, EasyList.build(group2));

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            (GlobalPermissionManager) mockGlobalPermissionManager.proxy(), null);
        final Collection visibleGroups = groupAssociationUtil.getGroupsModifiableByCurrentUser(null, EasyList.build(group1, group2));
        assertEquals(1, visibleGroups.size());
        assertTrue(visibleGroups.contains(group1));
        assertFalse(visibleGroups.contains(group2));
    }

}
