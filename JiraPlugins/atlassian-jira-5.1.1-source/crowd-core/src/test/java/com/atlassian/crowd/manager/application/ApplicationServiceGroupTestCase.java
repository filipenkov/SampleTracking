package com.atlassian.crowd.manager.application;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.manager.directory.DirectoryPermissionException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.google.common.collect.ImmutableList;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * TestCase to cover Group based operations on the ApplicationManager
 */
@SuppressWarnings({"ThrowableInstanceNeverThrown", "deprecation"})
public class ApplicationServiceGroupTestCase extends ApplicationServiceTestCase
{
    protected static final String GROUP1_NAME = "Remote Group 1";
    protected static final String GROUP2_NAME = "Remote Group 2";
    protected static final String GROUP3_NAME = "Z Remote Group 3";
    protected static final String INACTIVE_GROUP_NAME = "Inactive Remote Group";
    protected static final String NON_EXISTENT_GROUP_NAME = "Non-existent group";

    protected Group group1;
    protected List<User> group1Members;
    protected Group group2;
    protected List<User> group2Members;
    protected Group group3;
    protected List<User> group3Members;
    protected Group inactiveGroup;
    protected List<User> inactiveGroupMembers;
    protected List<Group> principal1Memberships;
    protected List<Group> principal2Memberships;
    protected List<Group> principal3Memberships;
    protected List<Group> inactivePrincipalMemberships;
    protected Group duplicateGroup1;
    protected List<User> duplicateGroup1Members;
    protected Group duplicateGroup2;
    protected List<User> duplicateGroup2Members;

    @Before
    public void setUp()
    {
        super.setUp();
        group1 = mock(Group.class);
        when(group1.getName()).thenReturn(GROUP1_NAME);
        when(group1.getDirectoryId()).thenReturn(DIRECTORY1_ID);
        when(group1.getType()).thenReturn(GroupType.GROUP);

        group2 = mock(Group.class);
        when(group2.getName()).thenReturn(GROUP2_NAME);
        when(group2.getDirectoryId()).thenReturn(DIRECTORY2_ID);
        when(group2.getType()).thenReturn(GroupType.GROUP);

        group3 = mock(Group.class);
        when(group3.getName()).thenReturn(GROUP3_NAME); // "Z" in name tests ordering
        when(group3.getDirectoryId()).thenReturn(DIRECTORY3_ID);
        when(group3.getType()).thenReturn(GroupType.GROUP);

        inactiveGroup = mock(Group.class);
        when(inactiveGroup.getName()).thenReturn(INACTIVE_GROUP_NAME);
        when(inactiveGroup.getDirectoryId()).thenReturn(INACTIVE_DIRECTORY_ID);
        when(inactiveGroup.getType()).thenReturn(GroupType.GROUP);

        duplicateGroup1 = mock(Group.class);
        when(duplicateGroup1.getName()).thenReturn(GROUP1_NAME);
        when(duplicateGroup1.getDirectoryId()).thenReturn(DIRECTORY2_ID);
        when(duplicateGroup1.getType()).thenReturn(GroupType.GROUP);
        when(duplicateGroup1.getDescription()).thenReturn("Duplicate of Remote Group 1");

        duplicateGroup2 = mock(Group.class);
        when(duplicateGroup2.getName()).thenReturn(GROUP2_NAME);
        when(duplicateGroup2.getDirectoryId()).thenReturn(DIRECTORY2_ID);
        when(duplicateGroup2.getType()).thenReturn(GroupType.GROUP);
        when(duplicateGroup2.getDescription()).thenReturn("Duplicate of Remote Group 2");

        principal1Memberships = Arrays.asList(group1);
        principal2Memberships = Arrays.asList(group2, group3);
        principal3Memberships = Arrays.asList(group3);
        inactivePrincipalMemberships = Arrays.asList(inactiveGroup);

        duplicateGroup1Members = Arrays.asList(principal1);
        duplicateGroup2Members = Arrays.asList(principal2);

        group1Members = Arrays.asList(principal1);
        group2Members = Arrays.asList(principal2);
        group3Members = Arrays.asList(principal2, principal3);
        inactiveGroupMembers = Arrays.asList(inactivePrincipal);
    }

    @After
    public void tearDown()
    {
        group1 = null;
        group1Members = null;
        group2 = null;
        group2Members = null;
        group3 = null;
        group3Members = null;
        inactiveGroup = null;
        inactiveGroupMembers = null;
        principal1Memberships = null;
        principal2Memberships = null;
        principal3Memberships = null;
        inactivePrincipalMemberships = null;
        duplicateGroup1 = null;
        duplicateGroup1Members = null;
        duplicateGroup2 = null;
        duplicateGroup2Members = null;

        super.tearDown();
    }

    @Test
    public void testAddGroup() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // directory1: group doesn't exist
        // directory2: group doesn't exist

        GroupTemplate group1Template = new GroupTemplate(group1);

        // Setup test expectations
        when(permissionManager.hasPermission(eq(application), eq(directory1), eq(OperationType.CREATE_GROUP))).thenReturn(true);
        when(permissionManager.hasPermission(eq(application), eq(directory2), eq(OperationType.CREATE_GROUP))).thenReturn(true);
        when(directoryManager.findGroupByName(eq(DIRECTORY1_ID), anyString()))
                .thenThrow(new GroupNotFoundException(GROUP1_NAME))
                .thenReturn(group1);
        when(directoryManager.findGroupByName(eq(DIRECTORY2_ID), anyString()))
                .thenThrow(new GroupNotFoundException(GROUP1_NAME));
        when(directoryManager.addGroup(directory1.getId(), group1Template)).thenReturn(group1);
        when(directoryManager.addGroup(directory2.getId(), group1Template)).thenReturn(group1);


        // add group to application's directories
        Group newGroup = applicationService.addGroup(application, group1Template);

        // assert the returned group is the same is the added group
        assertEquals(group1, newGroup);

