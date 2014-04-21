package com.atlassian.crowd.manager.application;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.UserComparator;
import com.atlassian.crowd.event.user.UserAuthenticatedEvent;
import com.atlassian.crowd.event.user.UserAuthenticationFailedInvalidAuthenticationEvent;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.BulkAddFailedException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.directory.BulkAddResult;
import com.atlassian.crowd.manager.directory.DirectoryPermissionException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TestCase to cover Principal based operations on the ApplicationManager
 */
@SuppressWarnings({"ThrowableInstanceNeverThrown"})
public class ApplicationServicePrincipalTestCase extends ApplicationServiceTestCase
{
    protected User duplicatePrincipal1Username;
    protected User duplicatePrincipal2Username;

    private PasswordCredential credential;

    protected User anotherPrincipal1;

    @Before
    public void setUp()
    {
        super.setUp();

        credential = PasswordCredential.unencrypted("password");

        duplicatePrincipal1Username = mock(User.class);
        when(duplicatePrincipal1Username.getName()).thenReturn(USER1_NAME);
        when(duplicatePrincipal1Username.getDirectoryId()).thenReturn(DIRECTORY2_ID);

        duplicatePrincipal2Username = mock(User.class);
        when(duplicatePrincipal2Username.getName()).thenReturn(USER2_NAME);
        when(duplicatePrincipal2Username.getDirectoryId()).thenReturn(DIRECTORY3_ID);

        anotherPrincipal1 = mock(User.class);
        when(anotherPrincipal1.getName()).thenReturn(USER1_NAME);
        when(anotherPrincipal1.getDirectoryId()).thenReturn(DIRECTORY2_ID);
    }

    @After
    public void tearDown()
    {
        duplicatePrincipal1Username = null;
        duplicatePrincipal2Username = null;

        credential = null;

        super.tearDown();
    }

