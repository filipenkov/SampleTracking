package com.atlassian.jira.util;

import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.user.GroupUtils;
import com.atlassian.core.util.collection.EasyList;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.Group;
import com.opensymphony.user.ImmutableException;
import com.opensymphony.user.User;

import java.util.Collection;
import java.util.List;

/**
 * Tests GlobalPermissionGroupAssociationUtil
 *
 * @since v3.12
 */
public class TestGlobalPermissionGroupAssociationUtil extends LegacyJiraMockTestCase
{
    public void testGetMemberGroupNames() throws ImmutableException
    {
        final User testUser = UtilsForTests.getTestUser("testGetMemberGroupNames");
        final Group group = GroupUtils.getGroupSafely("group1");
        testUser.addToGroup(group);

        final List permGroups = EasyList.build("group1", "group2");
        final Mock mockGlobalPermManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermManager.expectAndReturn("getGroupNames", P.ANY_ARGS, permGroups);

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            (GlobalPermissionManager) mockGlobalPermManager.proxy());
        final Collection memberGroups = groupAssociationUtil.getMemberGroupNames(testUser, Permissions.SYSTEM_ADMIN);
        assertNotNull(memberGroups);
        assertEquals(1, memberGroups.size());
        assertEquals("group1", memberGroups.iterator().next());
    }

    public void testGetMemberGroupNamesNoGroups() throws ImmutableException
    {
        final User testUser = UtilsForTests.getTestUser("testGetMemberGroupNamesNoGroups");

        final List permGroups = EasyList.build("group1", "group2");
        final Mock mockGlobalPermManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermManager.expectAndReturn("getGroupNames", P.ANY_ARGS, permGroups);

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            (GlobalPermissionManager) mockGlobalPermManager.proxy());
        final Collection memberGroups = groupAssociationUtil.getMemberGroupNames(testUser, Permissions.SYSTEM_ADMIN);
        assertNotNull(memberGroups);
        assertEquals(0, memberGroups.size());
    }

    public void testGetAdminMemberGroups()
    {
        final User testUser = UtilsForTests.getTestUser("testGetAdminMemberGroups");

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null)
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
    {
        final User testUser = UtilsForTests.getTestUser("testGetSysAdminMemberGroups");

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null)
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
        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null)
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
        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null)
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
        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null)
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
        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null)
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
            (GlobalPermissionManager) mockGlobalPermissionManager.proxy());
        assertTrue(groupAssociationUtil.isUserAbleToDeleteGroup(null, "testgroup"));
    }

    public void testIsUserAbleToDeleteGroupGroupNotInSysAdmins()
    {
        final Mock mockGlobalPermissionManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.FALSE);
        mockGlobalPermissionManager.expectAndReturn("getGroupNames", P.ANY_ARGS, EasyList.build("othergroup"));

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            (GlobalPermissionManager) mockGlobalPermissionManager.proxy());
        assertTrue(groupAssociationUtil.isUserAbleToDeleteGroup(null, "testgroup"));
    }

    public void testIsUserAbleToDeleteGroupNotSysAdminWithSysAdminGroup()
    {
        final Mock mockGlobalPermissionManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.FALSE);
        mockGlobalPermissionManager.expectAndReturn("getGroupNames", P.ANY_ARGS, EasyList.build("testgroup"));

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            (GlobalPermissionManager) mockGlobalPermissionManager.proxy());
        assertFalse(groupAssociationUtil.isUserAbleToDeleteGroup(null, "testgroup"));
    }

    public void testGetGroupNamesModifiableByCurrentUserHasPerm()
    {
        final Mock mockGlobalPermissionManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            (GlobalPermissionManager) mockGlobalPermissionManager.proxy());
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
            (GlobalPermissionManager) mockGlobalPermissionManager.proxy());
        final Collection visibleGroups = groupAssociationUtil.getGroupNamesModifiableByCurrentUser(null, EasyList.build("testgroup1", "testgroup2"));
        assertEquals(1, visibleGroups.size());
        assertTrue(visibleGroups.contains("testgroup1"));
        assertFalse(visibleGroups.contains("testgroup2"));
    }

    public void testGetGroupsModifiableByCurrentUserHasPerm() throws ImmutableException
    {
        final Mock mockGlobalPermissionManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        final Group group1 = GroupUtils.getGroupSafely("testgroup1");
        final Group group2 = GroupUtils.getGroupSafely("testgroup2");

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            (GlobalPermissionManager) mockGlobalPermissionManager.proxy());
        final Collection visibleGroups = groupAssociationUtil.getGroupsModifiableByCurrentUser(null, EasyList.build(group1, group2));
        assertEquals(2, visibleGroups.size());
        assertTrue(visibleGroups.contains(group1));
        assertTrue(visibleGroups.contains(group2));
    }

    public void testGetGroupsModifiableByCurrentUserHasNoPerm() throws ImmutableException
    {
        final Group group1 = GroupUtils.getGroupSafely("testgroup1");
        final Group group2 = GroupUtils.getGroupSafely("testgroup2");

        final Mock mockGlobalPermissionManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.FALSE);
        mockGlobalPermissionManager.expectAndReturn("getGroups", P.ANY_ARGS, EasyList.build(group2));

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            (GlobalPermissionManager) mockGlobalPermissionManager.proxy());
        final Collection visibleGroups = groupAssociationUtil.getGroupsModifiableByCurrentUser(null, EasyList.build(group1, group2));
        assertEquals(1, visibleGroups.size());
        assertTrue(visibleGroups.contains(group1));
        assertFalse(visibleGroups.contains(group2));
    }

}
