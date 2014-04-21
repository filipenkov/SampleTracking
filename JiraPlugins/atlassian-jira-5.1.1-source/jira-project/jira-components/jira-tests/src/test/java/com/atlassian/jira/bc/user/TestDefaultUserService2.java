package com.atlassian.jira.bc.user;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Iterator;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Clean tests for DefaultUserService.
 *
 * @see TestDefaultUserService
 *
 * @since v5.0
 */
@RunWith (ListeningMockitoRunner.class)
public class TestDefaultUserService2 extends TestCase
{
    @Mock
    UserUtil userUtil;

    private I18nHelper.BeanFactory i18nFactory = new MockI18nBean.MockI18nBeanFactory();

    @Override
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void testValidateDeleteUserNoPermission() throws Exception
    {
        DefaultUserService userService = new DefaultUserService(null, new MockPermissionManager(), null, null, i18nFactory, null);
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(null, "fred");
        assertEquals("You do not have the permission to remove users.", validationResult.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateDeleteUserNull() throws Exception
    {
        DefaultUserService userService = new DefaultUserService(null, new MockPermissionManager(true), null, null, i18nFactory, null);
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(null, null);
        assertTrue(validationResult.getErrorCollection().hasAnyErrors());
        assertEquals("Username for delete can not be null or empty.", validationResult.getErrorCollection().getErrors().get("username"));
    }

    @Test
    public void testValidateDeleteUserEmpty() throws Exception
    {
        DefaultUserService userService = new DefaultUserService(null, new MockPermissionManager(true), null, null, i18nFactory, null);
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(null, "");
        assertTrue(validationResult.getErrorCollection().hasAnyErrors());
        assertEquals("Username for delete can not be null or empty.", validationResult.getErrorCollection().getErrors().get("username"));
    }

    @Test
    public void testValidateDeleteUserDeleteSelf() throws Exception
    {
        DefaultUserService userService = new DefaultUserService(null, new MockPermissionManager(true), null, null, i18nFactory, null);
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(new MockUser("fred"), "fred");
        assertTrue(validationResult.getErrorCollection().hasAnyErrors());
        assertEquals("You cannot delete the currently logged in user.", validationResult.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateDeleteUserNotExist() throws Exception
    {
        DefaultUserService userService = new DefaultUserService(null, new MockPermissionManager(true), new MockUserManager(), null, i18nFactory, null);
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(new MockUser("dude"), "fred");
        assertTrue(validationResult.getErrorCollection().hasAnyErrors());
        assertEquals("This user does not exist please select a user from the user browser.", validationResult.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateDeleteUserReadOnly() throws Exception
    {
        final MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("fred"));
        userManager.setWritableDirectory(false);
        DefaultUserService userService = new DefaultUserService(null, new MockPermissionManager(true), userManager, null, i18nFactory, null);

        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(new MockUser("dude"), "fred");
        assertTrue(validationResult.getErrorCollection().hasAnyErrors());
        assertEquals("Cannot delete user, the user directory is read-only.", validationResult.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateDeleteUserForeignKeys() throws Exception
    {
        final MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("fred"));
        DefaultUserService userService = new DefaultUserService(userUtil, new MockPermissionManager(true), userManager, null, i18nFactory, null);
        when(userUtil.getNumberOfReportedIssuesIgnoreSecurity(new MockUser("admin"), new MockUser("fred"))).thenReturn(387L);
        when(userUtil.getNumberOfAssignedIssuesIgnoreSecurity(new MockUser("admin"), new MockUser("fred"))).thenReturn(26L);
        when(userUtil.getProjectsLeadBy(new MockUser("fred"))).thenReturn(Collections.<Project>singleton(new MockProject(10003L)));

        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(new MockUser("admin"), "fred");
        assertTrue(validationResult.getErrorCollection().hasAnyErrors());
        final Iterator<String> iterator = validationResult.getErrorCollection().getErrorMessages().iterator();
        assertEquals("Cannot delete user 'fred' because 387 issues were reported by this person.", iterator.next());
        assertEquals("Cannot delete user 'fred' because 26 issues are currently assigned to this person.", iterator.next());
        assertEquals("Cannot delete user 'fred' because they are currently the project lead on 1 projects.", iterator.next());
    }

    @Test
    public void testValidateDeleteUserNonSysAdminAttemptingToDeleteSysAdmin() throws Exception
    {
        final MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("fred"));
        DefaultUserService userService = new DefaultUserService(userUtil, new MockPermissionManager(true), userManager, null, i18nFactory, null);
        when(userUtil.isNonSysAdminAttemptingToDeleteSysAdmin(new MockUser("admin"), new MockUser("fred"))).thenReturn(true);

        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(new MockUser("admin"), "fred");
        assertTrue(validationResult.getErrorCollection().hasAnyErrors());
        assertEquals("As a user with JIRA Administrators permission, you cannot delete users with JIRA System Administrators permission.", validationResult.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateDeleteUserHappyHappyJoyJoy() throws Exception
    {
        final MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("fred"));
        DefaultUserService userService = new DefaultUserService(userUtil, new MockPermissionManager(true), userManager, null, i18nFactory, null);

        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(new MockUser("admin"), "fred");
        assertFalse(validationResult.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testRemoveUserNullResult()
    {
        final UserService userService = new DefaultUserService(userUtil, new MockPermissionManager(true), null, null, null, null);

        try
        {
            userService.removeUser(new MockUser("admin"), null);
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
            // ok
        }
    }

    @Test
    public void testRemoveUserResultWithError()
    {
        final UserService userService = new DefaultUserService(userUtil, new MockPermissionManager(true), null, null, null, null);

        final ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("error");
        try
        {
            final UserService.DeleteUserValidationResult result = new UserService.DeleteUserValidationResult(errors);
            userService.removeUser(new MockUser("admin"), result);
            fail();
        }
        catch (final IllegalStateException e)
        {
            //ok
        }
    }

    @Test
    public void testRemoveUser() throws Exception
    {
        final MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("fred"));
        DefaultUserService userService = new DefaultUserService(userUtil, new MockPermissionManager(true), userManager, null, i18nFactory, null);

        final MockUser loggedInUser = new MockUser("admin");
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(loggedInUser, "fred");
        assertFalse(validationResult.getErrorCollection().hasAnyErrors());
        
        userService.removeUser(loggedInUser, validationResult);

        verify(userUtil).removeUser(loggedInUser, new MockUser("fred"));
    }
}