    @Test
    public void testAuthenticateUser() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.authenticateUser(DIRECTORY1_ID, USER1_NAME, credential)).thenReturn(principal1);

        User user = applicationService.authenticateUser(application, USER1_NAME, credential);
        assertNotNull(user);

        verify(mockEventPublisher).publish(new UserAuthenticatedEvent(applicationService, directory1, application, user));
    }

    @Test
    public void testAuthenticateUserWhenTheErrorUnderlyingFirstDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.authenticateUser(DIRECTORY1_ID, USER1_NAME, credential)).thenThrow(new OperationFailedException("failed"));
        when(directoryManager.authenticateUser(DIRECTORY2_ID, USER1_NAME, credential)).thenReturn(anotherPrincipal1);

        // although the first directory throws an OperationFailedException, this should not terminate the whole process.
        User user = applicationService.authenticateUser(application, USER1_NAME, credential);
        assertNotNull(user);
        assertEquals(anotherPrincipal1, user);

        verify(mockEventPublisher, times(0)).publish(new UserAuthenticatedEvent(applicationService, directory1, application, user));
        verify(mockEventPublisher, times(1)).publish(new UserAuthenticatedEvent(applicationService, directory2, application, user));
    }

    @Test
    public void testAuthenticateUserWhenTheErrorUnderlyingAllDirectories() throws Exception
    {
        final OperationFailedException exceptionFromDirectory1 = new OperationFailedException("failed1");
        final OperationFailedException exceptionFromDirectory2 = new OperationFailedException("failed2");

        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.authenticateUser(DIRECTORY1_ID, USER1_NAME, credential)).thenThrow(exceptionFromDirectory1);
        when(directoryManager.authenticateUser(DIRECTORY2_ID, USER1_NAME, credential)).thenThrow(exceptionFromDirectory2);

        try
        {
            applicationService.authenticateUser(application, USER1_NAME, credential);
            fail("OperationFailedException expected");
        }
        catch (OperationFailedException ofe)
        {
            assertSame("the exception from the first directory should bubble", exceptionFromDirectory1, ofe);
        }
    }

    @Test
    public void testAuthenticateUserWhenTheErrorUnderlyingFirstDirectoryAndUserNotExistInSecond() throws Exception
    {
        final OperationFailedException exceptionFromDirectory1 = new OperationFailedException("failed1");

        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.authenticateUser(DIRECTORY1_ID, USER1_NAME, credential)).thenThrow(exceptionFromDirectory1);
        when(directoryManager.authenticateUser(DIRECTORY2_ID, USER1_NAME, credential)).thenThrow(new UserNotFoundException(USER1_NAME));

        try
        {
            applicationService.authenticateUser(application, USER1_NAME, credential);
            fail("OperationFailedException expected");
        }
        catch (OperationFailedException ofe)
        {
            assertSame("the exception from the first directory should bubble", exceptionFromDirectory1, ofe);
        }
    }

    /**
     * Tests that a user in an inactive directory is not authenticated.
     */
    @Test
    public void testAuthenticateUser_InactiveDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(inactiveDirectoryMapping));

        try
        {
            applicationService.authenticateUser(application, inactivePrincipal.getName(), credential);
            fail("Directory is inactive.  Should have thrown an InvalidAuthenticationException");
        }
        catch (UserNotFoundException e)
        {
            // correct behaviour
        }
        verify(directoryManager, never()).authenticateUser(INACTIVE_DIRECTORY_ID, INACTIVE_USER_NAME, credential);
    }

    @Test
    public void testInvalidUserAuthentication() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.authenticateUser(DIRECTORY1_ID, USER1_NAME, credential)).thenThrow(InvalidAuthenticationException.newInstanceWithName(USER1_NAME));

        try
        {
            applicationService.authenticateUser(application, principal1.getName(), credential);
            fail("Expected an invalid authentication exception");
        }
        catch (InvalidAuthenticationException e)
        {
            // we should be here
        }
        verify(mockEventPublisher).publish(new UserAuthenticationFailedInvalidAuthenticationEvent(directoryManager, directory1, USER1_NAME));
    }

    @Test(expected = OperationFailedException.class)
    public void testStoreUserAttributes_WithErrorInUnderlyingDirectory()
            throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);

        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_USER_ATTRIBUTE)).thenReturn(true);
        doThrow(new OperationFailedException()).when(directoryManager).storeUserAttributes(anyLong(), anyString(), any(Map.class));

        applicationService.storeUserAttributes(application, USER1_NAME, new HashMap<String, Set<String>>());
    }

    @Test(expected = ApplicationPermissionException.class)
    public void testStoreUserAttributes_WithNoApplicationPermissions() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_USER_ATTRIBUTE)).thenReturn(false);

        applicationService.storeUserAttributes(application, USER1_NAME, new HashMap<String, Set<String>>());
    }

    @Test(expected = UserNotFoundException.class)
    public void testStoreUserAttributes_WherePrincipalDoesNotExist() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenThrow(new UserNotFoundException(USER1_NAME));

        applicationService.storeUserAttributes(application, principal1.getName(), new HashMap<String, Set<String>>());
    }

    @Test
    public void testStoreUserAttributes() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // Setup expectations
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER2_NAME)).thenThrow(new UserNotFoundException(USER2_NAME));
        when(directoryManager.findUserByName(DIRECTORY2_ID, USER2_NAME)).thenReturn(principal2);

        when(directoryManager.findDirectoryById(DIRECTORY2_ID)).thenReturn(directory2);

        when(permissionManager.hasPermission(application, directory2, OperationType.UPDATE_USER_ATTRIBUTE)).thenReturn(true);

        Map<String, Set<String>> attribs = new HashMap<String, Set<String>>();
        attribs.put("beep", Sets.newHashSet("boop"));

        // make the call
        applicationService.storeUserAttributes(application, USER2_NAME, attribs);

        verify(directoryManager).storeUserAttributes(DIRECTORY2_ID, USER2_NAME, attribs);
    }

    @Test(expected = OperationFailedException.class)
    public void testAddUser_WithErrorInUnderlyingDirectory()
            throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // add principal to directory 1, but directory 2 fails and hence failure results

        // find ensures user doesn't exist
        when(directoryManager.findUserByName(anyLong(), eq(USER1_NAME))).thenThrow(new UserNotFoundException(USER1_NAME));

        // OperationFailedException and hence failure results
        when(directoryManager.addUser(eq(DIRECTORY1_ID), any(UserTemplate.class), any(PasswordCredential.class))).thenThrow(new OperationFailedException());

        // permissions are fine
        when(permissionManager.hasPermission(application, directory1, OperationType.CREATE_USER)).thenReturn(true);

        applicationService.addUser(application, new UserTemplate(principal1), credential);
    }

    @Test(expected = ApplicationPermissionException.class)
    public void testAddUser_WithNoApplicationPermissions() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        // no directories have add permissions

        // find ensures user doesn't preexist
        when(directoryManager.findUserByName(anyLong(), eq(USER1_NAME))).thenThrow(new UserNotFoundException(USER1_NAME));
        when(permissionManager.hasPermission(application, directory1, OperationType.CREATE_USER)).thenReturn(false);

        applicationService.addUser(application, new UserTemplate(principal1), credential);
    }

    @Test
    public void testAddAllUsers() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(inactiveDirectoryMapping, directoryMapping1, directoryMapping2));

        Collection<UserTemplateWithCredentialAndAttributes> principals = new ArrayList<UserTemplateWithCredentialAndAttributes>();
        principals.add(new UserTemplateWithCredentialAndAttributes(principal1, PasswordCredential.unencrypted("blah")));
        principals.add(new UserTemplateWithCredentialAndAttributes(principal2, PasswordCredential.unencrypted("blah")));

        // We expect users to be added to directory1 but not directory2
        when(directoryManager.addAllUsers(DIRECTORY1_ID, principals, false)).thenReturn(new BulkAddResult(0, false));

        // permissions are fine
        when(permissionManager.hasPermission(application, directory1, OperationType.CREATE_USER)).thenReturn(true);

        applicationService.addAllUsers(application, principals);

        verify(directoryManager, never()).addAllUsers(INACTIVE_DIRECTORY_ID, principals, false);
    }

    @Test
    public void testAddAllUsersRespectsPermissions() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));

        Collection<UserTemplateWithCredentialAndAttributes> principals = new ArrayList<UserTemplateWithCredentialAndAttributes>();
        principals.add(new UserTemplateWithCredentialAndAttributes(principal1, PasswordCredential.unencrypted("blah")));
        principals.add(new UserTemplateWithCredentialAndAttributes(principal2, PasswordCredential.unencrypted("blah")));

        // Directory1 is read-only, so we add to directory2
        when(directoryManager.addAllUsers(DIRECTORY2_ID, principals, false)).thenReturn(new BulkAddResult(0, false));

        // permissions are fine
        when(permissionManager.hasPermission(application, directory1, OperationType.CREATE_USER)).thenReturn(false);
        when(permissionManager.hasPermission(application, directory2, OperationType.CREATE_USER)).thenReturn(true);

        applicationService.addAllUsers(application, principals);
    }

    @Test(expected = ApplicationPermissionException.class)
    public void testAddAllUsers_directoryExceptions() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        Collection<UserTemplateWithCredentialAndAttributes> principals = new ArrayList<UserTemplateWithCredentialAndAttributes>();
        principals.add(new UserTemplateWithCredentialAndAttributes(principal1, PasswordCredential.unencrypted("blah")));
        principals.add(new UserTemplateWithCredentialAndAttributes(principal2, PasswordCredential.unencrypted("blah")));
        principals.add(new UserTemplateWithCredentialAndAttributes(principal3, PasswordCredential.unencrypted("blah")));

        // throw DirectoryPermissionException on DirectoryManager.addAllUsers()
        when(directoryManager.addAllUsers(DIRECTORY2_ID, principals, false)).thenThrow(new DirectoryPermissionException("No Permission"));

        // permissions are fine
        when(permissionManager.hasPermission(application, directory1, OperationType.CREATE_USER)).thenReturn(false);
        when(permissionManager.hasPermission(application, directory2, OperationType.CREATE_USER)).thenReturn(true);

        applicationService.addAllUsers(application, principals);
    }

    @Test(expected = ApplicationPermissionException.class)
    public void testAddAllUsersNoAddPermission() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        Collection<UserTemplateWithCredentialAndAttributes> principals = new ArrayList<UserTemplateWithCredentialAndAttributes>();
        principals.add(new UserTemplateWithCredentialAndAttributes(principal1, PasswordCredential.unencrypted("blah")));
        principals.add(new UserTemplateWithCredentialAndAttributes(principal2, PasswordCredential.unencrypted("blah")));
        //no add permission
        when(permissionManager.hasPermission(application, directory1, OperationType.CREATE_USER)).thenReturn(false);
        when(permissionManager.hasPermission(application, directory2, OperationType.CREATE_USER)).thenReturn(false);

        applicationService.addAllUsers(application, principals);
    }

    @Test
    public void testAddAllUsersOneUserExisted() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        Collection<UserTemplateWithCredentialAndAttributes> principals = new ArrayList<UserTemplateWithCredentialAndAttributes>();
        principals.add(new UserTemplateWithCredentialAndAttributes(principal1, PasswordCredential.unencrypted("blah")));
        principals.add(new UserTemplateWithCredentialAndAttributes(principal2, PasswordCredential.unencrypted("blah")));
        principals.add(new UserTemplateWithCredentialAndAttributes(principal3, PasswordCredential.unencrypted("blah")));

        final BulkAddResult<User> result = new BulkAddResult<User>(0, false);
        result.addExistingEntity(principal3);
        when(directoryManager.addAllUsers(DIRECTORY1_ID, principals, false)).thenReturn(result);

        // permissions are fine
        when(permissionManager.hasPermission(application, directory1, OperationType.CREATE_USER)).thenReturn(true);

        try
        {
            applicationService.addAllUsers(application, principals);
            fail("BulkAddFailedException expected!");
        }
        catch (BulkAddFailedException ex)
        {
            final Set<String> failedUsers = ex.getExistingUsers();
            assertEquals(1, failedUsers.size());
            assertEquals(USER3_NAME, failedUsers.iterator().next());
            assertEquals(0, ex.getFailedUsers().size());
        }
    }

    @Test
    public void testAddUser() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // add user to permissible directories 1 and 3

        // ensure user doesn't exist
        when(directoryManager.findUserByName(anyLong(), eq(USER1_NAME))).thenThrow(new UserNotFoundException(USER1_NAME));

        // Won't try to add to Directory1 because no CREATE_USER permission.
        when(directoryManager.addUser(eq(DIRECTORY2_ID), any(UserTemplate.class), any(PasswordCredential.class))).thenReturn(principal1);

        // permissions are fine
        when(permissionManager.hasPermission(application, directory1, OperationType.CREATE_USER)).thenReturn(false);
        when(permissionManager.hasPermission(application, directory2, OperationType.CREATE_USER)).thenReturn(true);

        User returnedPrincipal = applicationService.addUser(application, new UserTemplate(principal1), PasswordCredential.unencrypted(""));

        assertEquals(principal1, returnedPrincipal);
    }

    @Test(expected = InvalidUserException.class)
    public void testAddUser_UserAlreadyExists() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        // try to add existing principal
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);

        applicationService.addUser(application, new UserTemplate(principal1), new PasswordCredential(""));
    }

    @Test
    public void testUpdateUser() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.findUserByName(anyLong(), anyString())).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);

        // permissions are fine
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_USER)).thenReturn(true);
        when(directoryManager.updateUser(eq(DIRECTORY1_ID), any(UserTemplate.class))).thenReturn(principal1);

        final UserTemplate userTemplate = new UserTemplate(principal1);
        userTemplate.setDisplayName("Katie Mouse");
        User returnedPrincipal = applicationService.updateUser(application, userTemplate);

        assertEquals(principal1, returnedPrincipal);
    }

    @Test(expected = UserNotFoundException.class)
    public void testUpdateUser_InactiveDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(inactiveDirectoryMapping));
        verify(directoryManager, never()).findDirectoryById(anyLong());
        verify(directoryManager, never()).updateUser(anyLong(), any(UserTemplate.class));
        verify(permissionManager, never()).hasPermission(any(Application.class), any(Directory.class), any(OperationType.class));

        final UserTemplate userTemplate = new UserTemplate(inactivePrincipal);
        userTemplate.setDisplayName("Katie Mouse");

        applicationService.updateUser(application, userTemplate);
    }

    @Test(expected = InvalidUserException.class)
    public void testUpdateUserWithInvalidDirectoryId() throws Exception
    {
        final long INVALID_DIRECTORY_ID = 4L;
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // ensure user doesn't exist
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);

        final UserTemplate userTemplate = new UserTemplate(principal1);
        userTemplate.setDirectoryId(INVALID_DIRECTORY_ID);
        applicationService.updateUser(application, userTemplate);
    }

    @Test(expected = UserNotFoundException.class)
    public void testFindUserByName_WithErrorInUnderlyingDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // principal exists in none of the directories
        when(directoryManager.findUserByName(anyLong(), eq(USER1_NAME))).thenThrow(new UserNotFoundException(USER1_NAME));

        applicationService.findUserByName(application, USER1_NAME);
    }

    @Test
    public void testFindUserByName() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // principal exists in 2nd directory
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenThrow(new UserNotFoundException(USER1_NAME));
        when(directoryManager.findUserByName(DIRECTORY2_ID, USER1_NAME)).thenReturn(principal1);

        User returnedPrincipal = applicationService.findUserByName(application, USER1_NAME);

        assertEquals(principal1, returnedPrincipal);
    }

    @Test
    public void testFindUserByName_InactiveDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(inactiveDirectoryMapping, directoryMapping1, directoryMapping2));
        // principal does not exist in any of the active directories
        when(directoryManager.findUserByName(anyLong(), eq(INACTIVE_USER_NAME))).thenThrow(new UserNotFoundException(USER1_NAME));

        try
        {
            applicationService.findUserByName(application, INACTIVE_USER_NAME);
            fail("User is in an inactive directory.  Should have thrown a UserNotFoundException");
        }
        catch (UserNotFoundException e)
        {
            // correct behaviour
        }
        verify(directoryManager, never()).findUserByName(INACTIVE_DIRECTORY_ID, INACTIVE_USER_NAME);
    }

    @Test(expected = OperationFailedException.class)
    public void testRemoveUserAttribute_WithErrorInUnderlyingDirectory() throws Exception
    {
        final String ATTRIB_NAME = "attrib";
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);

        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_USER_ATTRIBUTE)).thenReturn(true);
        doThrow(new OperationFailedException()).when(directoryManager).removeUserAttributes(DIRECTORY1_ID, USER1_NAME, ATTRIB_NAME);

        applicationService.removeUserAttributes(application, USER1_NAME, ATTRIB_NAME);
    }

    @Test(expected = ApplicationPermissionException.class)
    public void testRemoveUserAttribute_WithNoApplicationPermissions() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_USER_ATTRIBUTE)).thenReturn(false);

        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);
        applicationService.removeUserAttributes(application, USER1_NAME, "attrib");
    }

    @Test(expected = UserNotFoundException.class)
    public void testRemoveUserAttribute_WherePrincipalDoesNotExist() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenThrow(new UserNotFoundException(USER1_NAME));

        applicationService.removeUserAttributes(application, USER1_NAME, "attrib");
    }

    @Test
    public void testRemoveUserAttribute() throws Exception
    {
        final String ATTRIB_NAME = "attrib";
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_USER_ATTRIBUTE)).thenReturn(true);
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);

        // Make the call
        applicationService.removeUserAttributes(application, USER1_NAME, ATTRIB_NAME);
        verify(directoryManager).removeUserAttributes(DIRECTORY1_ID, USER1_NAME, ATTRIB_NAME);
    }

    @Test
    public void testRemoveUserAttribute_InactiveDirectory() throws Exception
    {
        final String ATTRIB_NAME = "attrib";
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(inactiveDirectoryMapping, directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, INACTIVE_USER_NAME)).thenThrow(new UserNotFoundException(USER1_NAME));

        // Make the call
        try
        {
            applicationService.removeUserAttributes(application, INACTIVE_USER_NAME, ATTRIB_NAME);            
            fail("Remove an inactive directory user's attributes.  Should have thrown a UserNotFoundException");
        }
        catch (UserNotFoundException e)
        {
            // correct behaviour
        }
        verify(permissionManager, never()).hasPermission(application, inactiveDirectory, OperationType.UPDATE_USER_ATTRIBUTE);
        verify(directoryManager, never()).findDirectoryById(INACTIVE_DIRECTORY_ID);
        verify(directoryManager, never()).findUserByName(INACTIVE_DIRECTORY_ID, INACTIVE_USER_NAME);
        verify(directoryManager, never()).removeUserAttributes(INACTIVE_DIRECTORY_ID, INACTIVE_USER_NAME, ATTRIB_NAME);
    }

    /**
     * Tests that an error while removing bubbles up to the top
     */
    @Test(expected = OperationFailedException.class)
    public void testRemoveUser_WithErrorInUnderlyingDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        // Setup expectations
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);
        when(permissionManager.hasPermission(application, directory1, OperationType.DELETE_USER)).thenReturn(true);
        doThrow(new OperationFailedException()).when(directoryManager).removeUser(DIRECTORY1_ID, USER1_NAME);

        applicationService.removeUser(application, USER1_NAME);
    }

    /**
     * tests trying to remove a principal where none of directories have permissions to remove
     */
    @Test(expected = ApplicationPermissionException.class)
    public void testRemoveUser_WithNoApplicationPermissions() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        // Setup expectations
        when(permissionManager.hasPermission(application, directory1, OperationType.DELETE_USER)).thenReturn(false);
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);

        applicationService.removeUser(application, USER1_NAME);
    }

    /**
     * tests trying to remove someone that doesn't exist
     */
    @Test(expected = UserNotFoundException.class)
    public void testRemoveUser_WherePrincipalDoesNotExist() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenThrow(new UserNotFoundException(USER1_NAME));

        applicationService.removeUser(application, USER1_NAME);
    }

    /**
     * Tests removing a principal, skipping directories without perms, skipping directories without user
     */
    @Test
    public void testRemoveUser() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // Setup expectations
        when(permissionManager.hasPermission(application, directory1, OperationType.DELETE_USER)).thenReturn(true);
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);

        // Note that we only attempt to remove the user from one directory

        // Make the call
        applicationService.removeUser(application, principal1.getName());
        verify(directoryManager).removeUser(DIRECTORY1_ID, USER1_NAME);
    }

    @Test
    public void testRemoveUser_InactiveDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(inactiveDirectoryMapping, directoryMapping1, directoryMapping2));
        // Setup expectations
        when(directoryManager.findUserByName(anyLong(), eq(INACTIVE_USER_NAME))).thenThrow(new UserNotFoundException(USER1_NAME));


        // Make the call
        try
        {
            applicationService.removeUser(application, INACTIVE_USER_NAME);
            fail("Removing an inactive directory user.  Should have thrown a UserNotFoundException.");
        }
        catch (UserNotFoundException e)
        {
            // correct behaviour
        }
        verify(directoryManager, never()).findUserByName(INACTIVE_DIRECTORY_ID, INACTIVE_USER_NAME);
        verify(directoryManager, never()).findDirectoryById(INACTIVE_DIRECTORY_ID);
        // removeUser should never be called since we are trying to remove a user in an inactive directory
        verify(directoryManager, never()).removeUser(INACTIVE_DIRECTORY_ID, INACTIVE_USER_NAME);
    }

    @Test(expected = OperationFailedException.class)
    public void testResetUserCredential_WithErrorInUnderlyingDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_USER)).thenReturn(true);
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        doThrow(new OperationFailedException()).when(directoryManager).updateUserCredential(DIRECTORY1_ID, USER1_NAME, credential);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);

        applicationService.updateUserCredential(application, USER1_NAME, credential);
    }

    @Test(expected = ApplicationPermissionException.class)
    public void testResetUserCredential_WithNoApplicationPermissions() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_USER)).thenReturn(false);
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);

        applicationService.updateUserCredential(application, USER1_NAME, credential);
    }

    @Test(expected = UserNotFoundException.class)
    public void testResetUserCredential_WherePrincipalDoesNotExist() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenThrow(new UserNotFoundException(USER1_NAME));

        applicationService.updateUserCredential(application, USER1_NAME, new PasswordCredential(""));
    }

    @Test
    public void testResetUserCredential() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_USER)).thenReturn(true);
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);

        // Make the call
        applicationService.resetUserCredential(application, USER1_NAME);
        verify(directoryManager).resetPassword(DIRECTORY1_ID, USER1_NAME);
    }

    @Test
    public void testResetUserCredential_InactiveDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(inactiveDirectoryMapping));

        // Make the call
        try
        {
            applicationService.resetUserCredential(application, inactivePrincipal.getName());
            fail("Resetting an inactive directory user's credentials.  Should have thrown a UserNotFoundException");
        }
        catch (UserNotFoundException e)
        {
            // correct behaviour
        }
        verify(permissionManager, never()).hasPermission(any(Application.class), any(Directory.class), any(OperationType.class));
        verify(directoryManager, never()).findUserByName(anyLong(), anyString());
        verify(directoryManager, never()).findDirectoryById(anyLong());
        verify(directoryManager, never()).resetPassword(anyLong(), anyString());
    }

    @Test
    public void testSearchUsers_WithErrorInUnderlyingDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // directories will blow up with a OperationFailedException, but this should be ignored and subsequent directories searched as well
        when(directoryManager.searchUsers(anyLong(), any(EntityQuery.class))).thenThrow(new OperationFailedException());

        applicationService.searchUsers(application, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(50));
        verify(directoryManager, times(2)).searchUsers(anyLong(), any(EntityQuery.class));
    }

    @Test
    public void testSearchUsers_indexOutOfBounds() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // search for principals: return indexes 50 to 99

        when(directoryManager.searchUsers(eq(DIRECTORY1_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(principal1));
        when(directoryManager.searchUsers(eq(DIRECTORY2_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(principal2));

        List<User> principals = applicationService.searchUsers(application, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).startingAt(50).returningAtMost(50));

        assertNotNull(principals);
        assertEquals(0, principals.size());
    }

    @Test
    public void testSearchUsers() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        // Create two extra principals to return
        User principal4 = mock(User.class);
        when(principal4.getName()).thenReturn("Z Principal 4");
        when(principal4.getDirectoryId()).thenReturn(DIRECTORY1_ID);

        User principal5 = mock(User.class);
        when(principal5.getName()).thenReturn("A Principal 5");
        when(principal5.getDirectoryId()).thenReturn(DIRECTORY2_ID);

        when(directoryManager.searchUsers(eq(DIRECTORY1_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(principal1, principal4));
        when(directoryManager.searchUsers(eq(DIRECTORY2_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(principal2, principal5));

        Collection principals = applicationService.searchUsers(application, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(50));

        assertNotNull(principals);
        assertEquals(4, principals.size());

        Object[] principalsArray = principals.toArray();
        User foundPrincipal1 = (User) principalsArray[0];
        User foundPrincipal2 = (User) principalsArray[1];
        User foundPrincipal3 = (User) principalsArray[2];
        User foundPrincipal4 = (User) principalsArray[3];

        // check ordering
        assertEquals("First user should have been principal4", principal5, foundPrincipal1);
        assertEquals("Second user should have been principal1", principal1, foundPrincipal2);
        assertEquals("Third user should have been principal2", principal2, foundPrincipal3);
        assertEquals("Fourth user should have been principal3", principal4, foundPrincipal4);
    }

    /**
     * Test that search returns requested amount of results in the order they are found.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSearchGroups_Constraints() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchUsers(eq(DIRECTORY1_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(USER2_NAME));
        when(directoryManager.searchUsers(eq(DIRECTORY2_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(USER1_NAME));

        List<String> users = applicationService.searchUsers(application, QueryBuilder.queryFor(String.class, EntityDescriptor.user()).returningAtMost(1));

        assertEquals(1, users.size());
        assertEquals(USER2_NAME, users.get(0));
    }

    @Test
    public void testSearchUsers_InactiveDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(inactiveDirectoryMapping));

        Collection principals = applicationService.searchUsers(application, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(10));
        assert(principals.isEmpty());
        verify(directoryManager, never()).searchUsers(anyLong(), any(EntityQuery.class));
    }


    @Test(expected = OperationFailedException.class)
    public void testUpdateUserAttribute_WithErrorInUnderlyingDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_USER_ATTRIBUTE)).thenReturn(true);

        doThrow(new OperationFailedException()).when(directoryManager).storeUserAttributes(eq(DIRECTORY1_ID), eq(USER1_NAME), any(Map.class));

        applicationService.storeUserAttributes(application, principal1.getName(), new HashMap<String, Set<String>>());
    }

    @Test(expected = ApplicationPermissionException.class)
    public void testUpdateUserAttribute_WithNoApplicationPermissions() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_USER_ATTRIBUTE)).thenReturn(false);
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);

        applicationService.storeUserAttributes(application, principal1.getName(), new HashMap<String, Set<String>>());
    }

    @Test(expected = UserNotFoundException.class)
    public void testUpdateUserAttribute_WherePrincipalDoesNotExist() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenThrow(new UserNotFoundException(USER1_NAME));

        applicationService.storeUserAttributes(application, USER1_NAME, new HashMap<String, Set<String>>());
    }

    @Test
    public void testUpdateUserAttribute() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_USER_ATTRIBUTE)).thenReturn(true);
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);

        // make the call
        applicationService.storeUserAttributes(application, USER1_NAME, new HashMap<String, Set<String>>());
        verify(directoryManager).storeUserAttributes(eq(DIRECTORY1_ID), anyString(), any(Map.class));
    }

    @Test(expected = OperationFailedException.class)
    public void testUpdateUserCredential_WithErrorInUnderlyingDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_USER)).thenReturn(true);
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);
        doThrow(new OperationFailedException()).when(directoryManager).updateUserCredential(DIRECTORY1_ID, USER1_NAME, credential);
        applicationService.updateUserCredential(application, USER1_NAME, credential);
    }

    @Test(expected = ApplicationPermissionException.class)
    public void testUpdateUserCredential_WithNoApplicationPermissions() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_USER)).thenReturn(false);
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);

        applicationService.updateUserCredential(application, USER1_NAME, credential);
    }

    @Test(expected = UserNotFoundException.class)
    public void testUpdateUserCredential_WherePrincipalDoesNotExist() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenThrow(new UserNotFoundException(USER1_NAME));

        applicationService.updateUserCredential(application, USER1_NAME, credential);
    }

    @Test
    public void testUpdateUserCredential() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(permissionManager.hasPermission(application, directory1, OperationType.UPDATE_USER)).thenReturn(true);
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(directoryManager.findDirectoryById(DIRECTORY1_ID)).thenReturn(directory1);

        // Perform call
        applicationService.updateUserCredential(application, principal1.getName(), credential);
        verify(directoryManager).updateUserCredential(DIRECTORY1_ID, USER1_NAME, credential);
    }

    /**
     * Tests {@link ApplicationService#searchUsersAllowingDuplicateNames(com.atlassian.crowd.model.application.Application, com.atlassian.crowd.search.query.entity.EntityQuery)}.
     * Duplicate user names in multiple directories should return only unique user names.
     */
    @Test
    public void testSearchUsers_DuplicateUserNameInMultipleDirectories() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchUsers(eq(DIRECTORY1_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(principal1));
        when(directoryManager.searchUsers(eq(DIRECTORY2_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(duplicatePrincipal1Username));

        List<User> users = applicationService.searchUsers(application, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(50));
        assertEquals(1, users.size());
        for (User user : users)
        {
            assertEquals(USER1_NAME, user.getName());
        }
    }

    /**
     * Tests {@link ApplicationService#searchUsersAllowingDuplicateNames(com.atlassian.crowd.model.application.Application, com.atlassian.crowd.search.query.entity.EntityQuery)}.
     * Duplicate user names in multiple directories should return all the users.
     */
    @Test
    public void testSearchUsersAllowingDuplicateNames_DuplicateUserNameInMultipleDirectories() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchUsers(eq(DIRECTORY1_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(principal1));
        when(directoryManager.searchUsers(eq(DIRECTORY2_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(duplicatePrincipal1Username));

        List<User> users = applicationService.searchUsersAllowingDuplicateNames(application, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(50));
        assertEquals(2, users.size());
        for (User user : users)
        {
            assertEquals(USER1_NAME, user.getName());
        }
    }

    /**
     * Tests {@link ApplicationService#searchUsersAllowingDuplicateNames(com.atlassian.crowd.model.application.Application, com.atlassian.crowd.search.query.entity.EntityQuery)}.
     * Duplicate user names in multiple directories and not just the first directory, should return all the users.
     */
    @Test
    public void testSearchUsersAllowingDuplicates_DuplicateUserNameInMultipleDirectories_NotFirstDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2, directoryMapping3));
        when(directoryManager.searchUsers(eq(DIRECTORY1_ID), any(EntityQuery.class))).thenReturn(Collections.emptyList());
        when(directoryManager.searchUsers(eq(DIRECTORY2_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(principal2));
        when(directoryManager.searchUsers(eq(DIRECTORY3_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(duplicatePrincipal2Username));

        List<User> users = applicationService.searchUsersAllowingDuplicateNames(application, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(50));
        assertEquals(2, users.size());
        for (User user : users)
        {
            assertEquals(USER2_NAME, user.getName());
        }
    }

    /**
     * Tests {@link ApplicationService#searchUsers} with duplicate usernames.  Checks that duplicate usernames are handled
     * correctly and the correct number of users is returned.
     */
    @Test
    public void testSearchUsernames_WithDuplicateUsernames() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchUsers(eq(DIRECTORY1_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(USER1_NAME, USER2_NAME));
        when(directoryManager.searchUsers(eq(DIRECTORY2_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(USER1_NAME, USER2_NAME, USER3_NAME));
        when(directoryManager.searchUsers(DIRECTORY2_ID, QueryBuilder.queryFor(String.class, EntityDescriptor.user()).returningAtMost(2))).thenReturn(Arrays.asList(USER1_NAME, USER2_NAME));
        when(directoryManager.searchUsers(DIRECTORY2_ID, QueryBuilder.queryFor(String.class, EntityDescriptor.user()).returningAtMost(1))).thenReturn(Arrays.asList(USER1_NAME));

        List<String> expectedUsers = Arrays.asList(USER1_NAME, USER2_NAME, USER3_NAME);
        List<String> users = applicationService.searchUsers(application, QueryBuilder.queryFor(String.class, EntityDescriptor.user()).returningAtMost(expectedUsers.size()));
        assertEquals(expectedUsers.size(), users.size());
        assertTrue(users.containsAll(expectedUsers));
    }

    /**
     * Tests {@link ApplicationService#searchUsers} with duplicate usernames.  Checks that duplicate usernames are
     * handled correctly and the correct number of users is returned. 
     */
    @Test
    public void testSearchUsers_WithDuplicateUsernames() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchUsers(eq(DIRECTORY1_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(principal1, principal2));
        when(directoryManager.searchUsers(eq(DIRECTORY2_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(duplicatePrincipal1Username, duplicatePrincipal2Username, principal3));
        when(directoryManager.searchUsers(DIRECTORY2_ID, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(2))).thenReturn(Arrays.asList(duplicatePrincipal1Username, duplicatePrincipal2Username));
        when(directoryManager.searchUsers(DIRECTORY2_ID, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(1))).thenReturn(Arrays.asList(duplicatePrincipal1Username));

        List<User> expectedUsers = Arrays.asList(principal1, principal2, principal3);
        List<User> users = applicationService.searchUsers(application, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(expectedUsers.size()));
        assertEquals(expectedUsers.size(), users.size());
        for (int i = 0, n = users.size(); i < n; i++)
        {
            assertTrue(UserComparator.equal(expectedUsers.get(i), users.get(i)));
        }
    }

    /**
     * Tests {@link ApplicationService#searchUsersAllowingDuplicateNames(com.atlassian.crowd.model.application.Application, com.atlassian.crowd.search.query.entity.EntityQuery)}
     * with duplicates.  Checks that the number of users returned is correct.
     */
    @Test
    public void testSearchUsersAllowingDuplicateNames_CorrectNumberUsers() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1, directoryMapping2));
        when(directoryManager.searchUsers(eq(DIRECTORY1_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(principal1, principal2));
        when(directoryManager.searchUsers(eq(DIRECTORY2_ID), any(EntityQuery.class))).thenReturn(Arrays.asList(duplicatePrincipal1Username, duplicatePrincipal2Username, principal3));
        when(directoryManager.searchUsers(DIRECTORY2_ID, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(2))).thenReturn(Arrays.asList(duplicatePrincipal1Username, duplicatePrincipal2Username));
        when(directoryManager.searchUsers(DIRECTORY2_ID, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(1))).thenReturn(Arrays.asList(duplicatePrincipal1Username));

        List<User> expectedUsers = Arrays.asList(principal1, duplicatePrincipal1Username, principal2);
        List<User> users = applicationService.searchUsersAllowingDuplicateNames(application, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(3));
        assertEquals(expectedUsers.size(), users.size());
        for (int i = 0, n = users.size(); i < n; i++)
        {
            assertTrue(UserComparator.equal(expectedUsers.get(i), users.get(i)));
        }
    }
}
