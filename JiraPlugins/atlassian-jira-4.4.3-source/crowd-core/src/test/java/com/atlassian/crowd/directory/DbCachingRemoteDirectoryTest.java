package com.atlassian.crowd.directory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.directory.ldap.cache.DirectoryCacheFactory;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.ReadOnlyGroupException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.InternalEntityTemplate;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.group.InternalGroup;
import com.atlassian.crowd.model.group.InternalGroupWithAttributes;
import com.atlassian.crowd.model.user.InternalUser;
import com.atlassian.crowd.model.user.InternalUserWithAttributes;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithAttributes;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;

import com.google.common.collect.ImmutableSet;

import junit.framework.TestCase;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DbCachingRemoteDirectoryTest extends TestCase
{
    private DirectoryDao directoryDao;
    private Directory directory;
    private RemoteDirectory remoteDirectory;
    private InternalRemoteDirectory internalDirectory;
    private DirectoryCacheFactory directoryCacheFactory;
    private DbCachingRemoteDirectory dbCachingRemoteDirectory;

     private static final String USERNAME = "username";
    private static final InternalUser USER = new InternalUser(new UserTemplateWithCredentialAndAttributes(USERNAME, 0L, PasswordCredential.NONE), new DirectoryImpl(new InternalEntityTemplate(0L, "", true, new Date(), new Date())));
    private static final InternalUserWithAttributes USER_WITH_ATTRIBUTES = new InternalUserWithAttributes(USER, Collections.EMPTY_MAP);

    private static final String GROUP_NAME = "group name";
    private static final InternalGroup GROUP = createInternalGroup(GROUP_NAME);

    private static final String GROUP2_NAME = "group2 name";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        directoryDao = mock(DirectoryDao.class);
        directory = mock(Directory.class);
        remoteDirectory = mock(RemoteDirectory.class);
        internalDirectory = mock(InternalRemoteDirectory.class);
        directoryCacheFactory = mock(DirectoryCacheFactory.class);
        dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        when(directoryDao.findById(anyLong())).thenReturn(directory);
    }

    /**
     * Tests that directory id is fetched from the remote directory.
     *
     * @throws Exception if the test fails
     */
    public void testGetDirectoryId() throws Exception
    {
        when(remoteDirectory.getDirectoryId()).thenReturn(0L);

        assertEquals(0L, dbCachingRemoteDirectory.getDirectoryId());

        verify(internalDirectory, never()).getDirectoryId();
    }

    /**
     * Tests that descriptive name is fetched from the remote directory.
     *
     * @throws Exception if the test fails
     */
    public void testGetDescriptiveName() throws Exception
    {
        when(remoteDirectory.getDescriptiveName()).thenReturn("name");

        assertEquals("name", dbCachingRemoteDirectory.getDescriptiveName());

        verify(internalDirectory, never()).getDescriptiveName();
    }

    /**
     * Tests that users are fetched from the internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testFindUserByName() throws Exception
    {
        when(internalDirectory.findUserByName(USERNAME)).thenReturn(USER);

        assertEquals(USER, dbCachingRemoteDirectory.findUserByName(USERNAME));

        verify(remoteDirectory, never()).findUserByName(any(String.class));
    }

    /**
     * Tests that users with attributes are fetched from the internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testFindUserWithAttributesByName() throws Exception
    {
        when(internalDirectory.findUserWithAttributesByName(USERNAME)).thenReturn(USER_WITH_ATTRIBUTES);

        assertEquals(USER, dbCachingRemoteDirectory.findUserWithAttributesByName(USERNAME));

        verify(remoteDirectory, never()).findUserWithAttributesByName(any(String.class));
    }


    public void testAuthenticateInactiveUser() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();
        // Construct the class under test
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        // Add active user to remote directory
        UserTemplateWithAttributes user = createUser("fred", "Fred Normal");
        user.setActive(true);
        remoteDirectory.addUser(user, null);

        // Add inactive user to internal directory
        user.setActive(false);
        internalDirectory.addUser(user, null);

        try
        {
            dbCachingRemoteDirectory.authenticate("fred", new PasswordCredential(""));
            fail("Authentication succeeded with inactive account.");
        }
        catch (InactiveAccountException e)
        {
            // Success
        }
    }

    public void testAuthenticateNotSynchronisedUser() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();
        // Construct the class under test
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        // Add active user to remote directory
        UserTemplateWithAttributes user = createUser("fred", "Fred Normal");
        user.setActive(true);
        remoteDirectory.addUser(user, null);

        dbCachingRemoteDirectory.authenticate("fred", new PasswordCredential(""));
    }

    /**
     * Tests that DbCachingRemoteDirectory adds missing user on successful
     * authentication.
     *
     * @throws Exception if the test fails
     */
    public void testAuthenticateUser_CreatesMissingUser() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = mock(RemoteDirectory.class);
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();
        // Construct the class under test
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        // Remote directory authenticates fred.
        UserTemplateWithAttributes user = createUser("fred", "Fred Normal");
        user.setActive(true);
        when(remoteDirectory.authenticate(any(String.class), any(PasswordCredential.class))).thenReturn(user);

        dbCachingRemoteDirectory.authenticate("fred", new PasswordCredential(""));

        // Check that user can be found.
        assertEquals(user, dbCachingRemoteDirectory.findUserByName("fred"));
    }

    /**
     * Tests that DbCachingRemoteDirectory adds missing user and user's group
     * memberships on successful authentication.
     *
     * @throws Exception if the test fails
     */
    public void testAuthenticateUser_CreatesMissingUserWithGroupMemberships() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = mock(RemoteDirectory.class);
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();
        // Construct the class under test
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        // Remote directory authenticates fred.
        UserTemplateWithAttributes user = createUser("fred", "Fred Normal");
        user.setActive(true);
        when(remoteDirectory.authenticate(any(String.class), any(PasswordCredential.class))).thenReturn(user);
        when(remoteDirectory.searchGroupRelationships(any(MembershipQuery.class))).thenReturn(Arrays.asList("group1", "group2"));

        dbCachingRemoteDirectory.authenticate("fred", new PasswordCredential(""));

        // Check that user can be found.
        assertEquals(user, dbCachingRemoteDirectory.findUserByName("fred"));
        assertTrue(dbCachingRemoteDirectory.isUserDirectGroupMember("fred", "group1"));
        assertTrue(dbCachingRemoteDirectory.isUserDirectGroupMember("fred", "group2"));
    }

    /**
     * Tests that DbCachingRemoteDirectory does not create user when the
     * authentication fails.
     *
     * @throws Exception if the test fails
     */
    public void testAuthenticateUser_FailsWithNoUserCreation() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = mock(RemoteDirectory.class);
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();
        // Construct the class under test
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        // Remote directory authenticates fred.
        UserTemplateWithAttributes user = createUser("fred", "Fred Normal");
        user.setActive(true);
        when(remoteDirectory.authenticate(any(String.class), any(PasswordCredential.class))).thenThrow(new InvalidAuthenticationException("fred"));
        when(remoteDirectory.searchGroupRelationships(any(MembershipQuery.class))).thenReturn(Arrays.asList("group1", "group2"));

        try
        {
            dbCachingRemoteDirectory.authenticate("fred", new PasswordCredential(""));
            fail("Authentication passed");
        }
        catch (UserNotFoundException e)
        {
            // Success: authentication failed -> user was not created -> UserNotFoundException thrown
        }

        // Check that user was not added.
        try
        {
            dbCachingRemoteDirectory.findUserByName("fred");
            fail("User fred was found");
        }
        catch (UserNotFoundException e)
        {
            // Success
        }
    }

    /**
     * Tests that users are added to both directories.
     *
     * @throws Exception if the test fails
     */
    public void testAddUser() throws Exception
    {
        final UserTemplate userTemplate = new UserTemplate(USERNAME);

        when(remoteDirectory.addUser(userTemplate, PasswordCredential.NONE)).thenReturn(USER);
        when(internalDirectory.addUser(new UserTemplate(USER), PasswordCredential.encrypted(DbCachingRemoteDirectory.INTERNAL_USER_PASSWORD))).thenReturn(USER);

        assertEquals(USER, dbCachingRemoteDirectory.addUser(userTemplate, PasswordCredential.NONE));
    }

    /**
     * Tests that users are updated in both directories.
     *
     * @throws Exception if the test fails
     */
    public void testUpdateUser() throws Exception
    {
        final UserTemplate userTemplate = new UserTemplate(USERNAME);

        when(remoteDirectory.updateUser(userTemplate)).thenReturn(USER);
        when(remoteDirectory.supportsInactiveAccounts()).thenReturn(true);
        when(internalDirectory.updateUser(new UserTemplate(USER))).thenReturn(USER);

        assertEquals(USER, dbCachingRemoteDirectory.updateUser(userTemplate));
    }

    /**
     * Tests that local users are updated in the internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testUpdateUser_LocalActiveFlag() throws Exception
    {
        final UserTemplate originalUserTemplate = new UserTemplate(USERNAME);
        originalUserTemplate.setActive(false);

        when(remoteDirectory.updateUser(originalUserTemplate)).thenReturn(USER);
        when(remoteDirectory.supportsInactiveAccounts()).thenReturn(false);

        final UserTemplate addedUserTemplate = new UserTemplate(USER);
        addedUserTemplate.setActive(false);
        when(internalDirectory.updateUser(addedUserTemplate)).thenReturn(USER);

        assertEquals(USER, dbCachingRemoteDirectory.updateUser(originalUserTemplate));
    }

    /**
     * Tests that user credential is updated in the remote directory.
     *
     * @throws Exception if the test fails
     */
    public void testUpdateUserCredential() throws Exception
    {
        dbCachingRemoteDirectory.updateUserCredential(USERNAME, PasswordCredential.NONE);

        verify(remoteDirectory).updateUserCredential(USERNAME, PasswordCredential.NONE);
        verify(internalDirectory, never()).updateUserCredential(any(String.class), any(PasswordCredential.class));
    }

    public void testStoreUserAttributes() throws Exception
    {
        // Create mocks for the underlying directories
        final MockRemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();
        // Construct the class under test
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        UserTemplateWithAttributes user = createUser("fred", "Fred Normal");

        // Add user to remote and internal directory
        remoteDirectory.addUser(user, null);
        internalDirectory.addUser(user, null);
        // Set a test attribute
        remoteDirectory.storeUserAttributes("fred", Collections.singletonMap("testKey", Collections.singleton("testValue")));
        internalDirectory.storeUserAttributes("fred", Collections.singletonMap("testKey", Collections.singleton("testValue")));

        // Update the test attribute
        dbCachingRemoteDirectory.storeUserAttributes("fred", Collections.singletonMap("testKey", Collections.singleton("newTestValue")));

        UserWithAttributes remoteUser = remoteDirectory.findUserWithAttributesByName("fred");
        assertEquals("Remote directory attributes should not be updated", "testValue", remoteUser.getValue("testKey"));
        UserWithAttributes internalUser = internalDirectory.findUserWithAttributesByName("fred");
        assertEquals("newTestValue", internalUser.getValue("testKey"));
    }

    public void testRemoveUserAttributes() throws Exception
    {
        // Create mocks for the underlying directories
        final MockRemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();
        // Construct the class under test
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        UserTemplateWithAttributes user = createUser("fred", "Fred Normal");

        // Add user to remote and internal directory
        remoteDirectory.addUser(user, null);
        internalDirectory.addUser(user, null);
        // Set a test attribute
        remoteDirectory.storeUserAttributes("fred", Collections.singletonMap("testKey", Collections.singleton("testValue")));
        internalDirectory.storeUserAttributes("fred", Collections.singletonMap("testKey", Collections.singleton("testValue")));

        // Remove the test attribute
        dbCachingRemoteDirectory.removeUserAttributes("fred", "testKey");

        UserWithAttributes remoteUser = remoteDirectory.findUserWithAttributesByName("fred");
        assertEquals("Remote directory attributes should not be removed", "testValue", remoteUser.getValue("testKey"));
        UserWithAttributes internalUser = internalDirectory.findUserWithAttributesByName("fred");
        assertNull("User attribute has not been removed", internalUser.getValue("testKey"));
    }

    /**
     * Tests that users are removed from both directories.
     *
     * @throws Exception if the test fails
     */
    public void testRemoveUser() throws Exception
    {
        dbCachingRemoteDirectory.removeUser(USERNAME);

        verify(remoteDirectory).removeUser(USERNAME);
        verify(internalDirectory).removeUser(USERNAME);
    }

    /**
     * Tests that a user is removed from the internal directory even if the
     * user does not exist in the remote directory.
     *
     * @throws Exception if the test fails
     */
    public void testRemoveUser_UserNotFound() throws Exception
    {
        doThrow(new UserNotFoundException(USERNAME)).when(remoteDirectory).removeUser(USERNAME);

        try
        {
            dbCachingRemoteDirectory.removeUser(USERNAME);
            fail();
        }
        catch (UserNotFoundException e)
        {
            // Success
        }

        verify(remoteDirectory).removeUser(USERNAME);
        verify(internalDirectory).removeUser(USERNAME);
    }

    /**
     * Tests that users are searched from the internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testSearchUsers() throws Exception
    {
        final EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).returningAtMost(EntityQuery.ALL_RESULTS);
        final List<String> results = Arrays.asList(USERNAME);

        when(internalDirectory.searchUsers(query)).thenReturn(results);

        assertEquals(results, dbCachingRemoteDirectory.searchUsers(query));

        verify(remoteDirectory, never()).searchUsers(any(EntityQuery.class));
    }

    /**
     * Tests that groups are fetched from the internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testFindGroupByName() throws Exception
    {
        when(internalDirectory.findGroupByName(GROUP_NAME)).thenReturn(GROUP);

        assertEquals(GROUP, dbCachingRemoteDirectory.findGroupByName(GROUP_NAME));

        verify(remoteDirectory, never()).findGroupByName(any(String.class));
    }

    /**
     * Tests that groups with attributes are fetched from the internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testFindGroupWithAttributesByName() throws Exception
    {
        final GroupWithAttributes groupWithAttributes = new InternalGroupWithAttributes(GROUP, Collections.EMPTY_MAP);

        when(internalDirectory.findGroupWithAttributesByName(GROUP_NAME)).thenReturn(groupWithAttributes);

        assertEquals(groupWithAttributes, dbCachingRemoteDirectory.findGroupWithAttributesByName(GROUP_NAME));

        verify(remoteDirectory, never()).findGroupWithAttributesByName(any(String.class));
    }

    /**
     * Tests that groups are added to both directories.
     *
     * @throws Exception if the test fails
     */
    public void testAddGroup() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups disabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.FALSE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        final GroupTemplate groupTemplate = new GroupTemplate(GROUP_NAME);

        when(remoteDirectory.addGroup(groupTemplate)).thenReturn(GROUP);
        when(internalDirectory.addGroup(new GroupTemplate(GROUP))).thenReturn(GROUP);

        assertEquals(GROUP, dbCachingRemoteDirectory.addGroup(groupTemplate));
    }

    /**
     * Tests that local groups are added to internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testAddGroup_LocalGroups() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups enabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.TRUE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        when(remoteDirectory.findGroupByName(GROUP_NAME)).thenThrow(new GroupNotFoundException(GROUP_NAME));

        final GroupTemplate groupTemplate = new GroupTemplate(GROUP);
        when(internalDirectory.addLocalGroup(groupTemplate)).thenReturn(GROUP);

        assertEquals(GROUP, dbCachingRemoteDirectory.addGroup(groupTemplate));
    }

    /**
     * Tests that a group cannot be added if it exists in the remote directory.
     *
     * @throws Exception if the test fails
     */
    public void testAddGroup_LocalGroupsDuplicate() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups enabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.TRUE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        when(remoteDirectory.findGroupByName(GROUP_NAME)).thenReturn(GROUP);

        try
        {
            dbCachingRemoteDirectory.addGroup(new GroupTemplate(GROUP));
            fail();
        }
        catch (InvalidGroupException e)
        {
            // Success
        }
    }

    /**
     * Tests that groups are updated in both directories.
     *
     * @throws Exception if the test fails
     */
    public void testUpdateGroup() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups disabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.FALSE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        final GroupTemplate groupTemplate = new GroupTemplate(GROUP_NAME);

        when(remoteDirectory.updateGroup(groupTemplate)).thenReturn(GROUP);
        when(internalDirectory.updateGroup(new GroupTemplate(GROUP))).thenReturn(GROUP);

        assertEquals(GROUP, dbCachingRemoteDirectory.updateGroup(groupTemplate));
    }

    /**
     * Tests that local groups are updated in the internal directory
     *
     * @throws Exception if the test fails
     */
    public void testUpdateGroup_LocalGroups() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups enabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.TRUE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        when(remoteDirectory.findGroupByName(GROUP_NAME)).thenThrow(new GroupNotFoundException(GROUP_NAME));

        final InternalGroup localGroup = new InternalGroup(GROUP, GROUP.getDirectory());
        localGroup.setLocal(true);
        when(internalDirectory.findGroupByName(GROUP_NAME)).thenReturn(localGroup);

        final GroupTemplate groupTemplate = new GroupTemplate(localGroup);
        when(internalDirectory.updateGroup(groupTemplate)).thenReturn(localGroup);

        assertEquals(localGroup, dbCachingRemoteDirectory.updateGroup(groupTemplate));
    }

    /**
     * Tests that groups existing in the remote directory cannot be updated when local groups are enabled.
     *
     * @throws Exception if the test fails
     */
    public void testUpdateGroup_LocalGroupsReadOnly() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups enabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.TRUE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        when(remoteDirectory.findGroupByName(GROUP_NAME)).thenReturn(GROUP);

        try
        {
            dbCachingRemoteDirectory.updateGroup(new GroupTemplate(GROUP));
            fail();
        }
        catch (ReadOnlyGroupException e)
        {
            // Success
        }
    }

    /**
     * Tests that groups attributes are stored in the internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testStoreGroupAttributes() throws Exception
    {
        dbCachingRemoteDirectory.storeGroupAttributes(GROUP_NAME, Collections.EMPTY_MAP);

        verify(internalDirectory).storeGroupAttributes(GROUP_NAME, Collections.EMPTY_MAP);
        verify(remoteDirectory, never()).storeGroupAttributes(any(String.class), any(Map.class));
    }

    /**
     * Tests that groups attributes are removed in the internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testRemoveGroupAttributes() throws Exception
    {
        final String key = "key";

        dbCachingRemoteDirectory.removeGroupAttributes(GROUP_NAME, key);

        verify(internalDirectory).removeGroupAttributes(GROUP_NAME, key);
        verify(remoteDirectory, never()).removeGroupAttributes(any(String.class), any(String.class));
    }

    /**
     * Tests that groups are removed from both directories.
     *
     * @throws Exception if the test fails
     */
    public void testRemoveGroup() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups disabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.FALSE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        dbCachingRemoteDirectory.removeGroup(GROUP_NAME);

        verify(remoteDirectory).removeGroup(GROUP_NAME);
        verify(internalDirectory).removeGroup(GROUP_NAME);
    }

    /**
     * Tests that a group is removed from internal directory even if it doesn't
     * exist in the remote directory.
     *
     * @throws Exception if the test fails
     */
    public void testRemoveGroup_GroupNotFound() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups disabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.FALSE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        doThrow(new GroupNotFoundException(GROUP_NAME)).when(remoteDirectory).removeGroup(GROUP_NAME);

        try
        {
            dbCachingRemoteDirectory.removeGroup(GROUP_NAME);
            fail();
        }
        catch (GroupNotFoundException e)
        {
            // Success
        }

        verify(remoteDirectory).removeGroup(GROUP_NAME);
        verify(internalDirectory).removeGroup(GROUP_NAME);
    }

    /**
     * Tests that local groups are removed from internal directories.
     *
     * @throws Exception if the test fails
     */
    public void testRemoveGroup_LocalGroups() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups enabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.TRUE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        when(remoteDirectory.findGroupByName(GROUP_NAME)).thenThrow(new GroupNotFoundException(GROUP_NAME));

        dbCachingRemoteDirectory.removeGroup(GROUP_NAME);

        verify(internalDirectory).removeGroup(GROUP_NAME);
    }

    /**
     * Tests that a local group cannot be removed if it exists in the remote directory.
     *
     * @throws Exception if the test fails
     */
    public void testRemoveGroup_LocalGroupsReadOnly() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups enabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.TRUE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        when(remoteDirectory.findGroupByName(GROUP_NAME)).thenReturn(GROUP);

        try
        {
            dbCachingRemoteDirectory.removeGroup(GROUP_NAME);
            fail();
        }
        catch (ReadOnlyGroupException e)
        {
            // Success
        }
    }

    /**
     * Tests that groups are searched from the internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testSearchGroups() throws Exception
    {
        final EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).returningAtMost(EntityQuery.ALL_RESULTS);
        final List<String> results = Arrays.asList(GROUP_NAME);

        when(internalDirectory.searchGroups(query)).thenReturn(results);

        assertEquals(results, dbCachingRemoteDirectory.searchGroups(query));

        verify(remoteDirectory, never()).searchGroups(any(EntityQuery.class));
    }

    /**
     * Tests that user membership queries are performed on the internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testIsUserDirectGroupMember() throws Exception
    {
        when(internalDirectory.isUserDirectGroupMember(USERNAME, GROUP_NAME)).thenReturn(true);

        assertTrue(dbCachingRemoteDirectory.isUserDirectGroupMember(USERNAME, GROUP_NAME));

        verify(remoteDirectory, never()).isUserDirectGroupMember(any(String.class), any(String.class));
    }

    /**
     * Tests that group membership queries are performed on the internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testIsGroupDirectGroupMember() throws Exception
    {
        when(internalDirectory.isGroupDirectGroupMember(GROUP2_NAME, GROUP_NAME)).thenReturn(true);

        assertTrue(dbCachingRemoteDirectory.isGroupDirectGroupMember(GROUP2_NAME, GROUP_NAME));

        verify(remoteDirectory, never()).isGroupDirectGroupMember(any(String.class), any(String.class));
    }

    /**
     * Tests that user memberships are added to both directories.
     *
     * @throws Exception if the test fails
     */
    public void testAddUserToGroup() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups disabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.FALSE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        dbCachingRemoteDirectory.addUserToGroup(USERNAME, GROUP_NAME);

        verify(remoteDirectory).addUserToGroup(USERNAME, GROUP_NAME);
        verify(internalDirectory).addUserToGroup(USERNAME, GROUP_NAME);
    }

    /**
     * Tests that user membership to a local group is added to the internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testAddUserToGroup_LocalGroups() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups enabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.TRUE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        when(remoteDirectory.findGroupByName(GROUP_NAME)).thenThrow(new GroupNotFoundException(GROUP_NAME));

        final InternalGroup localGroup = new InternalGroup(GROUP, GROUP.getDirectory());
        localGroup.setLocal(true);
        when(internalDirectory.findGroupByName(GROUP_NAME)).thenReturn(localGroup);

        dbCachingRemoteDirectory.addUserToGroup(USERNAME, GROUP_NAME);

        verify(remoteDirectory, never()).addUserToGroup(any(String.class), any(String.class));
        verify(internalDirectory).addUserToGroup(USERNAME, GROUP_NAME);
    }

    /**
     * Tests that user membership cannot be added to a group in remote
     * directory if local groups are enabled.
     *
     * @throws Exception if the test fails
     */
    public void testAddUserToGroup_LocalGroupsReadOnly() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups enabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.TRUE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        when(remoteDirectory.findGroupByName(GROUP_NAME)).thenReturn(GROUP);

        try
        {
            dbCachingRemoteDirectory.addUserToGroup(USERNAME, GROUP_NAME);
            fail();
        }
        catch (ReadOnlyGroupException e)
        {
            // Success
        }
    }

    /**
     * Tests that group memberships are added to both directories.
     *
     * @throws Exception if the test fails
     */
    public void testAddGroupToGroup() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups disabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.FALSE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        dbCachingRemoteDirectory.addGroupToGroup(GROUP2_NAME, GROUP_NAME);

        verify(remoteDirectory).addGroupToGroup(GROUP2_NAME, GROUP_NAME);
        verify(internalDirectory).addGroupToGroup(GROUP2_NAME, GROUP_NAME);
    }

    /**
     * Tests that group membership to a local group is added to the internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testAddGroupToGroup_LocalGroups() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups enabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.TRUE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        when(remoteDirectory.findGroupByName(GROUP_NAME)).thenThrow(new GroupNotFoundException(GROUP_NAME));
        when(remoteDirectory.findGroupByName(GROUP2_NAME)).thenThrow(new GroupNotFoundException(GROUP2_NAME));

        final InternalGroup localGroup = createInternalGroup(GROUP_NAME);
        localGroup.setLocal(true);
        final InternalGroup localGroup2 = createInternalGroup(GROUP2_NAME);
        localGroup2.setLocal(true);

        when(internalDirectory.findGroupByName(GROUP_NAME)).thenReturn(localGroup);
        when(internalDirectory.findGroupByName(GROUP2_NAME)).thenReturn(localGroup2);

        dbCachingRemoteDirectory.addGroupToGroup(GROUP2_NAME, GROUP_NAME);

        verify(remoteDirectory, never()).addGroupToGroup(any(String.class), any(String.class));
        verify(internalDirectory).addGroupToGroup(GROUP2_NAME, GROUP_NAME);
    }

    /**
     * Tests that group membership cannot be added to a group in remote
     * directory if local groups are enabled.
     *
     * @throws Exception if the test fails
     */
    public void testAddGroupToGroup_LocalGroupsReadOnly() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups enabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.TRUE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        when(remoteDirectory.findGroupByName(GROUP_NAME)).thenReturn(GROUP);

        try
        {
            dbCachingRemoteDirectory.addGroupToGroup(GROUP2_NAME, GROUP_NAME);
            fail();
        }
        catch (ReadOnlyGroupException e)
        {
            // Success
        }
    }

    /**
     * Tests that user memberships are removed from both directories.
     *
     * @throws Exception if the test fails
     */
    public void testRemoveUserFromGroup() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups disabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.FALSE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        dbCachingRemoteDirectory.removeUserFromGroup(USERNAME, GROUP_NAME);

        verify(remoteDirectory).removeUserFromGroup(USERNAME, GROUP_NAME);
        verify(internalDirectory).removeUserFromGroup(USERNAME, GROUP_NAME);
    }

    /**
     * Tests that user membership to a local group is removed from the internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testRemoveUserFromGroup_LocalGroups() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups enabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.TRUE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        when(remoteDirectory.findGroupByName(GROUP_NAME)).thenThrow(new GroupNotFoundException(GROUP_NAME));

        final InternalGroup localGroup = new InternalGroup(GROUP, GROUP.getDirectory());
        localGroup.setLocal(true);
        when(internalDirectory.findGroupByName(GROUP_NAME)).thenReturn(localGroup);

        dbCachingRemoteDirectory.removeUserFromGroup(USERNAME, GROUP_NAME);

        verify(remoteDirectory, never()).removeUserFromGroup(any(String.class), any(String.class));
        verify(internalDirectory).removeUserFromGroup(USERNAME, GROUP_NAME);
    }

    /**
     * Tests that user membership cannot be removed from a group in remote
     * directory if local groups are enabled.
     *
     * @throws Exception if the test fails
     */
    public void testRemoveUserFromGroup_LocalGroupsReadOnly() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups enabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.TRUE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        when(remoteDirectory.findGroupByName(GROUP_NAME)).thenReturn(GROUP);

        try
        {
            dbCachingRemoteDirectory.removeUserFromGroup(USERNAME, GROUP_NAME);
            fail();
        }
        catch (ReadOnlyGroupException e)
        {
            // Success
        }
    }

    /**
     * Tests that group memberships are removed from both directories.
     *
     * @throws Exception if the test fails
     */
    public void testRemoveGroupToGroup() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups disabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.FALSE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        dbCachingRemoteDirectory.removeGroupFromGroup(GROUP2_NAME, GROUP_NAME);

        verify(remoteDirectory).removeGroupFromGroup(GROUP2_NAME, GROUP_NAME);
        verify(internalDirectory).removeGroupFromGroup(GROUP2_NAME, GROUP_NAME);
    }

    /**
     * Tests that group membership to a local group is removed from the internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testRemoveGroupToGroup_LocalGroups() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups enabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.TRUE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        when(remoteDirectory.findGroupByName(GROUP_NAME)).thenThrow(new GroupNotFoundException(GROUP_NAME));
        when(remoteDirectory.findGroupByName(GROUP2_NAME)).thenThrow(new GroupNotFoundException(GROUP2_NAME));

        final InternalGroup localGroup = createInternalGroup(GROUP_NAME);
        localGroup.setLocal(true);
        final InternalGroup localGroup2 = createInternalGroup(GROUP2_NAME);
        localGroup2.setLocal(true);

        when(internalDirectory.findGroupByName(GROUP_NAME)).thenReturn(localGroup);
        when(internalDirectory.findGroupByName(GROUP2_NAME)).thenReturn(localGroup2);

        dbCachingRemoteDirectory.removeGroupFromGroup(GROUP2_NAME, GROUP_NAME);

        verify(remoteDirectory, never()).removeGroupFromGroup(any(String.class), any(String.class));
        verify(internalDirectory).removeGroupFromGroup(GROUP2_NAME, GROUP_NAME);
    }

    /**
     * Tests that group membership cannot be removed from a group in remote
     * directory if local groups are enabled.
     *
     * @throws Exception if the test fails
     */
    public void testRemoveGroupFromGroup_LocalGroupsReadOnly() throws Exception
    {
        // Construct DbCachingRemoteDirectory with local groups enabled
        when(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS)).thenReturn(Boolean.TRUE.toString());
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        when(remoteDirectory.findGroupByName(GROUP_NAME)).thenReturn(GROUP);

        try
        {
            dbCachingRemoteDirectory.removeGroupFromGroup(GROUP2_NAME, GROUP_NAME);
            fail();
        }
        catch (ReadOnlyGroupException e)
        {
            // Success
        }
    }

    /**
     * Tests that group relationship searches are performed against the
     * internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testSearchGroupRelationships() throws Exception
    {
        final MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(GROUP_NAME).returningAtMost(EntityQuery.ALL_RESULTS);
        final List<String> results = Arrays.asList(USERNAME);

        when(internalDirectory.searchGroupRelationships(query)).thenReturn(results);

        assertEquals(results, dbCachingRemoteDirectory.searchGroupRelationships(query));

        verify(remoteDirectory, never()).searchGroupRelationships(any(MembershipQuery.class));
    }

    /**
     * Tests that connection test is performed against the remote directory.
     *
     * @throws Exception if the test fails
     */
    public void testTestConnection() throws Exception
    {
        dbCachingRemoteDirectory.testConnection();

        verify(remoteDirectory).testConnection();
    }

    /**
     * Tests that inactive account support is read from the internal directory.
     *
     * @throws Exception if the test fails
     */
    public void testSupportsInactiveAccounts()
    {
        when(internalDirectory.supportsInactiveAccounts()).thenReturn(true);

        assertTrue(dbCachingRemoteDirectory.supportsInactiveAccounts());

        verify(internalDirectory).supportsInactiveAccounts();
    }

    /**
     * Tests that nested groups support is read from the remote directory.
     *
     * @throws Exception if the test fails
     */
    public void testSupportsNestedGroups()
    {
        when(remoteDirectory.supportsNestedGroups()).thenReturn(true);

        assertTrue(dbCachingRemoteDirectory.supportsNestedGroups());

        verify(remoteDirectory).supportsNestedGroups();
    }

    /**
     * Tests that roles disabled state is read from the remote directory.
     *
     * @throws Exception if the test fails
     */
    public void testIsRolesDisabled()
    {
        when(remoteDirectory.isRolesDisabled()).thenReturn(true);

        assertTrue(dbCachingRemoteDirectory.isRolesDisabled());

        verify(remoteDirectory).isRolesDisabled();
    }

    /**
     * Tests that attribute values are read from the remote directory.
     *
     * @throws Exception if the test fails
     */
    public void testGetValues()
    {
        final String key = "key";
        final Set<String> values = ImmutableSet.of("value");

        when(remoteDirectory.getValues(key)).thenReturn(values);

        assertEquals(values, dbCachingRemoteDirectory.getValues(key));
    }

    /**
     * Tests that attribute value is read from the remote directory.
     *
     * @throws Exception if the test fails
     */
    public void testGetValue()
    {
        final String key = "key";
        final String value = "value";

        when(remoteDirectory.getValue(key)).thenReturn(value);

        assertEquals(value, dbCachingRemoteDirectory.getValue(key));
    }

    /**
     * Tests that attribute emptiness is read from the remote directory.
     *
     * @throws Exception if the test fails
     */
    public void testIsEmpty()
    {
        when(remoteDirectory.isEmpty()).thenReturn(true);

        assertTrue(dbCachingRemoteDirectory.isEmpty());

        verify(remoteDirectory).isEmpty();
    }

    /**
     * Tests that attribute keys are read from the remote directory.
     *
     * @throws Exception if the test fails
     */
    public void testGetKeys()
    {
        final Set<String> keys = ImmutableSet.of("key");

        when(remoteDirectory.getKeys()).thenReturn(keys);

        assertEquals(keys, dbCachingRemoteDirectory.getKeys());
    }

    private static UserTemplateWithAttributes createUser(final String username, String displayName)
    {
        final UserTemplateWithAttributes user = new UserTemplateWithAttributes(username, 1);
        user.setDisplayName(displayName);
        return user;
    }

    private static InternalGroup createInternalGroup(String groupName)
    {
        GroupTemplate groupTemplate = new GroupTemplate(groupName, 1L, GroupType.GROUP);
        InternalEntityTemplate directoryTemplate = new InternalEntityTemplate(1L, "directoryName", true, null, null);
        return new InternalGroup(groupTemplate, new DirectoryImpl(directoryTemplate));
    }
}
