package com.atlassian.crowd.manager.directory;

import com.atlassian.crowd.dao.application.ApplicationDAO;
import com.atlassian.crowd.directory.InternalRemoteDirectory;
import com.atlassian.crowd.directory.MockInternalRemoteDirectory;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.event.directory.DirectoryDeletedEvent;
import com.atlassian.crowd.event.group.GroupCreatedEvent;
import com.atlassian.crowd.event.group.GroupMembershipCreatedEvent;
import com.atlassian.crowd.event.user.UserCreatedEvent;
import com.atlassian.crowd.event.user.UserCredentialUpdatedEvent;
import com.atlassian.crowd.exception.DirectoryCurrentlySynchronisingException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.directory.monitor.poller.DirectoryPollerManager;
import com.atlassian.crowd.manager.lock.DirectoryLockManager;
import com.atlassian.crowd.manager.permission.PermissionManager;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.util.PasswordHelper;
import com.atlassian.crowd.util.PasswordHelperImpl;
import com.atlassian.event.api.EventPublisher;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.concurrent.locks.Lock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


/**
 * DirectoryManagerGeneric Tester.
 */
public class DirectoryManagerGenericTest
{
    private DirectoryManagerGeneric directoryManager = null;

    @Mock
    private RemoteDirectory remoteDirectory;
    @Mock
    private DirectoryDao directoryDao;
    @Mock
    private ApplicationDAO applicationDAO;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private PermissionManager permissionManager;
    @Mock
    private DirectorySynchroniser directorySynchroniser;
    @Mock
    private DirectoryPollerManager directoryPollerManager;
    @Mock
    private DirectoryLockManager directoryLockManager;
    @Mock
    private DirectoryInstanceLoader directoryInstanceLoader;
    @Mock
    private Directory directory;
    @Mock
    private Lock lock;
    @Mock
    private SynchronisationStatusManager synchronisationStatusManager;

    
    private PasswordHelper passwordHelper;

    private static final long DIRECTORY_ID = 1L;

    private static final String USERNAME1 = "user1";
    private static final String USERNAME2 = "user2";
    private static final String USERNAME3 = "user3";

    private static final User USER1 = new UserTemplate(USERNAME1, DIRECTORY_ID);
    private static final User USER2 = new UserTemplate(USERNAME2, DIRECTORY_ID);
    private static final User USER3 = new UserTemplate(USERNAME3, DIRECTORY_ID);

    private static final String GROUP_NAME1 = "group1";
    private static final String GROUP_NAME2 = "group2";
    private static final String GROUP_NAME3 = "group3";

    private static final GroupTemplate GROUP1 = new GroupTemplate(GROUP_NAME1, DIRECTORY_ID);
    private static final GroupTemplate GROUP2 = new GroupTemplate(GROUP_NAME2, DIRECTORY_ID);
    private static final GroupTemplate GROUP3 = new GroupTemplate(GROUP_NAME3, DIRECTORY_ID);

    @Before
    public void setUp() throws Exception
    {
        initMocks(this);

        passwordHelper = new PasswordHelperImpl();
        directoryManager = new DirectoryManagerGeneric(
                directoryDao,
                applicationDAO,
                eventPublisher,
                permissionManager,
                passwordHelper,
                directoryInstanceLoader,
                directorySynchroniser,
                directoryPollerManager,
                directoryLockManager,
                synchronisationStatusManager);

        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);

        when(directory.getName()).thenReturn("testing-directory");
        when(directory.getId()).thenReturn(DIRECTORY_ID);
        when(directory.getType()).thenReturn(DirectoryType.INTERNAL);
        
        when(directoryInstanceLoader.getDirectory(directory)).thenReturn(remoteDirectory);

        when(directorySynchroniser.isSynchronising(anyLong())).thenReturn(false);
        when(directoryLockManager.getLock(anyLong())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
    }

