package com.atlassian.crowd.directory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import com.atlassian.crowd.directory.event.MockEventPublisher;
import com.atlassian.crowd.directory.ldap.cache.DirectoryCache;
import com.atlassian.crowd.directory.ldap.cache.DirectoryCacheFactory;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.event.group.GroupCreatedEvent;
import com.atlassian.crowd.event.group.GroupDeletedEvent;
import com.atlassian.crowd.event.group.GroupUpdatedEvent;
import com.atlassian.crowd.event.user.UserCreatedEvent;
import com.atlassian.crowd.event.user.UserDeletedEvent;
import com.atlassian.crowd.event.user.UserUpdatedEvent;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.directory.SynchronisationStatusManager;
import com.atlassian.crowd.model.InternalEntityTemplate;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.InternalGroup;
import com.atlassian.crowd.model.user.InternalUser;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithAttributes;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.event.api.EventPublisher;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import junit.framework.TestCase;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DbCachingRemoteDirectoryCacheTest extends TestCase
{
    private DirectoryDao directoryDao;
    private Directory directory;
    private RemoteDirectory remoteDirectory;
    private InternalRemoteDirectory internalDirectory;
    private EventPublisher eventPublisher;
    private DirectoryCacheFactory directoryCacheFactory;
    private SynchronisationStatusManager syncStatusManager;

    private static final String LONG_STRING = StringUtils.repeat("x", 300);
    private static final String TRUNCATED_STRING = StringUtils.repeat("x", 252).concat("...");

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        directoryDao = mock(DirectoryDao.class);
        directory = mock(Directory.class);
        remoteDirectory = mock(RemoteDirectory.class);
        internalDirectory = mock(InternalRemoteDirectory.class);
        eventPublisher = mock(EventPublisher.class);
        directoryCacheFactory = mock(DirectoryCacheFactory.class);
        syncStatusManager = mock(SynchronisationStatusManager.class);

        when(directoryDao.findById(anyLong())).thenReturn(directory);
        when(directoryCacheFactory.createDirectoryCache(remoteDirectory, internalDirectory)).thenReturn(
                newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher));
    }

    DirectoryCache newDirectoryCache(DirectoryDao directoryDao,
                                     RemoteDirectory remoteDirectory,
                                     InternalRemoteDirectory internalDirectory,
                                     SynchronisationStatusManager synchronisationStatusManager,
                                     EventPublisher eventPublisher)
    {
        DirectoryCacheChangeOperations ops = new DbCachingRemoteChangeOperations(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);

        return new DirectoryCacheImplUsingChangeOperations(ops);
    }

    public void testAddOrUpdateCachedUser() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();

        when(directory.getValue(SynchronisableDirectoryProperties.IS_SYNCHRONISING)).thenReturn(Boolean.FALSE.toString());

        // Construct the class under test
        DirectoryCache dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);
        // Do a user create
        dbCachingRemoteDirectoryCache.addOrUpdateCachedUsers(Arrays.asList(createUser("fred", "Fred Normal")), null);
        assertEquals("Fred Normal", internalDirectory.findUserByName("fred").getDisplayName());
        verify(eventPublisher).publish(isA(UserCreatedEvent.class));
        // Do a user update
        dbCachingRemoteDirectoryCache.addOrUpdateCachedUsers(Arrays.asList(createUser("fred", "Fred Super")), null);
        assertEquals("Fred Super", internalDirectory.findUserByName("fred").getDisplayName());
        verify(eventPublisher).publish(isA(UserUpdatedEvent.class));
    }

    public void testAddOrUpdateCachedUserUpdatesWhenUsernameCaseHasChanged() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();

        when(directory.getValue(SynchronisableDirectoryProperties.IS_SYNCHRONISING)).thenReturn(Boolean.FALSE.toString());

        // Construct the class under test
        DirectoryCache dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);
        // Do a user create
        dbCachingRemoteDirectoryCache.addOrUpdateCachedUsers(Arrays.asList(createUser("fred", "Fred Normal")), null);
        assertEquals("fred", internalDirectory.findUserByName("fred").getName());
        verify(eventPublisher).publish(isA(UserCreatedEvent.class));
        // Do a user update
        dbCachingRemoteDirectoryCache.addOrUpdateCachedUsers(Arrays.asList(createUser("Fred", "Fred Super")), null);
        assertEquals("Fred Super", internalDirectory.findUserByName("fred").getDisplayName());
        assertEquals("fred", internalDirectory.findUserByName("Fred").getName());
        verify(eventPublisher).publish(isA(UserUpdatedEvent.class));
    }

    /**
     * Tests that a remote user with a long string fields is not updated when
     * internal user with truncated string fields matches the remote user.
     */
    public void testAddOrUpdateCachedUserWithNoChanges() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final InternalRemoteDirectory internalDirectory = mock(InternalRemoteDirectory.class);

        // Construct the class under test
        DirectoryCache dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);

        final UserTemplate internalUserTemplate = createUser("fred", TRUNCATED_STRING);
        internalUserTemplate.setEmailAddress(TRUNCATED_STRING);
        internalUserTemplate.setFirstName(TRUNCATED_STRING);
        internalUserTemplate.setLastName(TRUNCATED_STRING);
        final Directory directory = new DirectoryImpl(new InternalEntityTemplate(1L, "", true, null, null));
        final InternalUser internalUser = new InternalUser(internalUserTemplate, directory, new PasswordCredential("", true));

        final UserTemplate remoteUser = createUser("fred", LONG_STRING);
        remoteUser.setEmailAddress(LONG_STRING);
        remoteUser.setFirstName(LONG_STRING);
        remoteUser.setLastName(LONG_STRING);

        when(internalDirectory.searchUsers(any(EntityQuery.class))).thenReturn(ImmutableList.of(internalUser));

        dbCachingRemoteDirectoryCache.addOrUpdateCachedUsers(Arrays.asList(remoteUser), null);

        verify(internalDirectory, never()).addAllUsers(any(Set.class));
        verify(internalDirectory, never()).updateUser(any(UserTemplate.class));
        verify(eventPublisher, never()).publish(any());
    }

    public void testAddOrUpdateCachedInactiveUser() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();

        when(directory.getValue(SynchronisableDirectoryProperties.IS_SYNCHRONISING)).thenReturn(Boolean.FALSE.toString());

        // Construct the class under test
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        // Add active user to remote directory
        UserTemplateWithAttributes user = createUser("fred", "Fred Normal");
        user.setActive(true);
        remoteDirectory.addUser(user, null);

        // Add inactive user to internal directory
        user.setActive(false);
        internalDirectory.addUser(user, null);

        // Update active user to DbCachingRemoteDirectory
        user.setActive(true);
        user.setDisplayName("Fred Super");

        DirectoryCache dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);
        dbCachingRemoteDirectoryCache.addOrUpdateCachedUsers(Collections.singletonList(user), null);

        assertFalse("User is not inactive", dbCachingRemoteDirectory.findUserByName("fred").isActive());

        verify(eventPublisher).publish(isA(UserUpdatedEvent.class));
    }

    public void testAddOrUpdateCachedUserAccountDisabledRemotely() throws Exception
    {
        // Create mocks for the underlying directories
        final MockRemoteDirectory remoteDirectory = new MockRemoteDirectory();
        remoteDirectory.setSupportsInactiveAccounts(true);
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();

        when(directory.getValue(SynchronisableDirectoryProperties.IS_SYNCHRONISING)).thenReturn(Boolean.FALSE.toString());

        // Construct the class under test
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);

        UserTemplateWithAttributes user = createUser("fred", "Fred Normal");

        // Add active user to internal directory
        user.setActive(true);
        internalDirectory.addUser(user, null);

        // Add inactive user to remote directory
        user.setActive(false);
        remoteDirectory.addUser(user, null);

        // Update inactive user to DbCachingRemoteDirectory
        DirectoryCache dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);
        dbCachingRemoteDirectoryCache.addOrUpdateCachedUsers(Collections.singletonList(user), null);

        assertFalse("User is not inactive", dbCachingRemoteDirectory.findUserByName("fred").isActive());

        verify(eventPublisher).publish(isA(UserUpdatedEvent.class));
    }

    public void testDeleteCachedUsersNotInDeletes() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();

        when(directory.getValue(SynchronisableDirectoryProperties.IS_SYNCHRONISING)).thenReturn(Boolean.FALSE.toString());

        // Construct the class under test
        DirectoryCache dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);
        // Do a user create
        dbCachingRemoteDirectoryCache.addOrUpdateCachedUsers(Arrays.asList(createUser("fred", "Fred Normal")), null);
        assertEquals("Fred Normal", internalDirectory.findUserByName("fred").getDisplayName());
        verify(eventPublisher).publish(isA(UserCreatedEvent.class));
        // Do a user delete
        dbCachingRemoteDirectoryCache.deleteCachedUsersNotIn(Collections.<User>emptyList(), null);
        try
        {
            internalDirectory.findUserByName("fred");
            fail("fred was not deleted");
        } catch (UserNotFoundException e)
        {
            // Success
        }
        verify(eventPublisher).publish(isA(UserDeletedEvent.class));
    }

    public void testDeleteCachedUsersNotInRetainsWhenUsernameCaseHasChanged() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();

        when(directory.getValue(SynchronisableDirectoryProperties.IS_SYNCHRONISING)).thenReturn(Boolean.FALSE.toString());

        // Construct the class under test
        DirectoryCache dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);
        // Do a user create
        dbCachingRemoteDirectoryCache.addOrUpdateCachedUsers(Arrays.asList(createUser("fred", "Fred Normal")), null);
        internalDirectory.findUserByName("fred").getDisplayName();
        verify(eventPublisher).publish(isA(UserCreatedEvent.class));
        // User should be retained even with different case username
        dbCachingRemoteDirectoryCache.deleteCachedUsersNotIn(Arrays.asList(createUser("Fred", "Fred Normal")), null);
        internalDirectory.findUserByName("fred").getDisplayName();
        verify(eventPublisher, times(0)).publish(isA(UserDeletedEvent.class));
    }

    public void testAddOrUpdateCachedGroup() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();

        DirectoryCache dbCachingRemoteDirectoryCache;

        // add new Group
        dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);
        dbCachingRemoteDirectoryCache.addOrUpdateCachedGroups(Arrays.asList(createGroup("cats", "meiow", GroupType.GROUP)), null);
        assertEquals("meiow", internalDirectory.getGroup("cats").getDescription());

        // First sync has been performed. Events should be published from consequent syncs.
        when(directory.getValue(SynchronisableDirectoryProperties.IS_SYNCHRONISING)).thenReturn(Boolean.FALSE.toString());

        // Update Group
        dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);
        dbCachingRemoteDirectoryCache.addOrUpdateCachedGroups(Arrays.asList(createGroup("cats", "scratch", GroupType.GROUP)), null);
        assertEquals("scratch", internalDirectory.getGroup("cats").getDescription());

        // Now add a role
        dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);
        dbCachingRemoteDirectoryCache.addOrUpdateCachedGroups(Arrays.asList(createGroup("dogs", "woof", GroupType.LEGACY_ROLE)), null);
        assertEquals("woof", internalDirectory.getRole("dogs").getDescription());


        // Add a role with same name as group
        dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);
        dbCachingRemoteDirectoryCache.addOrUpdateCachedGroups(Arrays.asList(createGroup("cats", "doh", GroupType.LEGACY_ROLE)), null);
        assertEquals("scratch", internalDirectory.getGroup("cats").getDescription());
        assertEquals(null, internalDirectory.getRole("cats"));

        // Add a group with same name as role
        dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);
        dbCachingRemoteDirectoryCache.addOrUpdateCachedGroups(Arrays.asList(createGroup("dogs", "catch", GroupType.GROUP)), null);
        assertEquals("catch", internalDirectory.getGroup("dogs").getDescription());
        assertEquals(null, internalDirectory.getRole("dogs"));

        verify(eventPublisher).publish(isA(GroupDeletedEvent.class));
        verify(eventPublisher, times(2)).publish(isA(GroupCreatedEvent.class));
        verify(eventPublisher).publish(isA(GroupUpdatedEvent.class));
    }

    /**
     * Tests that a remote group with a long description is added with truncated description.
     */
    public void testAddOrUpdateCachedGroupWithLongDescription() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final InternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();

        when(directory.getValue(SynchronisableDirectoryProperties.IS_SYNCHRONISING)).thenReturn(Boolean.FALSE.toString());

        // Construct the class under test
        DirectoryCache dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);

        final Group remoteGroup = createGroup("group", LONG_STRING, GroupType.GROUP);

        dbCachingRemoteDirectoryCache.addOrUpdateCachedGroups(Arrays.asList(remoteGroup), null);

        assertEquals(TRUNCATED_STRING, internalDirectory.findGroupByName("group").getDescription());

        verify(eventPublisher).publish(isA(GroupCreatedEvent.class));
    }

    public void testAddOrUpdateCachedGroupWhenGroupNameCaseHasChanged() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final InternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();

        when(directory.getValue(SynchronisableDirectoryProperties.IS_SYNCHRONISING)).thenReturn(Boolean.FALSE.toString());

        // Construct the class under test
        DirectoryCache dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);

        // Add group
        Group remoteGroup = createGroup("group", "Normal Group", GroupType.GROUP);
        dbCachingRemoteDirectoryCache.addOrUpdateCachedGroups(Arrays.asList(remoteGroup), null);
        assertEquals("Normal Group", internalDirectory.findGroupByName("group").getDescription());
        verify(eventPublisher).publish(isA(GroupCreatedEvent.class));

        // Update group when group name casing has changed
        remoteGroup = createGroup("Group", "Uppercase Group", GroupType.GROUP);
        dbCachingRemoteDirectoryCache.addOrUpdateCachedGroups(Arrays.asList(remoteGroup), null);
        assertEquals("Uppercase Group", internalDirectory.findGroupByName("group").getDescription());

        verify(eventPublisher).publish(isA(GroupUpdatedEvent.class));
    }

    /**
     * Tests that a remote group with a long description is not updated when
     * internal group with truncated description matches the remote group.
     */
    public void testAddOrUpdateCachedGroupWithNoChanges() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final InternalRemoteDirectory internalDirectory = mock(InternalRemoteDirectory.class);

        // Construct the class under test
        DirectoryCache dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);

        final Directory directory = new DirectoryImpl(new InternalEntityTemplate(1L, "", true, null, null));
        final InternalGroup internalGroup = new InternalGroup(createGroup("group", TRUNCATED_STRING, GroupType.GROUP), directory);

        final Group remoteGroup = createGroup("group", LONG_STRING, GroupType.GROUP);

        when(internalDirectory.searchGroups(any(EntityQuery.class))).thenReturn(ImmutableList.of(internalGroup));

        dbCachingRemoteDirectoryCache.addOrUpdateCachedGroups(Arrays.asList(remoteGroup), null);

        verify(internalDirectory, never()).addAllGroups(any(Set.class));

        verify(eventPublisher, never()).publish(any());
    }

    public void testDeleteCachedGroupsNotInDeletes() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();

        when(directory.getValue(SynchronisableDirectoryProperties.IS_SYNCHRONISING)).thenReturn(Boolean.FALSE.toString());

        // Construct the class under test
        DirectoryCache dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);

        // Create a group
        Group remoteGroup = createGroup("group", "Normal Group", GroupType.GROUP);
        dbCachingRemoteDirectoryCache.addOrUpdateCachedGroups(Arrays.asList(remoteGroup), null);
        assertEquals("Normal Group", internalDirectory.findGroupByName("group").getDescription());
        verify(eventPublisher).publish(isA(GroupCreatedEvent.class));
        // Do a group delete
        dbCachingRemoteDirectoryCache.deleteCachedGroupsNotIn(GroupType.GROUP, Collections.<Group>emptyList(), null);
        try
        {
            internalDirectory.findGroupByName("group");
            fail("fred was not deleted");
        } catch (GroupNotFoundException e)
        {
            // Success
        }
        verify(eventPublisher).publish(isA(GroupDeletedEvent.class));
    }

    public void testDeleteCachedGroupsNotInRetainsWhenUsernameCaseHasChanged() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();

        when(directory.getValue(SynchronisableDirectoryProperties.IS_SYNCHRONISING)).thenReturn(Boolean.FALSE.toString());

        // Construct the class under test
        DirectoryCache dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);

        // Create a group
        Group remoteGroup = createGroup("group", "Normal Group", GroupType.GROUP);
        dbCachingRemoteDirectoryCache.addOrUpdateCachedGroups(Arrays.asList(remoteGroup), null);
        internalDirectory.findGroupByName("group").getDescription();
        verify(eventPublisher).publish(isA(GroupCreatedEvent.class));
        // Group should be retained even with different case group name
        remoteGroup = createGroup("Group", "Uppercase Group", GroupType.GROUP);
        dbCachingRemoteDirectoryCache.deleteCachedGroupsNotIn(GroupType.GROUP, Arrays.asList(remoteGroup), null);
        internalDirectory.findGroupByName("group").getDescription();
        verify(eventPublisher, times(0)).publish(isA(GroupDeletedEvent.class));
    }

    public void testUpdateInactiveUser() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();
        // Construct the class under test
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);
        DirectoryCache dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);

        // Add active user to remote directory
        UserTemplateWithAttributes user = createUser("fred", "Fred Normal");
        user.setActive(true);
        remoteDirectory.addUser(user, null);
        // ... and sync
        dbCachingRemoteDirectoryCache.addOrUpdateCachedUsers(Collections.singletonList(user), null);

        // Mark user inactive
        user.setActive(false);
        dbCachingRemoteDirectory.updateUser(user);

        assertFalse("User is not inactive", dbCachingRemoteDirectory.findUserByName("fred").isActive());
    }

    public void testAuthenticateUser() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();
        final EventPublisher eventPublisher = new MockEventPublisher();
        // Construct the class under test
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);
        DirectoryCache dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);

        // Add active user to remote directory
        UserTemplateWithAttributes user = createUser("fred", "Fred Normal");
        user.setActive(true);
        remoteDirectory.addUser(user, null);

        dbCachingRemoteDirectoryCache.addOrUpdateCachedUsers(Collections.singletonList(user), null);

        UserWithAttributes userWithAttributesBefore = dbCachingRemoteDirectory.findUserWithAttributesByName("fred");
        long lastLoginBefore = NumberUtils.toLong(userWithAttributesBefore.getValue(com.atlassian.crowd.model.user.UserConstants.LAST_AUTHENTICATED), 0);

        dbCachingRemoteDirectory.authenticate("fred", new PasswordCredential(""));

        UserWithAttributes userWithAttributesAfter = dbCachingRemoteDirectory.findUserWithAttributesByName("fred");
        long lastLoginAfter = NumberUtils.toLong(userWithAttributesAfter.getValue(com.atlassian.crowd.model.user.UserConstants.LAST_AUTHENTICATED), 0);

        assertEquals("0", userWithAttributesAfter.getValue(com.atlassian.crowd.model.user.UserConstants.INVALID_PASSWORD_ATTEMPTS));
        assertTrue("Last login time not updated", lastLoginBefore < lastLoginAfter);
    }

    public void testAuthenticateUserFailure() throws Exception
    {
        // Create mocks for the underlying directories
        final RemoteDirectory remoteDirectory = new MockRemoteDirectory();
        final MockInternalRemoteDirectory internalDirectory = new MockInternalRemoteDirectory();
        final EventPublisher eventPublisher = new MockEventPublisher();
        // Construct the class under test
        DbCachingRemoteDirectory dbCachingRemoteDirectory = new DbCachingRemoteDirectory(remoteDirectory, internalDirectory, directoryCacheFactory);
        DirectoryCache dbCachingRemoteDirectoryCache = newDirectoryCache(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);

        // Add active user to remote directory
        UserTemplateWithAttributes user = createUser("fred", "Fred Normal");
        user.setActive(true);
        remoteDirectory.addUser(user, null);

        dbCachingRemoteDirectoryCache.addOrUpdateCachedUsers(Collections.singletonList(user), null);
        dbCachingRemoteDirectory.authenticate("fred", new PasswordCredential(""));

        UserWithAttributes userWithAttributesBefore = dbCachingRemoteDirectory.findUserWithAttributesByName("fred");
        long lastLoginBefore = NumberUtils.toLong(userWithAttributesBefore.getValue(com.atlassian.crowd.model.user.UserConstants.LAST_AUTHENTICATED), 0);

        try
        {
            dbCachingRemoteDirectory.authenticate("fred", null);
            fail("Authentication succeeded with inactive account.");
        } catch (InvalidAuthenticationException e)
        {
            // Success
        }

        UserWithAttributes userWithAttributesAfter = dbCachingRemoteDirectory.findUserWithAttributesByName("fred");
        long lastLoginAfter = NumberUtils.toLong(userWithAttributesAfter.getValue(com.atlassian.crowd.model.user.UserConstants.LAST_AUTHENTICATED), 0);

        assertEquals("1", userWithAttributesAfter.getValue(com.atlassian.crowd.model.user.UserConstants.INVALID_PASSWORD_ATTEMPTS));
        assertEquals("Last login time was updated.", lastLoginBefore, lastLoginAfter);
    }

    private static Group createGroup(final String name, final String description, final GroupType type)
    {
        GroupTemplate groupTemplate = new GroupTemplate(name, 1, type);
        groupTemplate.setDescription(description);
        return groupTemplate;
    }

    private static UserTemplateWithAttributes createUser(final String username, String displayName)
    {
        final UserTemplateWithAttributes user = new UserTemplateWithAttributes(username, 1);
        user.setDisplayName(displayName);
        return user;
    }
}
