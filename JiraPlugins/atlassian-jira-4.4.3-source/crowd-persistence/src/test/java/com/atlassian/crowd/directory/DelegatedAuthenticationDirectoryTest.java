package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.event.DirectoryEvent;
import com.atlassian.crowd.event.group.AutoGroupCreatedEvent;
import com.atlassian.crowd.event.group.AutoGroupMembershipCreatedEvent;
import com.atlassian.crowd.event.group.AutoGroupMembershipDeletedEvent;
import com.atlassian.crowd.event.group.GroupCreatedEvent;
import com.atlassian.crowd.event.group.GroupMembershipCreatedEvent;
import com.atlassian.crowd.event.group.GroupMembershipDeletedEvent;
import com.atlassian.crowd.event.user.AutoUserCreatedEvent;
import com.atlassian.crowd.event.user.AutoUserUpdatedEvent;
import com.atlassian.crowd.event.user.UserCreatedEvent;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.ReadOnlyGroupException;
import com.atlassian.crowd.exception.UserAlreadyExistsException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.InternalDirectoryGroup;
import com.atlassian.crowd.model.membership.MembershipType;
import com.atlassian.crowd.model.user.InternalUser;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.util.InternalEntityHelper;
import com.atlassian.event.api.EventPublisher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * DelegatedAuthenticationDirectory Tester.
 */
public class DelegatedAuthenticationDirectoryTest
{
    private static final Long DIRECTORY_ID = 1L;
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_USER_NAME_MIXEDCASE = "TestUser";
    private static final String TEST_USER_NAME = "testuser";
    private static final String TEST_REMOTE_GROUP_NAME1 = "remotegroup1";
    private static final String TEST_REMOTE_GROUP_NAME2 = "remotegroup2";

    private DelegatedAuthenticationDirectory directory;
    private EventPublisher eventPublisher;
    private PasswordCredential passwordCredential;
    private InternalUser user1;
    private InternalUser user2;
    private final RemoteDirectory internalDirectory = mock(RemoteDirectory.class);
    private RemoteDirectory ldapDirectory;
    private Group group;
    private final InternalDirectoryGroup internalDirectoryGroup = mock(InternalDirectoryGroup.class);
    private final Directory directoryAPI = mock(Directory.class);

    @Before
    public void setUp() throws Exception
    {
        passwordCredential = new PasswordCredential(TEST_PASSWORD, true);
        final DirectoryDao directoryDao = mock(DirectoryDao.class);
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directoryAPI);

        eventPublisher = mock(EventPublisher.class);

        when(internalDirectory.getDirectoryId()).thenReturn(DIRECTORY_ID);

        ldapDirectory = mock(RemoteDirectory.class);

        directory = new DelegatedAuthenticationDirectory(ldapDirectory, internalDirectory, eventPublisher, directoryDao);

        UserTemplate mixedCasePrincipal = new UserTemplate(TEST_USER_NAME_MIXEDCASE, DIRECTORY_ID);
        mixedCasePrincipal.setActive(true);