    @After
    public void tearDown()
    {
        passwordHelper = null;
        directoryManager = null;
        remoteDirectory = null;
        directoryDao = null;
        applicationDAO = null;
        eventPublisher = null;
        permissionManager = null;
        directorySynchroniser = null;
        directoryPollerManager = null;
        directoryInstanceLoader = null;
        directory = null;
    }

    @Test
    public void testAddAllGroups_EventsPublished() throws Exception
    {
        final InternalRemoteDirectory internalRemoteDirectory = new MockInternalRemoteDirectory();
        when(directoryInstanceLoader.getDirectory(directory)).thenReturn(internalRemoteDirectory);
        when(permissionManager.hasPermission(directory, OperationType.CREATE_GROUP)).thenReturn(true);

        directoryManager.addAllGroups(DIRECTORY_ID, ImmutableList.of(GROUP1, GROUP2, GROUP3), false);

        verify(eventPublisher, times(3)).publish(isA(GroupCreatedEvent.class));
    }

    @Test
    public void testAddAllUsers_EventsPublished() throws Exception
    {
        final UserTemplateWithCredentialAndAttributes USER_COMPLETE1 = new UserTemplateWithCredentialAndAttributes(USER1, PasswordCredential.NONE);
        final UserTemplateWithCredentialAndAttributes USER_COMPLETE2 = new UserTemplateWithCredentialAndAttributes(USER2, PasswordCredential.NONE);

        final InternalRemoteDirectory internalRemoteDirectory = new MockInternalRemoteDirectory();
        when(directoryInstanceLoader.getDirectory(directory)).thenReturn(internalRemoteDirectory);
        when(permissionManager.hasPermission(directory, OperationType.CREATE_USER)).thenReturn(true);

        directoryManager.addAllUsers(DIRECTORY_ID, ImmutableList.of(USER_COMPLETE1, USER_COMPLETE2), false);

        verify(eventPublisher, times(2)).publish(isA(UserCreatedEvent.class));
    }

    @Test
    public void testAddAllUsersToGroup_EventsPublished() throws Exception
    {
        final InternalRemoteDirectory internalRemoteDirectory = new MockInternalRemoteDirectory()
        {
            @Override
            public void addUserToGroup(String username, String groupName) throws GroupNotFoundException, UserNotFoundException, OperationFailedException
            {
                return;
            }
        };
        internalRemoteDirectory.addGroup(GROUP1);
        when(directoryInstanceLoader.getDirectory(directory)).thenReturn(internalRemoteDirectory);
        when(permissionManager.hasPermission(directory, OperationType.UPDATE_GROUP)).thenReturn(true);

        directoryManager.addAllUsersToGroup(DIRECTORY_ID, ImmutableList.of(USERNAME1, USERNAME2), GROUP_NAME1);

        verify(eventPublisher, times(2)).publish(isA(GroupMembershipCreatedEvent.class));
    }

    @Test
    public void testRemoveInternalDirectory() throws Exception
    {
        // now make the call
        directoryManager.removeDirectory(directory);

        // Remove directory from applications
        verify(applicationDAO).removeDirectoryMappings(DIRECTORY_ID);

        // Delete the directory
        verify(directoryDao).remove(directory);

        verify(eventPublisher).publish(any(DirectoryDeletedEvent.class));
    }

    @Test
    public void testRemoveDelegatedDirectory() throws Exception
    {
        // Make the directory type delegated
        when(directory.getType()).thenReturn(DirectoryType.DELEGATING);

        directoryManager.removeDirectory(directory);

        // Remove directory from applications
        verify(applicationDAO).removeDirectoryMappings(DIRECTORY_ID);

        // Delete the directory
        verify(directoryDao).remove(directory);

        verify(eventPublisher).publish(any(DirectoryDeletedEvent.class));
    }