        verify(permissionManager, times(2)).hasPermission(eq(application), any(Directory.class), eq(OperationType.CREATE_GROUP));
        verify(directoryManager, times(3)).findGroupByName(anyLong(), anyString());
        verify(directoryManager, times(2)).addGroup(anyLong(), any(GroupTemplate.class));
    }

    @Test(expected = InvalidGroupException.class)
    public void testAddGroup_GroupAlreadyExistsInADirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // directory1: group exists
        // directory2: group exists

        // Setup test expectations
        when(directoryManager.findGroupByName(DIRECTORY1_ID, GROUP1_NAME)).thenReturn(group1);


        // add group to application's directories
        applicationService.addGroup(application, new GroupTemplate(group1));
    }

    @Test(expected = ApplicationPermissionException.class)
    public void testAddGroup_WithNoApplicationPermissions() throws Exception
    {
        // Create the application etc. to pass to the addGroup call
        // Set expectations
        when(permissionManager.hasPermission(application, directory1, OperationType.CREATE_GROUP)).thenReturn(false);
        when(directoryManager.findGroupByName(anyLong(), anyString())).thenThrow(new GroupNotFoundException(GROUP1_NAME));


        // only set one mapping, no need for multiple iterations
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));

        applicationService.addGroup(application, new GroupTemplate(group1));
    }

    @Test(expected = ApplicationPermissionException.class)
    public void testAddGroup_WithNoDirectoryPermissions() throws Exception
    {
        GroupTemplate group1Template = new GroupTemplate(group1);
        // Setup test expectations
        when(permissionManager.hasPermission(application, directory1, OperationType.CREATE_GROUP)).thenReturn(true);
        when(directoryManager.findGroupByName(anyLong(), anyString())).thenThrow(new GroupNotFoundException(GROUP1_NAME));
        when(directoryManager.addGroup(DIRECTORY1_ID, group1Template)).thenThrow(new DirectoryPermissionException());

        // only set one mapping, no need for multiple iterations
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));

        // add group to application's directories
        applicationService.addGroup(application, group1Template);
    }

    @Test
    public void testAddUserToGroup() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));

        // DirectoryManager.findUserByName()
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        // Find the group in the user's directory
        when(directoryManager.findGroupByName(DIRECTORY1_ID, GROUP1_NAME)).thenReturn(group1);
        // OK - check permission
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_GROUP)).thenReturn(true);

        // Finally add membership in Directory
        applicationService.addUserToGroup(application, USER1_NAME, GROUP1_NAME);
        verify(directoryManager).addUserToGroup(DIRECTORY1_ID, USER1_NAME, GROUP1_NAME);
    }

    @Test
    public void testAddUserToGroup_WhereTheGroupExistsInDifferentDirectory() throws Exception
    {
        GroupTemplate group1Template = new GroupTemplate(GROUP1_NAME, DIRECTORY1_ID, GroupType.GROUP);
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));

        // DirectoryManager.findUserByName()
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        // DON'T find the group in the user's directory
        when(directoryManager.findGroupByName(DIRECTORY1_ID, GROUP1_NAME)).thenThrow(new GroupNotFoundException(""));
        // Try to find the group in ANY directory
        when(directoryManager.findGroupByName(DIRECTORY2_ID, GROUP1_NAME)).thenReturn(group1);
        // We will add the group to Directory 1 where the User is.
        when(directoryManager.addGroup(DIRECTORY1_ID, group1Template)).thenReturn(group1);

        // OK - check permission
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_GROUP)).thenReturn(true);

        // Finally add membership in Directory
        applicationService.addUserToGroup(application, USER1_NAME, GROUP1_NAME);
        verify(directoryManager).addUserToGroup(DIRECTORY1_ID, USER1_NAME, GROUP1_NAME);
    }

    @Test(expected = UserNotFoundException.class)
    public void testAddUserToGroup_WhereTheUserDoesNotExist() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));

        when(directoryManager.findUserByName(DIRECTORY1_ID, "dude")).thenThrow(new UserNotFoundException(USER1_NAME));
        when(directoryManager.findUserByName(DIRECTORY2_ID, "dude")).thenThrow(new UserNotFoundException(USER1_NAME));

        applicationService.addUserToGroup(application, "dude", GROUP1_NAME);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testAddUserToGroup_WhereTheGroupDoesNotExist() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));

        // DirectoryManager.findUserByName()
        when(directoryManager.findUserByName(DIRECTORY1_ID, "dude")).thenReturn(principal1);
        // First we search for the Group in the user's directory
        when(directoryManager.findGroupByName(DIRECTORY1_ID, GROUP1_NAME)).thenThrow(new GroupNotFoundException(""));
        // Next we search for the Group in all directories (including the one we just searched in)
        when(directoryManager.findGroupByName(DIRECTORY2_ID, GROUP1_NAME)).thenThrow(new GroupNotFoundException(""));

        applicationService.addUserToGroup(application, "dude", GROUP1_NAME);
    }

    @Test(expected = ApplicationPermissionException.class)
    public void testAddUserToGroup_WithNoApplicationPermission() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));

        // DirectoryManager.findUserByName()
        when(directoryManager.findUserByName(DIRECTORY1_ID, "dude")).thenReturn(principal1);
        // Find the group in the user's directory
        when(directoryManager.findGroupByName(DIRECTORY1_ID, GROUP1_NAME)).thenReturn(group1);
        // OK - check permission
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_GROUP)).thenReturn(false);

        applicationService.addUserToGroup(application, "dude", GROUP1_NAME);
    }

    @Test(expected = OperationFailedException.class)
    public void testAddUserToGroup_WithErrorInUnderlyingDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));

        // DirectoryManager.findUserByName()
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        // Find the group in the user's directory
        when(directoryManager.findGroupByName(DIRECTORY1_ID, GROUP1_NAME)).thenReturn(group1);
        // OK - check permission
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_GROUP)).thenReturn(true);
        // Try to add membership
        doThrow(new OperationFailedException()).when(directoryManager).addUserToGroup(DIRECTORY1_ID, USER1_NAME, GROUP1_NAME);

        applicationService.addUserToGroup(application, USER1_NAME, GROUP1_NAME);
    }

    @Test(expected = ApplicationPermissionException.class)
    public void testAddUserToGroup_WhereGroupTurnsOutToBeReadOnly() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));

        // DirectoryManager.findUserByName()
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        // Find the group in the user's directory
        when(directoryManager.findGroupByName(DIRECTORY1_ID, GROUP1_NAME)).thenReturn(group1);
        // OK - check permission
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_GROUP)).thenReturn(true);
        // Bitterly, the user addition ends up in ReadOnlyGroupException.
        doThrow(new ReadOnlyGroupException(GROUP1_NAME)).when(directoryManager).addUserToGroup(DIRECTORY1_ID, USER1_NAME, GROUP1_NAME);

        applicationService.addUserToGroup(application, USER1_NAME, GROUP1_NAME);
    }

    // tests amalgamation of members in a group spanning multiple directories

    @Test
    public void testFindGroupByName_GroupExistsInMultipleDirectories() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.findGroupByName(anyLong(), anyString())).thenReturn(group1);
        when(directoryManager.searchNestedGroupRelationships(anyLong(), any(MembershipQuery.class))).thenReturn(group1Members, group2Members);

        // execute the tested method
        Group returnedGroup = applicationService.findGroupByName(application, GROUP1_NAME);
        List members = applicationService.searchNestedGroupRelationships(application, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(GROUP1_NAME).returningAtMost(EntityQuery.ALL_RESULTS));

        // check that something is returned
        assertNotNull(returnedGroup);

        // check that the returned group matches the requested group name
        assertEquals(GROUP1_NAME, returnedGroup.getName());

        // check that the returned group has the amalgamated principals from both underlying directories
        assertEquals(2, members.size());
        assertTrue(members.contains(principal1));
        assertTrue(members.contains(principal2));
    }

    /**
     * Test that search returns requested amount of results in sorted order.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSearchDirectGroupRelationships_Constraints() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchDirectGroupRelationships(eq(DIRECTORY1_ID), any(MembershipQuery.class))).thenReturn(ImmutableList.of(USER2_NAME));
        when(directoryManager.searchDirectGroupRelationships(eq(DIRECTORY2_ID), any(MembershipQuery.class))).thenReturn(ImmutableList.of(USER1_NAME));

        // execute the tested method
        List<String> members = applicationService.searchDirectGroupRelationships(application, QueryBuilder.queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(GROUP1_NAME).returningAtMost(1));

        assertEquals(1, members.size());
        assertEquals(USER1_NAME, members.get(0));
    }

    /**
     * Test that search returns requested amount of results in sorted order.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSearchNestedGroupRelationships_Constraints() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchNestedGroupRelationships(eq(DIRECTORY1_ID), any(MembershipQuery.class))).thenReturn(ImmutableList.of(USER2_NAME));
        when(directoryManager.searchNestedGroupRelationships(eq(DIRECTORY2_ID), any(MembershipQuery.class))).thenReturn(ImmutableList.of(USER1_NAME));

        // execute the tested method
        List<String> members = applicationService.searchNestedGroupRelationships(application, QueryBuilder.queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(GROUP1_NAME).returningAtMost(1));

        assertEquals(1, members.size());
        assertEquals(USER1_NAME, members.get(0));
    }

    @Test
    public void testSearchNestedGroupRelationships_InactiveDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(inactiveDirectoryMapping));

        // execute the tested method
        List members = applicationService.searchNestedGroupRelationships(application, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(GROUP1_NAME).returningAtMost(EntityQuery.ALL_RESULTS));

        assertTrue(members.isEmpty());
        verify(directoryManager, never()).searchNestedGroupRelationships(anyLong(), any(MembershipQuery.class));
    }

    @Test
    public void testFindGroupByName_InactiveDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(inactiveDirectoryMapping));

        // execute the tested method
        try
        {
            applicationService.findGroupByName(application, inactiveGroup.getName());
            fail("Finding group in an inactive directory.  Should have thrown a GroupNotFoundException.");
        }
        catch (GroupNotFoundException e)
        {
            // correct behaviour
        }
        verify(directoryManager, never()).findGroupByName(anyLong(), anyString());
    }

    /**
     * Tests amalgamation of members in a group spanning multiple directories where the members have the same username
     * (and thus should be returned as one principal)
     */
    @Test
    public void testFindGroupByName_GroupExistsInMultipleDirectoriesWithDuplicatePrincipals() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // second call returns a copy of the second user
        when(directoryManager.searchNestedGroupRelationships(anyLong(), any(MembershipQuery.class))).thenReturn(Arrays.asList(USER1_NAME));

        // execute the tested method
        final List<String> userNames = applicationService.searchNestedGroupRelationships(application, QueryBuilder.queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(GROUP1_NAME).returningAtMost(EntityQuery.ALL_RESULTS));

        // check that something is returned
        assertNotNull(userNames);
        assertEquals(1, userNames.size());

        // check that the returned group has the amalgamated principals from
        // both underlying directories as the one principal (as they have the same username)
        assertTrue(userNames.contains(principal1.getName()));

        verify(directoryManager, times(2)).searchNestedGroupRelationships(anyLong(), any(MembershipQuery.class));
    }

    @Test(expected = GroupNotFoundException.class)
    public void testFindGroupByName_NoGroupFoundWithTheGivenName() throws Exception
    {
        // no directory has group
        when(directoryManager.findGroupByName(anyLong(), anyString())).thenThrow(new GroupNotFoundException(NON_EXISTENT_GROUP_NAME));

        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));

        // execute the tested method
        applicationService.findGroupByName(application, NON_EXISTENT_GROUP_NAME);
    }

    @Test(expected = RuntimeException.class)
    public void testFindGroupByName_WithErrorInUnderlyingDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.findGroupByName(anyLong(), anyString()))
                .thenThrow(new GroupNotFoundException(NON_EXISTENT_GROUP_NAME)) // first directory has no objects
                .thenThrow(new RuntimeException("Serious problem")); // second directory produces an error thus halting further searching

        applicationService.findGroupByName(application, GROUP1_NAME);
    }

    @Test
    public void testFindGroupMemberships() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.searchNestedGroupRelationships(eq(DIRECTORY1_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP1_NAME, GROUP2_NAME));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);

        final List groupNames = applicationService.searchNestedGroupRelationships(application, QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(principal1.getName()).returningAtMost(EntityQuery.ALL_RESULTS));

        assertNotNull(groupNames);
        assertEquals(2, groupNames.size());
        assertTrue(groupNames.contains(GROUP1_NAME));
        assertTrue(groupNames.contains(GROUP2_NAME));
    }

    @Test
    public void testFindGroupMemberships_WhereNoGroupsExistForPrincipal() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.searchNestedGroupRelationships(anyLong(), any(MembershipQuery.class))).thenReturn(Collections.emptyList());
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);

        final List groupNames = applicationService.searchNestedGroupRelationships(application, QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(principal1.getName()).returningAtMost(EntityQuery.ALL_RESULTS));
        assertNotNull(groupNames);
        assertEquals(0, groupNames.size());
    }

    @Test
    public void testFindGroupMemberships_WithErrorInUnderlyingDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.searchNestedGroupRelationships(anyLong(), any(MembershipQuery.class))).thenThrow(new OperationFailedException());
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);

        List<String> groupNames = applicationService.searchNestedGroupRelationships(application, QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(principal1.getName()).returningAtMost(EntityQuery.ALL_RESULTS));
        assert(groupNames.isEmpty());
    }

    @Test
    public void testFindGroupMemberships_FirstDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchNestedGroupRelationships(eq(DIRECTORY1_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP1_NAME));
        when(directoryManager.searchNestedGroupRelationships(eq(DIRECTORY2_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP2_NAME));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        UserNotFoundException notFoundException = new UserNotFoundException(principal1.getName());
        when(directoryManager.findUserByName(DIRECTORY2_ID, USER1_NAME)).thenThrow(notFoundException);

        final List groupNames = applicationService.searchNestedGroupRelationships(application, QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(principal1.getName()).returningAtMost(EntityQuery.ALL_RESULTS));

        assertNotNull(groupNames);
        assertEquals(1, groupNames.size());
        assertTrue(groupNames.contains(GROUP1_NAME));
    }

    @Test
    public void testFindGroupMemberships_SecondDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchNestedGroupRelationships(eq(DIRECTORY1_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP1_NAME));
        when(directoryManager.searchNestedGroupRelationships(eq(DIRECTORY2_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP2_NAME));
        UserNotFoundException notFoundException = new UserNotFoundException(principal2.getName());
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER2_NAME)).thenThrow(notFoundException);
        when(directoryManager.findUserByName(DIRECTORY2_ID, USER2_NAME)).thenReturn(principal2);

        final List groupNames = applicationService.searchNestedGroupRelationships(application, QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(principal2.getName()).returningAtMost(EntityQuery.ALL_RESULTS));

        assertNotNull(groupNames);
        assertEquals(1, groupNames.size());
        assertTrue(groupNames.contains(GROUP2_NAME));
    }

    @Test
    public void testFindGroupMemberships_MultipleDirectories() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchNestedGroupRelationships(eq(DIRECTORY1_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP1_NAME));
        when(directoryManager.searchNestedGroupRelationships(eq(DIRECTORY2_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP2_NAME));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER2_NAME)).thenReturn(principal1);
        when(directoryManager.findUserByName(DIRECTORY2_ID, USER2_NAME)).thenReturn(principal2);

        List groupNames = applicationService.searchNestedGroupRelationships(application, QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(principal2.getName()).returningAtMost(EntityQuery.ALL_RESULTS));

        assertNotNull(groupNames);
        assertEquals(1, groupNames.size());
        assertTrue(groupNames.contains(GROUP1_NAME));

        // Swap directory order, different result
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping2, directoryMapping1));
        when(directoryManager.searchNestedGroupRelationships(eq(DIRECTORY1_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP1_NAME));
        when(directoryManager.searchNestedGroupRelationships(eq(DIRECTORY2_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP2_NAME));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER2_NAME)).thenReturn(principal1);
        when(directoryManager.findUserByName(DIRECTORY2_ID, USER2_NAME)).thenReturn(principal2);

        groupNames = applicationService.searchNestedGroupRelationships(application, QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(principal2.getName()).returningAtMost(EntityQuery.ALL_RESULTS));

        assertNotNull(groupNames);
        assertEquals(1, groupNames.size());
        assertTrue(groupNames.contains(GROUP2_NAME));
    }

    @Test
    public void testFindNestedGroupMemberships_FirstDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchDirectGroupRelationships(eq(DIRECTORY1_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP1_NAME));
        when(directoryManager.searchDirectGroupRelationships(eq(DIRECTORY2_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP2_NAME));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        UserNotFoundException notFoundException = new UserNotFoundException(principal1.getName());
        when(directoryManager.findUserByName(DIRECTORY2_ID, USER1_NAME)).thenThrow(notFoundException);

        final List groupNames = applicationService.searchDirectGroupRelationships(application, QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(principal1.getName()).returningAtMost(EntityQuery.ALL_RESULTS));

        assertNotNull(groupNames);
        assertEquals(1, groupNames.size());
        assertTrue(groupNames.contains(GROUP1_NAME));
    }

    @Test
    public void testFindNestedGroupMemberships_SecondDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchDirectGroupRelationships(eq(DIRECTORY1_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP1_NAME));
        when(directoryManager.searchDirectGroupRelationships(eq(DIRECTORY2_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP2_NAME));
        UserNotFoundException notFoundException = new UserNotFoundException(principal2.getName());
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER2_NAME)).thenThrow(notFoundException);
        when(directoryManager.findUserByName(DIRECTORY2_ID, USER2_NAME)).thenReturn(principal2);

        final List groupNames = applicationService.searchDirectGroupRelationships(application, QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(principal2.getName()).returningAtMost(EntityQuery.ALL_RESULTS));

        assertNotNull(groupNames);
        assertEquals(1, groupNames.size());
        assertTrue(groupNames.contains(GROUP2_NAME));
    }

    @Test
    public void testFindNestedGroupMemberships_MultipleDirectories() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchDirectGroupRelationships(eq(DIRECTORY1_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP1_NAME));
        when(directoryManager.searchDirectGroupRelationships(eq(DIRECTORY2_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP2_NAME));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER2_NAME)).thenReturn(principal1);
        when(directoryManager.findUserByName(DIRECTORY2_ID, USER2_NAME)).thenReturn(principal2);

        List groupNames = applicationService.searchDirectGroupRelationships(application, QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(principal2.getName()).returningAtMost(EntityQuery.ALL_RESULTS));

        assertNotNull(groupNames);
        assertEquals(1, groupNames.size());
        assertTrue(groupNames.contains(GROUP1_NAME));

        // Swap directory order, different result
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping2, directoryMapping1));
        when(directoryManager.searchDirectGroupRelationships(eq(DIRECTORY1_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP1_NAME));
        when(directoryManager.searchDirectGroupRelationships(eq(DIRECTORY2_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(GROUP2_NAME));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER2_NAME)).thenReturn(principal1);
        when(directoryManager.findUserByName(DIRECTORY2_ID, USER2_NAME)).thenReturn(principal2);

        groupNames = applicationService.searchDirectGroupRelationships(application, QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(principal2.getName()).returningAtMost(EntityQuery.ALL_RESULTS));

        assertNotNull(groupNames);
        assertEquals(1, groupNames.size());
        assertTrue(groupNames.contains(GROUP2_NAME));
    }

    /**
     * Tests that the nested group membership is only checked in the directory of the first user found.
     *
     * ----------------------------------
     * | Directory | Membership         |
     * ----------------------------------
     * |     1     | Alice -> Group 1   |
     * ----------------------------------
     * |     1     | Group 1 -> Group 2 |
     * ----------------------------------
     * |     2     | Alice -> Group 3   |
     * ----------------------------------
     * |     2     | Group 2 -> Group 3 |
     * ----------------------------------
     */
    @Test
    public void testIsUserNestedGroupMember_DuplicateUserInDifferentDirectories() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findUserByName(DIRECTORY2_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.isUserNestedGroupMember(DIRECTORY1_ID, USER1_NAME, GROUP1_NAME)).thenReturn(true);
        when(directoryManager.isUserNestedGroupMember(DIRECTORY1_ID, USER1_NAME, GROUP2_NAME)).thenReturn(true);
        when(directoryManager.isGroupNestedGroupMember(DIRECTORY1_ID, GROUP1_NAME, GROUP2_NAME)).thenReturn(true);
        when(directoryManager.isUserNestedGroupMember(DIRECTORY2_ID, USER1_NAME, GROUP3_NAME)).thenReturn(true);
        when(directoryManager.isGroupNestedGroupMember(DIRECTORY2_ID, GROUP2_NAME, GROUP3_NAME)).thenReturn(true);

        boolean isMember = applicationService.isUserNestedGroupMember(application, USER1_NAME, GROUP1_NAME);
        assertTrue(isMember);
        isMember = applicationService.isUserNestedGroupMember(application, USER1_NAME, GROUP2_NAME);
        assertTrue(isMember);

        isMember = applicationService.isUserNestedGroupMember(application, USER1_NAME, GROUP3_NAME);
        assertFalse("User should not be a member of " + GROUP3_NAME + " since group membership is only checked in the directory of the first user found.", isMember);

        verify(directoryManager, never()).findUserByName(DIRECTORY2_ID, USER1_NAME);
    }

    /**
     * Tests that the direct group membership is only checked in the directory of the first user found.
     */
    @Test
    public void testIsUserDirectGroupMember_DuplicateUserInDifferentDirectories() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findUserByName(DIRECTORY2_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.isUserDirectGroupMember(DIRECTORY1_ID, USER1_NAME, GROUP1_NAME)).thenReturn(true);
        when(directoryManager.isUserDirectGroupMember(DIRECTORY2_ID, USER1_NAME, GROUP3_NAME)).thenReturn(true);

        boolean isMember = applicationService.isUserDirectGroupMember(application, USER1_NAME, GROUP1_NAME);
        assertTrue(isMember);

        isMember = applicationService.isUserDirectGroupMember(application, USER1_NAME, GROUP3_NAME);
        assertFalse("User should not be a member of " + GROUP3_NAME + " since group membership is only checked in the directory of the first user found.", isMember);
    }

    @Test
    public void testIsUserNestedGroupMember_InactiveDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(inactiveDirectoryMapping));

        boolean groupMember = applicationService.isUserNestedGroupMember(application, INACTIVE_USER_NAME, INACTIVE_GROUP_NAME);

        assertFalse(groupMember);
        verify(directoryManager, never()).isUserNestedGroupMember(anyLong(), anyString(), anyString());
    }

    @Test
    public void testIsUserGroupMember_SuccessFalse() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.isUserNestedGroupMember(anyLong(), anyString(), anyString())).thenReturn(false);

        boolean groupMember = applicationService.isUserNestedGroupMember(application, USER1_NAME, GROUP1_NAME);

        assertFalse(groupMember);
    }

    @Test
    public void testIsUserGroupMember_SuccessTrue() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.isUserNestedGroupMember(DIRECTORY1_ID, USER1_NAME, GROUP1_NAME)).thenReturn(true);

        boolean groupMember = applicationService.isUserNestedGroupMember(application, USER1_NAME, GROUP1_NAME);

        assertTrue(groupMember);
    }

    @Test
    public void testIsUserGroupMember_WhereUserDoesNotExist() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenThrow(new UserNotFoundException(USER1_NAME));
        when(directoryManager.isUserNestedGroupMember(anyLong(), anyString(), anyString())).thenReturn(false);

        boolean groupMember = applicationService.isUserNestedGroupMember(application, USER1_NAME, GROUP1_NAME);

        assertFalse(groupMember);
    }

    @Test
    public void testIsUserGroupMember_WithErrorInUnderlyingDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.isUserNestedGroupMember(DIRECTORY1_ID, USER1_NAME, GROUP1_NAME)).thenThrow(new OperationFailedException());

        applicationService.isUserNestedGroupMember(application, USER1_NAME, GROUP1_NAME);
    }

    @Test
    public void testRemoveGroup_InactiveDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(inactiveDirectoryMapping));

        // test removing the group
        try
        {
            applicationService.removeGroup(application, INACTIVE_GROUP_NAME);
            fail("GroupNotFoundException expected");
        }
        catch (GroupNotFoundException e)
        {
            // correct behaviour
        }
        verify(directoryManager, never()).findGroupByName(anyLong(), anyString());
    }

    @Test
    public void testRemoveGroup_IteratingOverMultipleDirectories() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // directory1: group does not exist
        // directory2: group removed
        when(directoryManager.findGroupByName(DIRECTORY1_ID, GROUP2_NAME)).thenThrow(new GroupNotFoundException(GROUP2_NAME));
        when(directoryManager.findGroupByName(DIRECTORY2_ID, GROUP2_NAME)).thenReturn(group2);

        when(permissionManager.hasPermission(eq(application), any(Directory.class), eq(OperationType.DELETE_GROUP))).thenReturn(true);

        // test removing the group
        applicationService.removeGroup(application, GROUP2_NAME);
        verify(directoryManager).removeGroup(DIRECTORY1_ID, GROUP2_NAME);
        verify(directoryManager).removeGroup(DIRECTORY2_ID, GROUP2_NAME);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testRemoveGroup_NoGroupsFoundToRemove() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // directory1: group does not exist
        // directory2: group does not exist
        when(directoryManager.findGroupByName(anyLong(), anyString())).thenThrow(new GroupNotFoundException(GROUP1_NAME));

        // test removing the group
        applicationService.removeGroup(application, GROUP1_NAME);
    }

    @Test(expected = OperationFailedException.class)
    public void testRemoveGroup_WithErrorInUnderlyingDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // directory1: operation fails (RemoteException)
        // directory2: operation DOES NOT get called

        // group with same name exists across multiple directories
        when(directoryManager.findGroupByName(anyLong(), anyString())).thenReturn(group1, group2);

        when(permissionManager.hasPermission(application, directory1, OperationType.DELETE_GROUP)).thenReturn(true);

        doThrow(new OperationFailedException()).when(directoryManager).removeGroup(DIRECTORY1_ID, GROUP1_NAME);


        // test removing the group
        applicationService.removeGroup(application, GROUP1_NAME);
    }

    @Test
    public void testRemoveGroup_WithNoApplicationPermissions() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // directory1: no permissions
        // directory2: no permissions

        // group with same name exists across multiple directories
        when(directoryManager.findGroupByName(DIRECTORY1_ID, GROUP1_NAME)).thenReturn(group1);
        when(directoryManager.findGroupByName(DIRECTORY2_ID, GROUP1_NAME)).thenThrow(new GroupNotFoundException(GROUP1_NAME));

        when(permissionManager.hasPermission(eq(application), any(Directory.class), eq(OperationType.DELETE_GROUP))).thenReturn(false);

        // test removing the group
        try
        {
            applicationService.removeGroup(application, GROUP1_NAME);
            fail("Should have thrown a ApplicationPermissionException");
        }
        catch (ApplicationPermissionException e)
        {
            // expected result
        }
        verify(directoryManager, never()).removeGroup(anyLong(), anyString());
    }

    @Test(expected = ApplicationPermissionException.class)
    public void testRemoveGroup_WithNoDirectoryPermissions() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        // directory1: no permissions

        when(directoryManager.findGroupByName(anyLong(), anyString())).thenReturn(group1);
        when(permissionManager.hasPermission(application, directory1, OperationType.DELETE_GROUP)).thenReturn(true);
        doThrow(new DirectoryPermissionException()).when(directoryManager).removeGroup(DIRECTORY1_ID, GROUP1_NAME);

        // test removing the group
        applicationService.removeGroup(application, GROUP1_NAME);
    }

    @Test(expected = OperationFailedException.class)
    public void testRemoveUserFromGroup_WithErrorInUnderlyingDirectory() throws Exception
    {
        // there are two directories.
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // user1 resides on directory1.
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        // group1 resides in directory1.
        when(directoryManager.findGroupByName(DIRECTORY1_ID, GROUP1_NAME)).thenReturn(group1);
        // search by id of directory1 returns the directory1.
        when(directoryManager.findDirectoryById(directory1.getId())).thenReturn(directory1);
        // user1 is a direct member of group1 under directory1.
        when(directoryManager.isUserDirectGroupMember(DIRECTORY1_ID, USER1_NAME, GROUP1_NAME)).thenReturn(true);
        // directory1 in application allows update operations.
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_GROUP)).thenReturn(true);
        // remove user from group results in a failure for unknown reason.
        doThrow(new OperationFailedException()).when(directoryManager).removeUserFromGroup(DIRECTORY1_ID, USER1_NAME, GROUP1_NAME);

        // hit it.
        applicationService.removeUserFromGroup(application, USER1_NAME, GROUP1_NAME);
    }

    // This case was described in CWD-2198.
    @Test(expected = ApplicationPermissionException.class)
    public void testRemoveUserFromGroup_WhereGroupTurnsOutToBeReadonly() throws Exception
    {
        // there are two directories
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // user2 does not exist in directory1.
        doThrow(new UserNotFoundException(USER2_NAME)).when(directoryManager).findUserByName(DIRECTORY1_ID, USER2_NAME);
        // user2 exists in directory2.
        when(directoryManager.findUserByName(DIRECTORY2_ID, USER2_NAME)).thenReturn(principal2);
        // group2 resides in directory2.
        when(directoryManager.findGroupByName(DIRECTORY2_ID, GROUP2_NAME)).thenReturn(group2);
        // search by id of directory2 returns the directory2.
        when(directoryManager.findDirectoryById(directory2.getId())).thenReturn(directory2);
        // user2 is a direct member of group2 under directory2.
        when(directoryManager.isUserDirectGroupMember(DIRECTORY2_ID, USER2_NAME, GROUP2_NAME)).thenReturn(true);
        // directory2 in application *seemingly* allows update operations.
        when(permissionManager.hasPermission(eq(application), any(Directory.class), eq(OperationType.UPDATE_GROUP))).thenReturn(true);
        // however, the remove user operation on directory2 actually is not allowed.
        doThrow(new ReadOnlyGroupException(GROUP1_NAME)).when(directoryManager).removeUserFromGroup(DIRECTORY2_ID, USER2_NAME, GROUP2_NAME);

        // hit it.
        applicationService.removeUserFromGroup(application, USER2_NAME, GROUP2_NAME);
    }

    @Test
    public void testRemoveUserFromGroup_WithNoApplicationPermissions() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.isUserDirectGroupMember(DIRECTORY1_ID, USER1_NAME, GROUP1_NAME)).thenReturn(true);
        when(permissionManager.hasPermission(eq(application), any(Directory.class), eq(OperationType.UPDATE_GROUP))).thenReturn(false);
        when(directoryManager.findGroupByName(DIRECTORY1_ID, GROUP1_NAME)).thenReturn(group1);

        try
        {
            applicationService.removeUserFromGroup(application, USER1_NAME, GROUP1_NAME);
            fail("Should have thrown an ApplicationPermissionException");
        }
        catch (ApplicationPermissionException e)
        {
            // expected result
        }
        verify(directoryManager, never()).removeUserFromGroup(anyLong(), anyString(), anyString());
    }

    @Test
    public void testRemoveUserFromGroup_success() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(directory1.getId())).thenReturn(directory1);
        when(directoryManager.isUserDirectGroupMember(DIRECTORY1_ID, USER1_NAME, GROUP1_NAME)).thenReturn(true);
        when(permissionManager.hasPermission(eq(application), any(Directory.class), eq(OperationType.UPDATE_GROUP))).thenReturn(true);
        when(directoryManager.findGroupByName(DIRECTORY1_ID, GROUP1_NAME)).thenReturn(group1);

        applicationService.removeUserFromGroup(application, USER1_NAME, GROUP1_NAME);
        verify(directoryManager, times(1)).removeUserFromGroup(anyLong(), eq(USER1_NAME), eq(GROUP1_NAME));
    }

    @Test
    public void testSearchGroups_InactiveDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(inactiveDirectoryMapping));

        Collection<Group> groups = applicationService.searchGroups(application, QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).returningAtMost(50));

        assertTrue(groups.isEmpty());
        verify(directoryManager, never()).searchGroups(anyLong(), any(EntityQuery.class));
    }

    @Test
    public void testSearchGroups() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchGroups(eq(DIRECTORY1_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(group1));
        when(directoryManager.searchGroups(eq(DIRECTORY2_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(group2));

        Collection<Group> groups = applicationService.searchGroups(application, QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).returningAtMost(50));

        assertNotNull(groups);
        assertEquals(2, groups.size());
        assertTrue(groups.contains(group1));
        assertTrue(groups.contains(group2));
    }

    /**
     * Test that search returns requested amount of results in sorted order.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSearchGroups_Constraints() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchGroups(eq(DIRECTORY1_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(group2));
        when(directoryManager.searchGroups(eq(DIRECTORY2_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(group1));

        List<Group> groups = applicationService.searchGroups(application, QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).returningAtMost(1));

        assertEquals(1, groups.size());
        assertEquals(GROUP1_NAME, groups.get(0).getName());
    }

    /**
     * Tests that when there are duplicate groups in multiple directories, only one of the group is returned by
     * {@link ApplicationService#searchGroups(com.atlassian.crowd.model.application.Application, com.atlassian.crowd.search.query.entity.EntityQuery)}.
     */
    @Test
    public void testSearchGroups_WithDuplicateGroupsInMultipleDirectories() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchGroups(eq(DIRECTORY1_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(group1));
        when(directoryManager.searchGroups(eq(DIRECTORY2_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(duplicateGroup1));

        Collection groups = applicationService.searchGroups(application, QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).returningAtMost(50));

        assertNotNull(groups);
        assertEquals(1, groups.size());
        assertTrue(groups.contains(group1));
    }

    @Test
    public void testSearchGroups_WithCheckForOrdering() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchGroups(eq(DIRECTORY1_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(group1, group3));
        when(directoryManager.searchGroups(eq(DIRECTORY2_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(group2, duplicateGroup1));

        List<Group> groups = applicationService.searchGroups(application, QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).returningAtMost(50));

        // check basics
        assertNotNull(groups);
        assertEquals(3, groups.size());

        // Note: group1 and duplicateGroup1 should have been detected as the same and been aggregated.
        Group foundGroup1 = groups.get(0);
        Group foundGroup2 = groups.get(1);
        Group foundGroup3 = groups.get(2);

        // check ordering
        assertEquals("First group should have been group1", group1, foundGroup1);
        assertEquals("Second group should have been group2", group2, foundGroup2);
        assertEquals("Third group should have been group3", group3, foundGroup3);
    }

    @Test
    public void testSearchGroupMemberships_WithCheckForMembershipAggregation() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchNestedGroupRelationships(eq(DIRECTORY1_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(principal1));
        when(directoryManager.searchNestedGroupRelationships(eq(DIRECTORY2_ID), any(MembershipQuery.class))).thenReturn(Arrays.asList(principal2, principal3));

        List<User> memberships = applicationService.searchNestedGroupRelationships(application, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(GROUP1_NAME).returningAtMost(EntityQuery.ALL_RESULTS));

        assertNotNull(memberships);
        assertEquals(3, memberships.size());
        assertTrue(memberships.contains(principal1));
        assertTrue(memberships.contains(principal2));
        assertTrue(memberships.contains(principal3));
    }

    @Test
    public void testSearchGroups_WithErrorInUnderlyingDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        // first directory will blow up with an OperationFailedException, no other directories should be searched
        when(directoryManager.searchGroups(eq(DIRECTORY1_ID), any(EntityQuery.class))).thenThrow(new OperationFailedException());

        List<Group> groups = applicationService.searchGroups(application, QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).returningAtMost(EntityQuery.ALL_RESULTS));
        assertNotNull(groups);
        assert(groups.isEmpty());
    }

    @Test
    public void testSearchGroups_indexOutOfBounds() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchGroups(eq(DIRECTORY1_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(group1, duplicateGroup1));
        when(directoryManager.searchGroups(eq(DIRECTORY2_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(group2, duplicateGroup2));

        // should get no results - there aren't that many groups
        Collection groups = applicationService.searchGroups(application, QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).startingAt(50).returningAtMost(1));

        assertNotNull(groups);
        assertEquals(0, groups.size());
    }

    @Test
    public void testUpdateGroup_InactiveDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(inactiveDirectoryMapping));

        try
        {
            applicationService.updateGroup(application, new GroupTemplate("Unknown Group Name", -1L, GroupType.GROUP));
            fail("GroupNotFoundException expected");
        }
        catch (GroupNotFoundException e)
        {
            // correct behaviour
        }
        verify(directoryManager, never()).findGroupByName(anyLong(), anyString());
    }

    @Test
    public void testUpdateGroup_GroupNotFound() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // directory1: no group
        // directory2: no group
        when(directoryManager.findGroupByName(anyLong(), eq(NON_EXISTENT_GROUP_NAME))).thenThrow(new GroupNotFoundException(NON_EXISTENT_GROUP_NAME));

        try
        {
            applicationService.updateGroup(application, new GroupTemplate(NON_EXISTENT_GROUP_NAME, -1L, GroupType.GROUP));
            fail("GroupNotFoundException expected");
        }
        catch (GroupNotFoundException e)
        {
            // we expect this
        }
        verify(directoryManager, never()).updateGroup(anyLong(), any(GroupTemplate.class));
    }

    @Test
    public void testUpdateGroup_WithErrorInUnderlyingDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findGroupByName(DIRECTORY1_ID, GROUP1_NAME)).thenReturn(group1);
        when(directoryManager.updateGroup(anyLong(), any(GroupTemplate.class))).thenThrow(new OperationFailedException());
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_GROUP)).thenReturn(true);

        // test updating the group
        try
        {
            applicationService.updateGroup(application, new GroupTemplate(GROUP1_NAME, -1L, GroupType.GROUP));
            fail("OperationFailedException expected");
        }
        catch (OperationFailedException e)
        {
            // we expect this
        }
    }

    @Test
    public void testUpdateGroup_WithNoApplicationPermission() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        // directory1: no perms

        when(directoryManager.findGroupByName(DIRECTORY1_ID, GROUP1_NAME)).thenReturn(group1);
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_GROUP)).thenReturn(false);

        try
        {
            applicationService.updateGroup(application, new GroupTemplate(GROUP1_NAME, -1L, GroupType.GROUP));

            fail("ApplicationPermissionException expected");
        }
        catch (ApplicationPermissionException e)
        {
            // we expect this
        }
        verify(directoryManager, never()).updateGroup(anyLong(), any(GroupTemplate.class));
    }

    @Test(expected = ApplicationPermissionException.class)
    public void testUpdateGroup_WithNoDirectoryPermission() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findGroupByName(DIRECTORY1_ID, GROUP1_NAME)).thenReturn(group1);
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_GROUP)).thenReturn(true);
        when(directoryManager.updateGroup(eq(DIRECTORY1_ID), any(GroupTemplate.class))).thenThrow(new DirectoryPermissionException());

        // test removing the group
        applicationService.updateGroup(application, new GroupTemplate(GROUP1_NAME, -1L, GroupType.GROUP));
    }

    @Test(expected = GroupNotFoundException.class)
    public void testAddGroupToGroup_WhereTheParentDoesNotExist() throws Exception
    {
        final String PARENT_GROUP_NAME = "freaks";
        final String CHILD_GROUP_NAME = "geeks";
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.findGroupByName(DIRECTORY1_ID, PARENT_GROUP_NAME)).thenThrow(new GroupNotFoundException(PARENT_GROUP_NAME));
        when(directoryManager.findGroupByName(DIRECTORY2_ID, PARENT_GROUP_NAME)).thenThrow(new GroupNotFoundException(PARENT_GROUP_NAME));

        applicationService.addGroupToGroup(application, CHILD_GROUP_NAME, PARENT_GROUP_NAME);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testAddGroupToGroup_WhereTheChildDoesNotExist() throws Exception
    {
        final String PARENT_GROUP_NAME = "freaks";
        final String CHILD_GROUP_NAME = "geeks";
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.findGroupByName(DIRECTORY1_ID, PARENT_GROUP_NAME)).thenReturn(group1);
        when(directoryManager.findGroupByName(DIRECTORY1_ID, CHILD_GROUP_NAME)).thenThrow(new GroupNotFoundException(CHILD_GROUP_NAME));
        when(directoryManager.findGroupByName(DIRECTORY2_ID, CHILD_GROUP_NAME)).thenThrow(new GroupNotFoundException(CHILD_GROUP_NAME));

        applicationService.addGroupToGroup(application, CHILD_GROUP_NAME, PARENT_GROUP_NAME);
    }

    @Test(expected = InvalidMembershipException.class)
    public void testAddGroupToGroup_DifferentTypes() throws Exception
    {
        final String PARENT_GROUP_NAME = "fruits";
        final String CHILD_GROUP_NAME = "banana";
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));

        Group parentGroup = new GroupTemplate(PARENT_GROUP_NAME, DIRECTORY1_ID, GroupType.GROUP);
        Group childGroup = new GroupTemplate(CHILD_GROUP_NAME, DIRECTORY1_ID, GroupType.LEGACY_ROLE);
        when(directoryManager.findGroupByName(DIRECTORY1_ID, PARENT_GROUP_NAME)).thenReturn(parentGroup);
        when(directoryManager.findGroupByName(DIRECTORY1_ID, CHILD_GROUP_NAME)).thenReturn(childGroup);

        applicationService.addGroupToGroup(application, CHILD_GROUP_NAME, PARENT_GROUP_NAME);
    }

    @Test(expected = InvalidMembershipException.class)
    public void testAddGroupToGroup_SameGroups() throws Exception
    {
        final String PARENT_GROUP_NAME = "bananas";
        final String CHILD_GROUP_NAME = "bananas";
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));

        Group parentGroup = new GroupTemplate(PARENT_GROUP_NAME, DIRECTORY1_ID, GroupType.GROUP);
        Group childGroup = new GroupTemplate(CHILD_GROUP_NAME, DIRECTORY1_ID, GroupType.GROUP);
        when(directoryManager.findGroupByName(eq(DIRECTORY1_ID), anyString())).thenReturn(parentGroup, childGroup);

        applicationService.addGroupToGroup(application, CHILD_GROUP_NAME, PARENT_GROUP_NAME);
    }

    @Test(expected = InvalidMembershipException.class)
    public void testAddGroupToGroup_CircularReference() throws Exception
    {
        final String PARENT_GROUP_NAME = "fruits";
        final String CHILD_GROUP_NAME = "bananas";
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));

        Group parentGroup = new GroupTemplate(PARENT_GROUP_NAME, DIRECTORY1_ID, GroupType.GROUP);
        Group childGroup = new GroupTemplate(CHILD_GROUP_NAME, DIRECTORY1_ID, GroupType.GROUP);
        // Find the Groups
        when(directoryManager.findGroupByName(DIRECTORY1_ID, PARENT_GROUP_NAME)).thenReturn(parentGroup);
        when(directoryManager.findGroupByName(DIRECTORY1_ID, CHILD_GROUP_NAME)).thenReturn(childGroup);
        // Check for Circular reference
        when(directoryManager.isGroupNestedGroupMember(DIRECTORY1_ID, PARENT_GROUP_NAME, CHILD_GROUP_NAME)).thenReturn(false);
        when(directoryManager.isGroupNestedGroupMember(DIRECTORY2_ID, PARENT_GROUP_NAME, CHILD_GROUP_NAME)).thenReturn(true);

        applicationService.addGroupToGroup(application, CHILD_GROUP_NAME, PARENT_GROUP_NAME);
    }

    @Test(expected = ApplicationPermissionException.class)
    public void testAddGroupToGroup_NoPermission() throws Exception
    {
        final String PARENT_GROUP_NAME = "fruits";
        final String CHILD_GROUP_NAME = "bananas";
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));

        Group parentGroup = new GroupTemplate(PARENT_GROUP_NAME, DIRECTORY1_ID, GroupType.GROUP);
        Group childGroup = new GroupTemplate(CHILD_GROUP_NAME, DIRECTORY1_ID, GroupType.GROUP);
        // Find the Groups
        when(directoryManager.findGroupByName(DIRECTORY1_ID, PARENT_GROUP_NAME)).thenReturn(parentGroup);
        when(directoryManager.findGroupByName(DIRECTORY1_ID, CHILD_GROUP_NAME)).thenReturn(childGroup);
        // Check for Circular reference
        when(directoryManager.isGroupNestedGroupMember(DIRECTORY1_ID, PARENT_GROUP_NAME, CHILD_GROUP_NAME)).thenReturn(false);
        when(directoryManager.isGroupNestedGroupMember(DIRECTORY2_ID, PARENT_GROUP_NAME, CHILD_GROUP_NAME)).thenReturn(false);
        // Try to add membership
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_GROUP)).thenReturn(false);
        when(permissionManager.hasPermission(application, directory2, OperationType.UPDATE_GROUP)).thenReturn(false);

        applicationService.addGroupToGroup(application, CHILD_GROUP_NAME, PARENT_GROUP_NAME);
    }

    @Test
    public void testAddGroupToGroup_AddToBoth() throws Exception
    {
        final String PARENT_GROUP_NAME = "fruits";
        final String CHILD_GROUP_NAME = "bananas";
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));

        Group parentGroup = new GroupTemplate(PARENT_GROUP_NAME, DIRECTORY1_ID, GroupType.GROUP);
        Group childGroup = new GroupTemplate(CHILD_GROUP_NAME, DIRECTORY1_ID, GroupType.GROUP);
        // Find the Groups
        when(directoryManager.findGroupByName(DIRECTORY1_ID, PARENT_GROUP_NAME)).thenReturn(parentGroup);
        when(directoryManager.findGroupByName(DIRECTORY1_ID, CHILD_GROUP_NAME)).thenReturn(childGroup);
        // Check for Circular reference
        when(directoryManager.isGroupNestedGroupMember(DIRECTORY1_ID, PARENT_GROUP_NAME, CHILD_GROUP_NAME)).thenReturn(false);
        when(directoryManager.isGroupNestedGroupMember(DIRECTORY2_ID, PARENT_GROUP_NAME, CHILD_GROUP_NAME)).thenReturn(false);
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_GROUP)).thenReturn(true);
        when(permissionManager.hasPermission(application, directory2, OperationType.UPDATE_GROUP)).thenReturn(true);

        // Try to add membership
        applicationService.addGroupToGroup(application, CHILD_GROUP_NAME, PARENT_GROUP_NAME);

        verify(directoryManager).addGroupToGroup(DIRECTORY1_ID, CHILD_GROUP_NAME, PARENT_GROUP_NAME);
        verify(directoryManager).addGroupToGroup(DIRECTORY2_ID, CHILD_GROUP_NAME, PARENT_GROUP_NAME);
    }

    @Test
    public void testAddGroupToGroup_AddToOne() throws Exception
    {
        final String PARENT_GROUP_NAME = "fruits";
        final String CHILD_GROUP_NAME = "bananas";
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));

        Group parentGroup = new GroupTemplate(PARENT_GROUP_NAME, DIRECTORY1_ID, GroupType.GROUP);
        Group childGroup = new GroupTemplate(CHILD_GROUP_NAME, DIRECTORY1_ID, GroupType.GROUP);
        // Find the Groups
        when(directoryManager.findGroupByName(DIRECTORY1_ID, PARENT_GROUP_NAME)).thenReturn(parentGroup);
        when(directoryManager.findGroupByName(DIRECTORY1_ID, CHILD_GROUP_NAME)).thenReturn(childGroup);
        // Check for Circular reference
        when(directoryManager.isGroupNestedGroupMember(DIRECTORY1_ID, PARENT_GROUP_NAME, CHILD_GROUP_NAME)).thenReturn(false);
        when(directoryManager.isGroupNestedGroupMember(DIRECTORY2_ID, PARENT_GROUP_NAME, CHILD_GROUP_NAME)).thenReturn(false);
        // Try to add membership
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_GROUP)).thenReturn(true);
        when(permissionManager.hasPermission(application, directory2, OperationType.UPDATE_GROUP)).thenReturn(true);

        doThrow(new NestedGroupsNotSupportedException(DIRECTORY2_ID)).when(directoryManager).addGroupToGroup(DIRECTORY2_ID, CHILD_GROUP_NAME, PARENT_GROUP_NAME);

        applicationService.addGroupToGroup(application, CHILD_GROUP_NAME, PARENT_GROUP_NAME);
        verify(directoryManager).addGroupToGroup(DIRECTORY2_ID, CHILD_GROUP_NAME, PARENT_GROUP_NAME);
    }

    @Test(expected = ApplicationPermissionException.class)
    public void testAddGroupToGroup_Fail() throws Exception
    {
        final String PARENT_GROUP_NAME = "fruits";
        final String CHILD_GROUP_NAME = "bananas";
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));

        Group parentGroup = new GroupTemplate(PARENT_GROUP_NAME, directory1.getId(), GroupType.GROUP);
        Group childGroup = new GroupTemplate(CHILD_GROUP_NAME, directory1.getId(), GroupType.GROUP);
        // Find the Groups
        when(directoryManager.findGroupByName(DIRECTORY1_ID, PARENT_GROUP_NAME)).thenReturn(parentGroup);
        when(directoryManager.findGroupByName(DIRECTORY1_ID, CHILD_GROUP_NAME)).thenReturn(childGroup);
        // Check for Circular reference
        when(directoryManager.isGroupNestedGroupMember(DIRECTORY1_ID, PARENT_GROUP_NAME, CHILD_GROUP_NAME)).thenReturn(false);
        when(directoryManager.isGroupNestedGroupMember(DIRECTORY2_ID, PARENT_GROUP_NAME, CHILD_GROUP_NAME)).thenReturn(false);
        // Try to add membership
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_GROUP)).thenReturn(false);
        when(permissionManager.hasPermission(application, directory2, OperationType.UPDATE_GROUP)).thenReturn(true);
        doThrow(new NestedGroupsNotSupportedException(DIRECTORY1_ID)).when(directoryManager).addGroupToGroup(DIRECTORY2_ID, CHILD_GROUP_NAME, PARENT_GROUP_NAME);

        applicationService.addGroupToGroup(application, "bananas", "fruits");
    }

    /**
     * Tests that {@link ApplicationService#isGroupDirectGroupMember(com.atlassian.crowd.model.application.Application, String, String)}
     * returns true if the group is a child of the parent group in <b>any</b> of the active directories.
     */
    @Test
    public void testIsGroupDirectGroupMember_IsMember() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.isGroupDirectGroupMember(DIRECTORY1_ID, GROUP1_NAME, GROUP2_NAME)).thenReturn(true);
        when(directoryManager.isGroupDirectGroupMember(DIRECTORY2_ID, GROUP1_NAME, GROUP3_NAME)).thenReturn(true);

        boolean isMember = applicationService.isGroupDirectGroupMember(application, GROUP1_NAME, GROUP2_NAME);
        assertTrue(isMember);

        isMember = applicationService.isGroupDirectGroupMember(application, GROUP1_NAME, GROUP3_NAME);
        assertTrue(isMember);
    }

    /**
     * Tests that {@link ApplicationService#isGroupDirectGroupMember(com.atlassian.crowd.model.application.Application, String, String)}
     * returns true if the group is a child of the parent group in <b>any</b> of the active directories.
     */
    @Test
    public void testIsGroupDirectGroupMember_NotAMember() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.isGroupDirectGroupMember(DIRECTORY1_ID, GROUP1_NAME, GROUP2_NAME)).thenReturn(false);
        when(directoryManager.isGroupDirectGroupMember(DIRECTORY2_ID, GROUP1_NAME, GROUP3_NAME)).thenReturn(false);

        boolean isMember = applicationService.isGroupDirectGroupMember(application, GROUP1_NAME, GROUP2_NAME);
        assertFalse(isMember);

        isMember = applicationService.isGroupDirectGroupMember(application, GROUP1_NAME, GROUP3_NAME);
        assertFalse(isMember);
    }

    /**
     * Tests that {@link ApplicationService#isGroupDirectGroupMember(com.atlassian.crowd.model.application.Application, String, String)}
     * returns true if the group is a child of the parent group in <b>any</b> of the active directories.
     */
    @Test
    public void testIsGroupNestedGroupMember() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.isGroupNestedGroupMember(DIRECTORY1_ID, GROUP1_NAME, GROUP2_NAME)).thenReturn(true);
        when(directoryManager.isGroupNestedGroupMember(DIRECTORY2_ID, GROUP2_NAME, GROUP3_NAME)).thenReturn(true);

        boolean isMember = applicationService.isGroupNestedGroupMember(application, GROUP1_NAME, GROUP2_NAME);
        assertTrue(isMember);

        isMember = applicationService.isGroupNestedGroupMember(application, GROUP2_NAME, GROUP3_NAME);
        assertTrue(isMember);

        // see EMBCWD-268
        isMember = applicationService.isGroupNestedGroupMember(application, GROUP1_NAME, GROUP3_NAME);
        assertFalse(isMember);
    }

    /**
     * Tests {@link ApplicationService#searchUsers} with duplicates.
     */
    @Test
    public void testSearchGroups_WithDuplicates() throws Exception
    {
        List<String> expectedGroups = Arrays.asList(GROUP1_NAME, GROUP2_NAME, GROUP3_NAME);
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchGroups(eq(DIRECTORY1_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(GROUP1_NAME, GROUP2_NAME));
        when(directoryManager.searchGroups(eq(DIRECTORY2_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(GROUP1_NAME, GROUP2_NAME, GROUP3_NAME));
        when(directoryManager.searchGroups(DIRECTORY2_ID, QueryBuilder.queryFor(String.class, EntityDescriptor.group()).returningAtMost(2))).thenReturn(Arrays.asList(GROUP1_NAME, GROUP2_NAME));
        when(directoryManager.searchGroups(DIRECTORY2_ID, QueryBuilder.queryFor(String.class, EntityDescriptor.group()).returningAtMost(1))).thenReturn(Arrays.asList(GROUP1_NAME));

        List<String> groups = applicationService.searchGroups(application, QueryBuilder.queryFor(String.class, EntityDescriptor.group()).returningAtMost(expectedGroups.size()));
        assertEquals(expectedGroups.size(), groups.size());
        Assert.assertTrue(groups.containsAll(expectedGroups));
    }
}

