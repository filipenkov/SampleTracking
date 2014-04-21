package com.atlassian.crowd.core.event.listener;

import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.event.user.UserAuthenticatedEvent;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AutoGroupAdderListenerTest
{
    private static final String USER_NAME = "user";
    private static final User USER = new UserTemplate(USER_NAME);

    @Mock private Directory directory;
    @Mock private DirectoryInstanceLoader directoryInstanceLoader;
    @Mock private RemoteDirectory remoteDirectory;
    @Mock private Application application;
    @Mock private UserWithAttributes userWithAttributes;
    private UserAuthenticatedEvent event;

    private AutoGroupAdderListener autoGroupAdderListener;

    @Before
    public void setUp() throws Exception
    {
        autoGroupAdderListener = new AutoGroupAdderListener();
        autoGroupAdderListener.setDirectoryInstanceLoader(directoryInstanceLoader);

        event = new UserAuthenticatedEvent(this, directory, application, USER);
    }

    @Test
    public void testHandleEventForDirectoryWithAutoAdd() throws Exception
    {
        when(directory.getAllowedOperations()).thenReturn(Collections.singleton(OperationType.UPDATE_GROUP));
        when(directoryInstanceLoader.getDirectory(directory)).thenReturn(remoteDirectory);
        when(directory.getValue(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS)).thenReturn("group1|group2|group3");
        when(remoteDirectory.findUserWithAttributesByName(USER_NAME)).thenReturn(userWithAttributes);
        when(remoteDirectory.searchGroupRelationships(any(MembershipQuery.class))).thenReturn(Collections.<String>emptyList());

        autoGroupAdderListener.handleEvent(event);

        verify(remoteDirectory).findUserWithAttributesByName(USER_NAME);
        verify(remoteDirectory).searchGroupRelationships(any(MembershipQuery.class));
        verify(remoteDirectory).addUserToGroup(USER_NAME, "group1");
        verify(remoteDirectory).addUserToGroup(USER_NAME, "group2");
        verify(remoteDirectory).addUserToGroup(USER_NAME, "group3");
        verify(remoteDirectory).storeUserAttributes(USER_NAME, ImmutableMap.of(AutoGroupAdderListener.AUTO_GROUPS_ADDED, Collections.singleton("true")));

        verifyNoMoreInteractions(remoteDirectory);
    }

    @Test
    public void testHandleEventForDirectoryWithAutoAddWhenOneGroupDoesNotExist() throws Exception
    {
        when(directory.getAllowedOperations()).thenReturn(Collections.singleton(OperationType.UPDATE_GROUP));
        when(directoryInstanceLoader.getDirectory(directory)).thenReturn(remoteDirectory);
        when(directory.getValue(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS)).thenReturn("group1|group2|group3");
        when(remoteDirectory.findUserWithAttributesByName(USER_NAME)).thenReturn(userWithAttributes);
        when(remoteDirectory.searchGroupRelationships(any(MembershipQuery.class))).thenReturn(Collections.<String>emptyList());
        doThrow(new GroupNotFoundException("group2")).when(remoteDirectory).addUserToGroup(USER_NAME, "group2");

        autoGroupAdderListener.handleEvent(event);

        verify(remoteDirectory).findUserWithAttributesByName(USER_NAME);
        verify(remoteDirectory).searchGroupRelationships(any(MembershipQuery.class));
        verify(remoteDirectory).addUserToGroup(USER_NAME, "group1");
        verify(remoteDirectory).addUserToGroup(USER_NAME, "group2");
        verify(remoteDirectory).addUserToGroup(USER_NAME, "group3"); // still executes
        verify(remoteDirectory).storeUserAttributes(USER_NAME, ImmutableMap.of(AutoGroupAdderListener.AUTO_GROUPS_ADDED, Collections.singleton("true")));

        verifyNoMoreInteractions(remoteDirectory);
    }

    @Test
    public void testHandleEventForUserWithSomeExistingMemberships() throws Exception
    {
        when(directory.getAllowedOperations()).thenReturn(Collections.singleton(OperationType.UPDATE_GROUP));
        when(directoryInstanceLoader.getDirectory(directory)).thenReturn(remoteDirectory);
        when(directory.getValue(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS)).thenReturn("group1|group2|group3");
        when(remoteDirectory.findUserWithAttributesByName(USER_NAME)).thenReturn(userWithAttributes);
        when(remoteDirectory.searchGroupRelationships(any(MembershipQuery.class))).thenReturn(Collections.singletonList("group2"));

        autoGroupAdderListener.handleEvent(event);

        verify(remoteDirectory).findUserWithAttributesByName(USER_NAME);
        verify(remoteDirectory).searchGroupRelationships(any(MembershipQuery.class));
        verify(remoteDirectory).addUserToGroup(USER_NAME, "group1");
        verify(remoteDirectory).addUserToGroup(USER_NAME, "group3");
        verify(remoteDirectory).storeUserAttributes(USER_NAME, ImmutableMap.of(AutoGroupAdderListener.AUTO_GROUPS_ADDED, Collections.singleton("true")));

        verifyNoMoreInteractions(remoteDirectory);
    }

    @Test
    public void testHandleEventForUserWithAllExistingMemberships() throws Exception
    {
        when(directory.getAllowedOperations()).thenReturn(Collections.singleton(OperationType.UPDATE_GROUP));
        when(directoryInstanceLoader.getDirectory(directory)).thenReturn(remoteDirectory);
        when(directory.getValue(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS)).thenReturn("group1|group2|group3");
        when(remoteDirectory.findUserWithAttributesByName(USER_NAME)).thenReturn(userWithAttributes);
        when(remoteDirectory.searchGroupRelationships(any(MembershipQuery.class))).thenReturn(Arrays.asList("group1", "group2", "group3"));

        autoGroupAdderListener.handleEvent(event);

        verify(remoteDirectory).findUserWithAttributesByName(USER_NAME);
        verify(remoteDirectory).searchGroupRelationships(any(MembershipQuery.class));
        verify(remoteDirectory).storeUserAttributes(USER_NAME, ImmutableMap.of(AutoGroupAdderListener.AUTO_GROUPS_ADDED, Collections.singleton("true")));

        verifyNoMoreInteractions(remoteDirectory);
    }

    @Test
    public void testHandleEventForUserWithMixedCaseExistingMemberships() throws Exception
    {
        when(directory.getAllowedOperations()).thenReturn(Collections.singleton(OperationType.UPDATE_GROUP));
        when(directoryInstanceLoader.getDirectory(directory)).thenReturn(remoteDirectory);
        when(directory.getValue(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS)).thenReturn("group1|group2|group3");
        when(remoteDirectory.findUserWithAttributesByName(USER_NAME)).thenReturn(userWithAttributes);
        when(remoteDirectory.searchGroupRelationships(any(MembershipQuery.class))).thenReturn(Arrays.asList("Group1", "GROUP2", "group3"));

        autoGroupAdderListener.handleEvent(event);

        verify(remoteDirectory).findUserWithAttributesByName(USER_NAME);
        verify(remoteDirectory).searchGroupRelationships(any(MembershipQuery.class));
        verify(remoteDirectory).storeUserAttributes(USER_NAME, ImmutableMap.of(AutoGroupAdderListener.AUTO_GROUPS_ADDED, Collections.singleton("true")));

        verifyNoMoreInteractions(remoteDirectory);
    }

    @Test
    public void testHandleEventForDirectoryWithNoPermission()
    {
        when(directory.getAllowedOperations()).thenReturn(Collections.<OperationType>emptySet());

        autoGroupAdderListener.handleEvent(event);

        verifyNoMoreInteractions(remoteDirectory);
    }

    @Test
    public void testHandleEventForDirectoryWithoutAutoAdd() throws Exception
    {
        when(directoryInstanceLoader.getDirectory(directory)).thenReturn(remoteDirectory);
        when(directory.getValue(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS)).thenReturn(null);

        autoGroupAdderListener.handleEvent(event);

        verifyNoMoreInteractions(remoteDirectory);
    }

    @Test
    public void testHandleEventWithPreviouslyAuthenticateUser() throws Exception
    {
        when(directory.getAllowedOperations()).thenReturn(Collections.singleton(OperationType.UPDATE_GROUP));
        when(directoryInstanceLoader.getDirectory(directory)).thenReturn(remoteDirectory);
        when(directory.getValue(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS)).thenReturn("group1|group2|group3");
        when(userWithAttributes.getValue(AutoGroupAdderListener.AUTO_GROUPS_ADDED)).thenReturn("true");
        when(remoteDirectory.findUserWithAttributesByName(USER_NAME)).thenReturn(userWithAttributes);

        autoGroupAdderListener.handleEvent(event);

        verify(remoteDirectory).findUserWithAttributesByName(USER_NAME);

        verifyNoMoreInteractions(remoteDirectory);
    }
}