    @Test
    public void testRemoveConnectorDirectory() throws Exception
    {
        when(directory.getType()).thenReturn(DirectoryType.CONNECTOR);

        directoryManager.removeDirectory(directory);

        // Remove directory from applications
        verify(applicationDAO).removeDirectoryMappings(DIRECTORY_ID);

        // Delete the directory
        verify(directoryDao).remove(directory);

        verify(eventPublisher).publish(any(DirectoryDeletedEvent.class));
    }

    /**
     * Tests that removing a directory while a synchronising is occurring will throw a
     * {@link com.atlassian.crowd.exception.DirectoryCurrentlySynchronisingException}.
     */
    @Test(expected = DirectoryCurrentlySynchronisingException.class)
    public void testRemoveConnectorDirectory_DirectoryIsSynchronising() throws Exception
    {
        when(lock.tryLock()).thenReturn(false);
        when(directory.getType()).thenReturn(DirectoryType.CONNECTOR);

        directoryManager.removeDirectory(directory);

        // Remove directory from applications
        verify(applicationDAO, never()).removeDirectoryMappings(DIRECTORY_ID);

        // Delete the directory
        verify(directoryDao, never()).remove(directory);

        verify(eventPublisher, never()).publish(any(DirectoryDeletedEvent.class));
    }

