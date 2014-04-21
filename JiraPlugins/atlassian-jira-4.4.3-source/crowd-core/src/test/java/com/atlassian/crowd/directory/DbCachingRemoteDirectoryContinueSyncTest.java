package com.atlassian.crowd.directory;

import com.atlassian.crowd.directory.ldap.cache.DirectoryCache;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.event.group.GroupMembershipCreatedEvent;
import com.atlassian.crowd.event.group.GroupMembershipDeletedEvent;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidMembershipException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.directory.SynchronisationStatusManager;
import com.atlassian.crowd.model.InternalEntityTemplate;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.InternalDirectoryGroup;
import com.atlassian.crowd.model.group.InternalGroup;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.util.BatchResult;
import com.atlassian.event.api.EventPublisher;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that membership sync continues even if users/groups are missing
 *
 * https://studio.atlassian.com/browse/EMBCWD-520
 */
public class DbCachingRemoteDirectoryContinueSyncTest extends TestCase
{
    private DirectoryDao directoryDao;
    private Directory directory;
    private RemoteDirectory remoteDirectory;
    private InternalRemoteDirectory internalDirectory;
    private SynchronisationStatusManager syncStatusManager;
    private EventPublisher eventPublisher;

    private DirectoryCache dbCachingRemoteDirectoryCache;

    public void setUp() throws Exception
    {
        super.setUp();

        directoryDao = mock(DirectoryDao.class);
        directory = mock(Directory.class);
        remoteDirectory = mock(RemoteDirectory.class);
        internalDirectory = mock(InternalRemoteDirectory.class);
        syncStatusManager = mock(SynchronisationStatusManager.class);
        eventPublisher = mock(EventPublisher.class);

        when(directoryDao.findById(anyLong())).thenReturn(directory);
        when(directory.getValue(SynchronisableDirectoryProperties.IS_SYNCHRONISING)).thenReturn(Boolean.FALSE.toString());

        // Construct the class under test
        DbCachingRemoteChangeOperations ops = new DbCachingRemoteChangeOperations(directoryDao, remoteDirectory, internalDirectory, syncStatusManager, eventPublisher);
        dbCachingRemoteDirectoryCache = new DirectoryCacheImplUsingChangeOperations(ops);
    }

    // ------------------------------------------------
    //  syncUserMembershipsForGroup
    // ------------------------------------------------

    public void testAddUserMissingUser() throws Exception
    {
        // Create test objects
        InternalDirectoryGroup testGroup = createInternalDirectoryGroup("Test Group", "Test Directory", GroupType.GROUP);

        // Stub the mocks
        when(internalDirectory.findGroupByName(testGroup.getName())).thenReturn(testGroup);
        when(internalDirectory.searchGroupRelationships(any(MembershipQuery.class))).thenReturn(Collections.emptyList());
        when(internalDirectory.addAllUsersToGroup(anySet(), eq(testGroup.getName()))).thenReturn(createBatchResult(Arrays.asList("user1", "user2", "user3"), Collections.singletonList("dud-user")));

        // Test the sync
        dbCachingRemoteDirectoryCache.syncUserMembersForGroup(testGroup, Arrays.asList("user1", "user2", "dud-user", "user3"));

        // Assert add/remove calls are made as expected despite the exception thrown
        verify(internalDirectory, times(1)).addAllUsersToGroup(new HashSet<String>(Arrays.asList("user1", "user2", "dud-user", "user3")), testGroup.getName());
        verify(eventPublisher, times(3)).publish(isA(GroupMembershipCreatedEvent.class)); // dud-user already exists
        verify(internalDirectory, never()).removeUserFromGroup(anyString(), eq(testGroup.getName())); // not called because no users to remove
    }

    public void testRemoveUserExceptionMissingUser() throws Exception
    {
        // Create test objects
        InternalDirectoryGroup testGroup = createInternalDirectoryGroup("Test Group", "Test Directory", GroupType.GROUP);

        // Stub the mocks
        when(internalDirectory.findGroupByName(testGroup.getName())).thenReturn(testGroup);
        when(internalDirectory.searchGroupRelationships(any(MembershipQuery.class))).thenReturn(Arrays.asList("user1", "dud-user", "user2", "user3", "user4"));
        doThrow(new UserNotFoundException("dud-user")).when(internalDirectory).removeUserFromGroup(anyString(), eq(testGroup.getName()));

        // Test the sync
        dbCachingRemoteDirectoryCache.syncUserMembersForGroup(testGroup, Arrays.asList("user1", "user3"));

        // Assert add/remove calls are made as expected despite the exception thrown
        verify(internalDirectory, never()).addAllUsersToGroup(anySet(), eq(testGroup.getName())); // no new users to add
        verify(internalDirectory, times(3)).removeUserFromGroup(anyString(), eq(testGroup.getName()));
        verify(eventPublisher, never()).publish(any()); // removals throw exception
    }