        user1 = InternalEntityHelper.createUser(DIRECTORY_ID, TEST_USER_NAME);
        user2 = InternalEntityHelper.createUser(DIRECTORY_ID, TEST_USER_NAME);
        group = mock(Group.class);
    }

    @Test
    public void testAuthentication() throws Exception
    {
        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);

        User remotePrincipal = directory.authenticate(TEST_USER_NAME, passwordCredential);

        verify(ldapDirectory).authenticate(TEST_USER_NAME, passwordCredential);
        assertEquals(user1, remotePrincipal);
    }

    @Test
    public void testAuthenticateWithInvalidCredentials() throws Exception
    {
        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        //noinspection ThrowableResultOfMethodCallIgnored
        when(ldapDirectory.authenticate(TEST_USER_NAME, passwordCredential)).thenThrow(InvalidAuthenticationException.newInstanceWithName(TEST_USER_NAME));

        try
        {
            directory.authenticate(TEST_USER_NAME, passwordCredential);
            fail("InvalidAuthenticationException expected");
        } catch (InvalidAuthenticationException e)
        {
            // expected
        }
    }

    @Test
    public void testAuthenticateWithInactiveUser() throws Exception
    {
        user1 = InternalEntityHelper.createUser(DIRECTORY_ID, TEST_USER_NAME, false);

        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);

        try
        {
            directory.authenticate(TEST_USER_NAME, passwordCredential);
            fail("InactiveAccountException expected");
        } catch (InactiveAccountException e)
        {
            // expected
        }
    }

    @Test
    public void testAuthenticateWhenUserDoesNotExistInternallyWithAutoCreate() throws Exception
    {
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_CREATE_USER_ON_AUTH)).thenReturn(Boolean.TRUE.toString());

        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenThrow(new UserNotFoundException(TEST_USER_NAME));
        when(ldapDirectory.authenticate(TEST_USER_NAME, passwordCredential)).thenReturn(user1);
        when(internalDirectory.addUser(new UserTemplate(user1), null)).thenReturn(user1);

        User remotePrincipal = directory.authenticate(TEST_USER_NAME, passwordCredential);

        verify(eventPublisher, times(1)).publish(isA(AutoUserCreatedEvent.class));
        assertEquals(user1, remotePrincipal);
    }

    @Test
    public void testAuthenticateWhenUserDoesNotExistInternallyWithoutAutoCreate() throws Exception
    {
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_CREATE_USER_ON_AUTH)).thenReturn(Boolean.FALSE.toString());

        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenThrow(new UserNotFoundException(TEST_USER_NAME));

        try
        {
            directory.authenticate(TEST_USER_NAME, passwordCredential);

            fail("UserNotFoundException expected");
        } catch (UserNotFoundException e)
        {
            // expected
        }

        verifyZeroInteractions(ldapDirectory);
        verifyZeroInteractions(eventPublisher);
    }

    @Test
    public void testAuthenticateWhenUserDoesNotExistInternallyWithAutoCreateButSuddenlyTheUserAppearsDuringTheAutoCreation() throws Exception
    {
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_CREATE_USER_ON_AUTH)).thenReturn(Boolean.TRUE.toString());

        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenThrow(new UserNotFoundException(TEST_USER_NAME)).thenReturn(user1);
        when(ldapDirectory.authenticate(TEST_USER_NAME, passwordCredential)).thenReturn(user1);
        when(internalDirectory.addUser(new UserTemplate(user1), null)).thenThrow(new UserAlreadyExistsException(DIRECTORY_ID, user1.getName()));

        DirectoryImpl directoryPojo = InternalEntityHelper.createDirectory(DIRECTORY_ID, "");
        directoryPojo.setAttribute(DelegatedAuthenticationDirectory.ATTRIBUTE_CREATE_USER_ON_AUTH, Boolean.TRUE.toString());

        User remotePrincipal = directory.authenticate(TEST_USER_NAME, passwordCredential);

        // if the user has been created by somebody else, the event should not be fired.
        verify(eventPublisher, times(0)).publish(isA(UserCreatedEvent.class));
        assertEquals(user1, remotePrincipal);
    }

    @Test
    public void testAuthenticateWhenUserDoesNotExistInternallyWithAutoCreateButSuddenlyTheUserAppearsDuringTheAutoCreationAndUserIsInactive() throws Exception
    {
        InternalUser inactiveUser = InternalEntityHelper.createUser(DIRECTORY_ID, TEST_USER_NAME, false);

        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_CREATE_USER_ON_AUTH)).thenReturn(Boolean.TRUE.toString());

        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenThrow(new UserNotFoundException(TEST_USER_NAME)).thenReturn(inactiveUser);
        when(ldapDirectory.authenticate(TEST_USER_NAME, passwordCredential)).thenReturn(user1);
        when(internalDirectory.addUser(new UserTemplate(user1), null)).thenThrow(new UserAlreadyExistsException(DIRECTORY_ID, user1.getName()));

        DirectoryImpl directoryPojo = InternalEntityHelper.createDirectory(DIRECTORY_ID, "");
        directoryPojo.setAttribute(DelegatedAuthenticationDirectory.ATTRIBUTE_CREATE_USER_ON_AUTH, Boolean.TRUE.toString());

        User remotePrincipal = null;
        try
        {
            remotePrincipal = directory.authenticate(TEST_USER_NAME, passwordCredential);
            fail("InactiveAccountException expected");
        } catch (InactiveAccountException iae)
        {
            // expected
        }

        // if the user has been created by somebody else, the event should not be fired.
        verify(eventPublisher, times(0)).publish(isA(DirectoryEvent.class));
        assertNull(remotePrincipal);
    }

    /**
     * When user exists locally, but has been removed from the remote server,
     * InvalidAuthenticationException should be thrown.
     *
     * @throws Exception hopefully every time
     */
    @Test(expected = InvalidAuthenticationException.class)
    public void testAuthenticateExistingUserHasBeenRemovedFromLDAP() throws Exception
    {
        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        when(ldapDirectory.authenticate(TEST_USER_NAME, passwordCredential)).thenThrow(new UserNotFoundException(TEST_USER_NAME));

        directory.authenticate(TEST_USER_NAME, passwordCredential);
    }

    @Test
    public void testAuthenticateWhenUserDoesExistInternallyWithAutoUpdate() throws Exception
    {
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_CREATE_USER_ON_AUTH)).thenReturn(Boolean.TRUE.toString());
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_UPDATE_USER_ON_AUTH)).thenReturn(Boolean.TRUE.toString());

        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        when(ldapDirectory.authenticate(TEST_USER_NAME, passwordCredential)).thenReturn(user1);
        when(internalDirectory.updateUser(new UserTemplate(user1))).thenReturn(user1);

        User remotePrincipal = directory.authenticate(TEST_USER_NAME, passwordCredential);

        verify(eventPublisher, times(1)).publish(isA(AutoUserUpdatedEvent.class));
        verify(internalDirectory).updateUser(new UserTemplate(user1));
        assertEquals(user1, remotePrincipal);
    }

    @Test
    public void testAuthenticateWithImportGroupsButNoAutoUpdate() throws Exception
    {
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_CREATE_USER_ON_AUTH)).thenReturn(Boolean.FALSE.toString());
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_UPDATE_USER_ON_AUTH)).thenReturn(Boolean.FALSE.toString());
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_KEY_IMPORT_GROUPS)).thenReturn(Boolean.TRUE.toString());

        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        when(ldapDirectory.authenticate(TEST_USER_NAME, passwordCredential)).thenReturn(user1);
        when(internalDirectory.updateUser(new UserTemplate(user1))).thenReturn(user1);

        // There are 2 memberships in the LDAP and only 1 membership in the local directory
        when(ldapDirectory.searchGroupRelationships(QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user1.getName()).returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(Arrays.asList(TEST_REMOTE_GROUP_NAME1, TEST_REMOTE_GROUP_NAME2));
        when(internalDirectory.searchGroupRelationships(QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user1.getName()).returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(Collections.singletonList(group));
        when(group.getName()).thenReturn(TEST_REMOTE_GROUP_NAME1);
        when(internalDirectory.findGroupByName(TEST_REMOTE_GROUP_NAME2)).thenReturn(internalDirectoryGroup);

        User remotePrincipal = directory.authenticate(TEST_USER_NAME, passwordCredential);

        verify(eventPublisher, never()).publish(isA(AutoUserUpdatedEvent.class));
        verify(eventPublisher, times(1)).publish(isA(AutoGroupMembershipCreatedEvent.class));
        verify(internalDirectory, times(1)).addUserToGroup(TEST_USER_NAME, TEST_REMOTE_GROUP_NAME2);
    }

    @Test
    public void testAddOrUpdateLdapUserWhenUserDoesNotExistInternally() throws Exception
    {
        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenThrow(new UserNotFoundException(TEST_USER_NAME));
        when(ldapDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        when(internalDirectory.addUser(new UserTemplate(user1), null)).thenReturn(user1);

        User remotePrincipal = directory.addOrUpdateLdapUser(TEST_USER_NAME);

        verify(eventPublisher, times(1)).publish(isA(AutoUserCreatedEvent.class));
        verify(internalDirectory).addUser(new UserTemplate(user1), null);
        assertEquals(user1, remotePrincipal);
    }

    @Test
    public void testAddOrUpdateLdapUserWhenUserExistsInternally() throws Exception
    {
        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        when(ldapDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        when(internalDirectory.updateUser(new UserTemplate(user1))).thenReturn(user1);

        User remotePrincipal = directory.addOrUpdateLdapUser(TEST_USER_NAME);

        verify(eventPublisher, times(1)).publish(isA(AutoUserUpdatedEvent.class));
        verify(internalDirectory).updateUser(new UserTemplate(user1));
        assertEquals(user1, remotePrincipal);
    }

    @Test
    public void testAddOrUpdateLdapUserWhenUserExistsInternallyButIsInactive() throws Exception
    {
        User inactiveUser = InternalEntityHelper.createUser(DIRECTORY_ID, TEST_USER_NAME, false);
        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenReturn(inactiveUser);
        when(ldapDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        when(internalDirectory.updateUser(any(UserTemplate.class))).thenReturn(inactiveUser);

        User remotePrincipal = directory.addOrUpdateLdapUser(TEST_USER_NAME);

        verify(eventPublisher, times(1)).publish(isA(AutoUserUpdatedEvent.class));
        verify(internalDirectory).updateUser(new UserTemplate(user1));
        ArgumentCaptor<UserTemplate> argument = ArgumentCaptor.forClass(UserTemplate.class);
        verify(internalDirectory).updateUser(argument.capture());
        assertFalse("should still not be active when updating", argument.getValue().isActive());
        assertEquals(user1, remotePrincipal);
        assertFalse("should still not be active after updating", remotePrincipal.isActive());
    }

    @Test
    public void testAddLdapUserAndGroupAndGroupMembership() throws Exception
    {
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_KEY_IMPORT_GROUPS)).thenReturn(String.valueOf(true));
        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenThrow(new UserNotFoundException(TEST_USER_NAME));
        when(ldapDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        when(ldapDirectory.searchGroupRelationships(QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user1.getName()).returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(Collections.singletonList(TEST_REMOTE_GROUP_NAME1));
        when(internalDirectory.findGroupByName(TEST_REMOTE_GROUP_NAME1)).thenThrow(new GroupNotFoundException(TEST_REMOTE_GROUP_NAME1));
        when(internalDirectory.addUser(new UserTemplate(user1), null)).thenReturn(user1);
        when(internalDirectory.addGroup(any(GroupTemplate.class))).thenReturn(group);

        User remotePrincipal = directory.addOrUpdateLdapUser(TEST_USER_NAME);

        verifyUserCreated(remotePrincipal);
        verifyGroupCreated(TEST_REMOTE_GROUP_NAME1);
        verifyGroupMembershipCreated(TEST_REMOTE_GROUP_NAME1);
    }

    @Test
    public void testAddLdapUserAndGroupMembershipToExistingGroup() throws Exception
    {
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_KEY_IMPORT_GROUPS)).thenReturn(String.valueOf(true));
        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenThrow(new UserNotFoundException(TEST_USER_NAME));
        when(ldapDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        when(ldapDirectory.searchGroupRelationships(QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user1.getName()).returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(Collections.singletonList(TEST_REMOTE_GROUP_NAME1));
        final InternalDirectoryGroup group = mock(InternalDirectoryGroup.class);
        when(group.isLocal()).thenReturn(false);
        when(internalDirectory.findGroupByName(TEST_REMOTE_GROUP_NAME1)).thenReturn(group);
        when(internalDirectory.addUser(new UserTemplate(user1), null)).thenReturn(user1);

        User remotePrincipal = directory.addOrUpdateLdapUser(TEST_USER_NAME);

        verifyUserCreated(remotePrincipal);
        verifyGroupNotCreated();
        verifyGroupMembershipCreated(TEST_REMOTE_GROUP_NAME1);
    }

    @Test
    public void testAddLdapUserButNotShadowedGroup() throws Exception
    {
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_KEY_IMPORT_GROUPS)).thenReturn(String.valueOf(true));
        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenThrow(new UserNotFoundException(TEST_USER_NAME));
        when(ldapDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        when(ldapDirectory.searchGroupRelationships(QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user1.getName()).returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(Collections.singletonList(TEST_REMOTE_GROUP_NAME1));
        final InternalDirectoryGroup group = mock(InternalDirectoryGroup.class);
        when(group.isLocal()).thenReturn(true);
        when(internalDirectory.findGroupByName(TEST_REMOTE_GROUP_NAME1)).thenReturn(group);
        when(internalDirectory.addUser(new UserTemplate(user1), null)).thenReturn(user1);

        User remotePrincipal = directory.addOrUpdateLdapUser(TEST_USER_NAME);

        verifyUserCreated(remotePrincipal);
        verifyGroupNotCreated();
        verifyGroupMembershipNotCreated();
    }

    @Test
    public void testAddLdapUserWithGroupImportDisabled() throws Exception
    {
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_KEY_IMPORT_GROUPS)).thenReturn(String.valueOf(false));
        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenThrow(new UserNotFoundException(TEST_USER_NAME));
        when(ldapDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        when(ldapDirectory.searchGroupRelationships(QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user1.getName()).returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(Collections.singletonList(TEST_REMOTE_GROUP_NAME1));
        when(internalDirectory.findGroupByName(TEST_REMOTE_GROUP_NAME1)).thenThrow(new GroupNotFoundException(TEST_REMOTE_GROUP_NAME1));
        when(internalDirectory.addUser(new UserTemplate(user1), null)).thenReturn(user1);
        when(internalDirectory.addGroup(any(GroupTemplate.class))).thenReturn(group);

        User remotePrincipal = directory.addOrUpdateLdapUser(TEST_USER_NAME);

        verifyUserCreated(remotePrincipal);
        verifyGroupNotCreated();
        verifyGroupMembershipNotCreated();
    }

    @Test
    public void testUpdateLdapUserWithGroupAndMembershipAdded() throws Exception
    {
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_KEY_IMPORT_GROUPS)).thenReturn(String.valueOf(true));
        when(ldapDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user2);
        when(internalDirectory.updateUser(Matchers.<UserTemplate>any())).thenReturn(user2);
        when(ldapDirectory.searchGroupRelationships(QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user1.getName()).returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(Arrays.asList(TEST_REMOTE_GROUP_NAME1, TEST_REMOTE_GROUP_NAME2));
        when(internalDirectory.searchGroupRelationships(QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user1.getName()).returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(Collections.singletonList(group));
        when(group.getName()).thenReturn(TEST_REMOTE_GROUP_NAME1);
        when(internalDirectory.findGroupByName(TEST_REMOTE_GROUP_NAME2)).thenThrow(new GroupNotFoundException(TEST_REMOTE_GROUP_NAME2));
        when(internalDirectory.addGroup(any(GroupTemplate.class))).thenReturn(group);

        directory.addOrUpdateLdapUser(TEST_USER_NAME);

        verify(eventPublisher, never()).publish(isA(UserCreatedEvent.class));
        verify(eventPublisher, times(1)).publish(isA(AutoUserUpdatedEvent.class));
        verify(eventPublisher, never()).publish(isA(GroupMembershipDeletedEvent.class));
        verifyGroupCreated(TEST_REMOTE_GROUP_NAME2);
        verifyGroupMembershipCreated(TEST_REMOTE_GROUP_NAME2);
    }

    @Test
    public void testUpdateLdapUserWithMembershipAdded() throws Exception
    {
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_KEY_IMPORT_GROUPS)).thenReturn(String.valueOf(true));
        when(ldapDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user2);
        when(internalDirectory.updateUser(Matchers.<UserTemplate>any())).thenReturn(user2);
        when(ldapDirectory.searchGroupRelationships(QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user1.getName()).returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(Arrays.asList(TEST_REMOTE_GROUP_NAME1, TEST_REMOTE_GROUP_NAME2));
        when(internalDirectory.searchGroupRelationships(QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user1.getName()).returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(Collections.singletonList(group));
        when(group.getName()).thenReturn(TEST_REMOTE_GROUP_NAME1);
        when(internalDirectory.findGroupByName(TEST_REMOTE_GROUP_NAME2)).thenReturn(internalDirectoryGroup);
        when(internalDirectory.addGroup(any(GroupTemplate.class))).thenReturn(group);

        directory.addOrUpdateLdapUser(TEST_USER_NAME);

        verify(eventPublisher, never()).publish(isA(AutoUserCreatedEvent.class));
        verify(eventPublisher, times(1)).publish(isA(AutoUserUpdatedEvent.class));
        verify(eventPublisher, never()).publish(isA(GroupMembershipDeletedEvent.class));
        verifyGroupNotCreated();
        verifyGroupMembershipCreated(TEST_REMOTE_GROUP_NAME2);
    }

    @Test
    public void testUpdateLdapUserWithMembershipRemoved() throws Exception
    {
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_KEY_IMPORT_GROUPS)).thenReturn(String.valueOf(true));
        when(ldapDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user2);
        when(internalDirectory.updateUser(Matchers.<UserTemplate>any())).thenReturn(user2);
        when(ldapDirectory.searchGroupRelationships(QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user1.getName()).returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(Collections.singletonList(TEST_REMOTE_GROUP_NAME1));
        when(internalDirectory.searchGroupRelationships(QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user1.getName()).returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(Arrays.asList(group, internalDirectoryGroup));
        when(group.getName()).thenReturn(TEST_REMOTE_GROUP_NAME1);
        when(internalDirectoryGroup.getName()).thenReturn(TEST_REMOTE_GROUP_NAME2);
        when(internalDirectory.findGroupByName(TEST_REMOTE_GROUP_NAME2)).thenReturn(internalDirectoryGroup);

        directory.addOrUpdateLdapUser(TEST_USER_NAME);

        verify(eventPublisher, never()).publish(isA(AutoUserCreatedEvent.class));
        verify(eventPublisher, times(1)).publish(isA(AutoUserUpdatedEvent.class));
        verify(eventPublisher, times(1)).publish(isA(AutoGroupMembershipDeletedEvent.class));
        verifyGroupNotCreated();
        verifyGroupMembershipNotCreated();
        verify(internalDirectory, times(1)).removeUserFromGroup(TEST_USER_NAME, TEST_REMOTE_GROUP_NAME2);
    }

    @Test
    public void testUpdateLdapUserWithMembershipUnchanged() throws Exception
    {
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_KEY_IMPORT_GROUPS)).thenReturn(String.valueOf(true));
        when(ldapDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user2);
        when(internalDirectory.updateUser(Matchers.<UserTemplate>any())).thenReturn(user2);
        when(ldapDirectory.searchGroupRelationships(QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user1.getName()).returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(Collections.singletonList(TEST_REMOTE_GROUP_NAME1));
        when(internalDirectory.searchGroupRelationships(QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user1.getName()).returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(Arrays.asList(group));
        when(group.getName()).thenReturn(TEST_REMOTE_GROUP_NAME1);

        directory.addOrUpdateLdapUser(TEST_USER_NAME);

        verify(eventPublisher, never()).publish(isA(UserCreatedEvent.class));
        verify(eventPublisher, times(1)).publish(isA(AutoUserUpdatedEvent.class));
        verify(eventPublisher, never()).publish(isA(GroupMembershipDeletedEvent.class));
        verifyGroupNotCreated();
        verifyGroupMembershipNotCreated();
        verify(internalDirectory, never()).removeUserFromGroup(Matchers.<String>any(), Matchers.<String>any());
    }

    @Test
    public void testUpdateLdapUserWithGroupImportDisabled() throws Exception
    {
        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_KEY_IMPORT_GROUPS)).thenReturn(String.valueOf(false));
        when(ldapDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user1);
        when(internalDirectory.findUserByName(TEST_USER_NAME)).thenReturn(user2);
        when(internalDirectory.updateUser(Matchers.<UserTemplate>any())).thenReturn(user2);
        when(ldapDirectory.searchGroupRelationships(QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user1.getName()).returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(Arrays.asList(TEST_REMOTE_GROUP_NAME1, TEST_REMOTE_GROUP_NAME2));
        when(internalDirectory.searchGroupRelationships(QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user1.getName()).returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(Collections.singletonList(group));
        when(group.getName()).thenReturn(TEST_REMOTE_GROUP_NAME1);
        when(internalDirectory.findGroupByName(TEST_REMOTE_GROUP_NAME2)).thenThrow(new GroupNotFoundException(TEST_REMOTE_GROUP_NAME2));
        when(internalDirectory.addGroup(any(GroupTemplate.class))).thenReturn(group);

        directory.addOrUpdateLdapUser(TEST_USER_NAME);

        verify(eventPublisher, never()).publish(isA(UserCreatedEvent.class));
        verify(eventPublisher, times(1)).publish(isA(AutoUserUpdatedEvent.class));
        verify(eventPublisher, never()).publish(isA(GroupMembershipDeletedEvent.class));
        verifyGroupNotCreated();
        verifyGroupMembershipNotCreated();
        verify(internalDirectory, never()).removeUserFromGroup(Matchers.<String>any(), Matchers.<String>any());
    }

    @Test
    public void testUpdateLdapUserIgnoresUsernameCasing() throws Exception
    {
        final String lowercaseUsername = "user";
        final String uppercaseUsername = "USER";

        final InternalUser lowercaseUser = InternalEntityHelper.createUser(DIRECTORY_ID, lowercaseUsername);
        final InternalUser uppercaseUser = InternalEntityHelper.createUser(DIRECTORY_ID, uppercaseUsername);

        when(internalDirectory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_KEY_IMPORT_GROUPS)).thenReturn(String.valueOf(false));
        when(ldapDirectory.findUserByName(lowercaseUsername)).thenReturn(uppercaseUser);
        when(internalDirectory.findUserByName(lowercaseUsername)).thenReturn(lowercaseUser);
        when(internalDirectory.updateUser(Matchers.<UserTemplate>any())).thenReturn(lowercaseUser);

        directory.addOrUpdateLdapUser(lowercaseUsername);

        verify(eventPublisher, never()).publish(isA(UserCreatedEvent.class));
        verify(internalDirectory, times(1)).updateUser(argThat(new ArgumentMatcher<UserTemplate>()
        {
            @Override
            public boolean matches(Object argument)
            {
                return lowercaseUsername.equals(((UserTemplate) argument).getName());
            }
        }));
        verify(eventPublisher, times(1)).publish(isA(AutoUserUpdatedEvent.class));
    }

    private void verifyUserCreated(User remotePrincipal) throws InvalidUserException, InvalidCredentialException, UserAlreadyExistsException, OperationFailedException
    {
        assertEquals(user1, remotePrincipal);
        verify(internalDirectory, times(1)).addUser(new UserTemplate(user1), null);
        verify(eventPublisher, times(1)).publish(isA(AutoUserCreatedEvent.class));
    }

    private void verifyGroupCreated(final String name) throws InvalidGroupException, OperationFailedException
    {
        final ArgumentCaptor<GroupTemplate> groupTemplate = ArgumentCaptor.forClass(GroupTemplate.class);
        verify(internalDirectory, times(1)).addGroup(groupTemplate.capture());
        assertEquals(new GroupTemplate(name, DIRECTORY_ID), groupTemplate.getValue());
        assertFalse(groupTemplate.getValue().isLocal());
        verify(eventPublisher, times(1)).publish(argThat(new ArgumentMatcher<AutoGroupCreatedEvent>()
        {

            @Override
            public boolean matches(final Object o)
            {
                if (o instanceof AutoGroupCreatedEvent)
                {
                    final AutoGroupCreatedEvent event = (AutoGroupCreatedEvent) o;
                    return event.getDirectory() == directoryAPI && event.getGroup() == group && event.getSource() == directory;
                } else
                {
                    return false;
                }
            }

        }));
    }

    private void verifyGroupNotCreated() throws InvalidGroupException, OperationFailedException
    {
        verify(internalDirectory, never()).addGroup(any(GroupTemplate.class));
        verify(eventPublisher, never()).publish(isA(GroupCreatedEvent.class));
    }

    private void verifyGroupMembershipCreated(final String groupName) throws GroupNotFoundException, UserNotFoundException, ReadOnlyGroupException, OperationFailedException
    {
        verify(internalDirectory, times(1)).addUserToGroup(TEST_USER_NAME, groupName);
        verify(eventPublisher, times(1)).publish(argThat(new ArgumentMatcher<AutoGroupMembershipCreatedEvent>()
        {

            @Override
            public boolean matches(final Object o)
            {
                if (o instanceof AutoGroupMembershipCreatedEvent)
                {
                    final AutoGroupMembershipCreatedEvent event = (AutoGroupMembershipCreatedEvent) o;
                    return event.getDirectory() == directoryAPI && event.getEntityName().equals(TEST_USER_NAME) && event.getGroupName().equals(groupName) && event.getMembershipType().equals(MembershipType.GROUP_USER) && event.getSource() == directory;
                } else
                {
                    return false;
                }
            }

        }));
    }

    private void verifyGroupMembershipNotCreated() throws GroupNotFoundException, UserNotFoundException, ReadOnlyGroupException, OperationFailedException
    {
        verify(internalDirectory, never()).addUserToGroup(anyString(), anyString());
        verify(eventPublisher, never()).publish(isA(GroupMembershipCreatedEvent.class));
    }

}