    /**
     * Test that nested search for user members returns requested amount of results in the order they are found.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSearchNestedGroupRelationships_UserChildrenMaxResults() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(remoteDirectory.supportsNestedGroups()).thenReturn(true);
        when(remoteDirectory.findGroupByName(GROUP_NAME3)).thenReturn(GROUP3);
        when(remoteDirectory.searchGroupRelationships(createUserChildrenQuery(GROUP_NAME3, 1))).thenReturn(ImmutableList.of(USER3));
        when(remoteDirectory.searchGroupRelationships(createGroupMembershipQuery(GROUP_NAME3, EntityQuery.ALL_RESULTS))).thenReturn(ImmutableList.<Group>of(GROUP2));
        when(remoteDirectory.searchGroupRelationships(createUserChildrenQuery(GROUP_NAME2, 1))).thenReturn(ImmutableList.of(USER2));
        when(remoteDirectory.searchGroupRelationships(createGroupMembershipQuery(GROUP_NAME2, EntityQuery.ALL_RESULTS))).thenReturn(ImmutableList.<Group>of(GROUP1));
        when(remoteDirectory.searchGroupRelationships(createUserChildrenQuery(GROUP_NAME1, 1))).thenReturn(ImmutableList.of(USER1));
        when(remoteDirectory.searchGroupRelationships(createGroupMembershipQuery(GROUP_NAME1, EntityQuery.ALL_RESULTS))).thenReturn(ImmutableList.<Group>of());

        final MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(GROUP_NAME3).returningAtMost(1);

        final List<String> results = directoryManager.searchNestedGroupRelationships(DIRECTORY_ID, query);

        assertEquals(1, results.size());
        assertEquals(USERNAME3, results.get(0));
    }

    /**
     * Test that nested search for group members returns requested amount of results in the order they are found.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSearchNestedGroupRelationships_GroupChildrenMaxResults() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(remoteDirectory.supportsNestedGroups()).thenReturn(true);
        when(remoteDirectory.findGroupByName(GROUP_NAME3)).thenReturn(GROUP3);
        when(remoteDirectory.searchGroupRelationships(createGroupMembershipQuery(GROUP_NAME3, 1))).thenReturn(ImmutableList.<Group>of(GROUP2));
        when(remoteDirectory.searchGroupRelationships(createGroupMembershipQuery(GROUP_NAME2, 1))).thenReturn(ImmutableList.<Group>of(GROUP1));
        when(remoteDirectory.searchGroupRelationships(createGroupMembershipQuery(GROUP_NAME1, 1))).thenReturn(ImmutableList.<Group>of());

        final MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).childrenOf(EntityDescriptor.group()).withName(GROUP_NAME3).returningAtMost(1);

        final List<String> results = directoryManager.searchNestedGroupRelationships(DIRECTORY_ID, query);

        assertEquals(1, results.size());
        assertEquals(GROUP_NAME2, results.get(0));
    }

    /**
     * Test that nested search for user's group memberships returns requested amount of results in the order they are found.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSearchNestedGroupRelationships_UserParentsMaxResults() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(remoteDirectory.supportsNestedGroups()).thenReturn(true);
        when(remoteDirectory.findUserByName(USERNAME3)).thenReturn(USER3);
        when(remoteDirectory.searchGroupRelationships(createUserParentsQuery(USERNAME3, 1))).thenReturn(ImmutableList.<Group>of(GROUP3));
        when(remoteDirectory.searchGroupRelationships(createGroupParentsQuery(GROUP_NAME3, 1))).thenReturn(ImmutableList.<Group>of(GROUP2));
        when(remoteDirectory.searchGroupRelationships(createGroupParentsQuery(GROUP_NAME2, 1))).thenReturn(ImmutableList.<Group>of(GROUP1));
        when(remoteDirectory.searchGroupRelationships(createGroupParentsQuery(GROUP_NAME1, 1))).thenReturn(ImmutableList.<Group>of());

        final MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(USERNAME3).returningAtMost(1);

        final List<String> results = directoryManager.searchNestedGroupRelationships(DIRECTORY_ID, query);

        assertEquals(1, results.size());
        assertEquals(GROUP_NAME3, results.get(0));
    }

    /**
     * Test that nested search for group's group memberships returns requested amount of results in the order they are found.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSearchNestedGroupRelationships_GroupParentsMaxResults() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(remoteDirectory.supportsNestedGroups()).thenReturn(true);
        when(remoteDirectory.findGroupByName(GROUP_NAME3)).thenReturn(GROUP3);
        when(remoteDirectory.searchGroupRelationships(createGroupParentsQuery(GROUP_NAME3, 1))).thenReturn(ImmutableList.<Group>of(GROUP2));
        when(remoteDirectory.searchGroupRelationships(createGroupParentsQuery(GROUP_NAME2, 1))).thenReturn(ImmutableList.<Group>of(GROUP1));
        when(remoteDirectory.searchGroupRelationships(createGroupParentsQuery(GROUP_NAME1, 1))).thenReturn(ImmutableList.<Group>of());

        final MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.group()).withName(GROUP_NAME3).returningAtMost(1);

        final List<String> results = directoryManager.searchNestedGroupRelationships(DIRECTORY_ID, query);

        assertEquals(1, results.size());
        assertEquals(GROUP_NAME2, results.get(0));
    }

    @Test
    public void testUpdatePrincipalCredentialFiresEvent() throws Exception
    {
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(permissionManager.hasPermission(eq(directory), any(OperationType.class))).thenReturn(true);

        directoryManager.updateUserCredential(DIRECTORY_ID, "beep", new PasswordCredential("newPassword"));

        verify(directoryDao, times(2)).findById(DIRECTORY_ID);
        verify(permissionManager).hasPermission(eq(directory), any(OperationType.class));
        verify(remoteDirectory).updateUserCredential(anyString(), any(PasswordCredential.class));

        // this is the point of the test - must be called.
        verify(eventPublisher).publish(any(UserCredentialUpdatedEvent.class));
    }

    private MembershipQuery<User> createUserChildrenQuery(String name, int maxResults)
    {
        return QueryBuilder.queryFor(User.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(name).returningAtMost(maxResults);
    }

    private MembershipQuery<Group> createGroupMembershipQuery(String name, int maxResults)
    {
        return QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).childrenOf(EntityDescriptor.group()).withName(name).returningAtMost(maxResults);
    }

    private MembershipQuery<Group> createUserParentsQuery(String name, int maxResults)
    {
        return QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(name).returningAtMost(maxResults);
    }

    private MembershipQuery<Group> createGroupParentsQuery(String name, int maxResults)
    {
        return QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.group()).withName(name).returningAtMost(maxResults);
    }
}