    public void testAddRemoveUserException() throws Exception
    {
        // Create test objects
        InternalDirectoryGroup testGroup = createInternalDirectoryGroup("Test Group", "Test Directory", GroupType.GROUP);

        // Stub the mocks
        when(internalDirectory.findGroupByName(testGroup.getName())).thenReturn(testGroup);
        when(internalDirectory.searchGroupRelationships(any(MembershipQuery.class))).thenReturn(Arrays.asList("some-user"));
        when(internalDirectory.addAllUsersToGroup(anySet(), eq(testGroup.getName()))).thenReturn(createBatchResult(Arrays.asList("user1", "user2", "user3"), Collections.singletonList("dud-user")));

        // Test the sync
        dbCachingRemoteDirectoryCache.syncUserMembersForGroup(testGroup, Arrays.asList("user1", "dud-user", "user2", "user3"));

        // Assert add/remove calls are made as expected despite the exception thrown
        verify(internalDirectory, times(1)).addAllUsersToGroup(new HashSet<String>(Arrays.asList("user1", "dud-user", "user2", "user3")), testGroup.getName());
        verify(eventPublisher, times(3)).publish(isA(GroupMembershipCreatedEvent.class));
        verify(internalDirectory, times(1)).removeUserFromGroup("some-user", testGroup.getName());
        verify(eventPublisher, times(1)).publish(isA(GroupMembershipDeletedEvent.class));
    }

    public void testAddRemoveGroupException() throws Exception
    {
        // Create test objects
        InternalDirectoryGroup testGroup = createInternalDirectoryGroup("Test Group", "Test Directory", GroupType.GROUP);

        // Stub the mocks
        when(internalDirectory.findGroupByName(testGroup.getName())).thenReturn(testGroup);
        when(internalDirectory.searchGroupRelationships(any(MembershipQuery.class))).thenReturn(Arrays.asList("some-user"));
        when(internalDirectory.addAllUsersToGroup(anySet(), eq(testGroup.getName()))).thenThrow(new GroupNotFoundException(testGroup.getName()));

        // Test the sync
        dbCachingRemoteDirectoryCache.syncUserMembersForGroup(testGroup, Arrays.asList("user1", "dud-user", "user2", "user3"));

        // Assert add/remove calls are made as expected despite the exception thrown
        verify(internalDirectory, times(1)).addAllUsersToGroup(new HashSet<String>(Arrays.asList("user1", "dud-user", "user2", "user3")), testGroup.getName());
        verify(internalDirectory, times(1)).removeUserFromGroup("some-user", testGroup.getName());
        verify(eventPublisher, times(1)).publish(isA(GroupMembershipDeletedEvent.class));
    }

    // ------------------------------------------------
    //  syncGroupMembershipsForGroup
    // ------------------------------------------------

    public void testAddGroupExceptionMissingGroup() throws Exception
    {
        // Create test objects
        InternalDirectoryGroup testGroup = createInternalDirectoryGroup("Test Group", "Test Directory", GroupType.GROUP);

        // Stub the mocks
        when(internalDirectory.findGroupByName(testGroup.getName())).thenReturn(testGroup);
        when(internalDirectory.searchGroupRelationships(any(MembershipQuery.class))).thenReturn(Collections.emptyList()); // no existing groups
        doThrow(new GroupNotFoundException("dud-group")).when(internalDirectory).addGroupToGroup("dud-group", testGroup.getName());

        // Test the sync
        dbCachingRemoteDirectoryCache.syncGroupMembersForGroup(testGroup, Arrays.asList("group1", "dud-group", "group2", "group3", "group4"));

        // Assert add/remove calls are made as expected despite the exception thrown
        verify(internalDirectory, times(5)).addGroupToGroup(anyString(), eq(testGroup.getName()));
        verify(eventPublisher, times(4)).publish(isA(GroupMembershipCreatedEvent.class)); // dud-group was not added
        verify(internalDirectory, never()).removeGroupFromGroup(anyString(), eq(testGroup.getName()));
    }

    public void testRemoveGroupExceptionMissingGroup() throws Exception
    {
        // Create test objects
        InternalDirectoryGroup testGroup = createInternalDirectoryGroup("Test Group", "Test Directory", GroupType.GROUP);

        // Stub the mocks
        when(internalDirectory.findGroupByName(testGroup.getName())).thenReturn(testGroup);
        when(internalDirectory.searchGroupRelationships(any(MembershipQuery.class))).thenReturn(Arrays.asList("group1", "dud-group", "group2", "group3", "group4"));
        doThrow(new GroupNotFoundException("dud-group")).when(internalDirectory).removeGroupFromGroup("dud-group", testGroup.getName());

        // Test the sync
        dbCachingRemoteDirectoryCache.syncGroupMembersForGroup(testGroup, Arrays.asList("group1", "group2"));

        // Assert add/remove calls are made as expected despite the exception thrown
        verify(internalDirectory, never()).addGroupToGroup(anyString(), eq(testGroup.getName()));
        verify(internalDirectory, times(3)).removeGroupFromGroup(anyString(), eq(testGroup.getName()));
        verify(eventPublisher, times(2)).publish(isA(GroupMembershipDeletedEvent.class));
    }

    public void testAddGroupExceptionInvalidMembership() throws Exception
    {
        // Create test objects
        InternalDirectoryGroup testGroup = createInternalDirectoryGroup("Test Group", "Test Directory", GroupType.GROUP);

        // Stub the mocks
        when(internalDirectory.findGroupByName(testGroup.getName())).thenReturn(testGroup);
        when(internalDirectory.searchGroupRelationships(any(MembershipQuery.class))).thenReturn(Collections.emptyList()); // no existing groups
        doThrow(new InvalidMembershipException("")).when(internalDirectory).addGroupToGroup("dud-group", testGroup.getName());

        // Test the sync
        dbCachingRemoteDirectoryCache.syncGroupMembersForGroup(testGroup, Arrays.asList("group1", "dud-group", "group2", "group3", "group4"));

        // Assert add/remove calls are made as expected despite the exception thrown
        verify(internalDirectory, times(5)).addGroupToGroup(anyString(), eq(testGroup.getName()));
        verify(eventPublisher, times(4)).publish(isA(GroupMembershipCreatedEvent.class)); // dud-group was not added
        verify(internalDirectory, never()).removeGroupFromGroup(anyString(), eq(testGroup.getName()));
    }

    public void testRemoveGroupExceptionInvalidMembership() throws Exception
    {
        // Create test objects
        InternalDirectoryGroup testGroup = createInternalDirectoryGroup("Test Group", "Test Directory", GroupType.GROUP);

        // Stub the mocks
        when(internalDirectory.findGroupByName(testGroup.getName())).thenReturn(testGroup);
        when(internalDirectory.searchGroupRelationships(any(MembershipQuery.class))).thenReturn(Arrays.asList("group1", "dud-group", "group2", "group3", "group4"));
        doThrow(new InvalidMembershipException("")).when(internalDirectory).removeGroupFromGroup("dud-group", testGroup.getName());

        // Test the sync
        dbCachingRemoteDirectoryCache.syncGroupMembersForGroup(testGroup, Arrays.asList("group1", "group2"));

        // Assert add/remove calls are made as expected despite the exception thrown
        verify(internalDirectory, never()).addGroupToGroup(anyString(), eq(testGroup.getName()));
        verify(internalDirectory, times(3)).removeGroupFromGroup(anyString(), eq(testGroup.getName()));
        verify(eventPublisher, times(2)).publish(isA(GroupMembershipDeletedEvent.class));
    }

    public void testAddRemoveGroupExceptionMissingGroup() throws Exception
    {
        // Create test objects
        InternalDirectoryGroup testGroup = createInternalDirectoryGroup("Test Group", "Test Directory", GroupType.GROUP);

        // Stub the mocks
        when(internalDirectory.findGroupByName(testGroup.getName())).thenReturn(testGroup);
        when(internalDirectory.searchGroupRelationships(any(MembershipQuery.class))).thenReturn(Arrays.asList("group1", "old-group", "older-group", "group2"));
        doThrow(new GroupNotFoundException("dud-group")).when(internalDirectory).addGroupToGroup("dud-group", testGroup.getName());
        doThrow(new GroupNotFoundException("old-group")).when(internalDirectory).removeGroupFromGroup("old-group", testGroup.getName());

        // Test the sync
        dbCachingRemoteDirectoryCache.syncGroupMembersForGroup(testGroup, Arrays.asList("group1", "dud-group", "group2", "group3", "group4"));

        // Assert add/remove calls are made as expected despite the exception thrown
        verify(internalDirectory, times(3)).addGroupToGroup(anyString(), eq(testGroup.getName()));
        verify(internalDirectory, times(2)).removeGroupFromGroup(anyString(), eq(testGroup.getName()));
        verify(eventPublisher, times(2)).publish(isA(GroupMembershipCreatedEvent.class));
        verify(eventPublisher, times(1)).publish(isA(GroupMembershipDeletedEvent.class));
    }

    private InternalDirectoryGroup createInternalDirectoryGroup(final String groupName, final String directoryName, final GroupType type)
    {
        GroupTemplate groupTemplate = new GroupTemplate(groupName, 1L, type);
        InternalEntityTemplate directoryTemplate = new InternalEntityTemplate(1L, directoryName, true, null, null);
        return new InternalGroup(groupTemplate, new DirectoryImpl(directoryTemplate));
    }

    private <T> BatchResult<T> createBatchResult(Collection<T> successes, Collection<T> failures)
    {
        final BatchResult batchResult = new BatchResult(successes.size());
        batchResult.addSuccesses(successes);
        batchResult.addFailures(failures);
        return batchResult;
    }
}
