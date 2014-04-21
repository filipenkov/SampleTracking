package com.atlassian.jira.bc.user;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.core.user.GroupUtils;
import com.atlassian.core.user.UserUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.core.util.StaticCrowdServiceFactory;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.user.util.UserUtilImpl;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.query.Query;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.EntityNotFoundException;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.easymock.EasyMock.expect;

/**
 * Unit test for UserUtilImpl
 */
public class TestDefaultUserService extends LegacyJiraMockTestCase
{
    private Group testGroup;
    private User testUser;
    private AtomicInteger clearActiveUsersCallCount;
    private SearchRequestService searchRequestService;
    private PortalPageService portalPageService;
    private JiraServiceContextImpl ctx;
    private MockController mockController;
    private PermissionManager permissionMananger;
    private ApplicationProperties applicationProperties;
    private UserManager userManager;
    private User user;
    private SearchProvider searchProvider;
    private ProjectManager projectManager;
    private ProjectRoleService projectRoleService;
    private ProjectComponentManager projectComponentManager;
    private SubscriptionManager subscriptionManager;
    private NotificationSchemeManager notificationSchemeManager;
    private GlobalPermissionManager globalPermissionManager;
    private User createdUser;
    private UserUtil userUtil;
    private UserHistoryManager userHistoryManager;
    private JiraLicenseService jiraLicenseService;
    private ComponentLocator componentLocator;

    public TestDefaultUserService(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mockController = new MockController();
        clearActiveUsersCallCount = new AtomicInteger();
        final Mock mockIssueSecurityLevelManager = new Mock(IssueSecurityLevelManager.class);

        globalPermissionManager = mockController.getMock(GlobalPermissionManager.class);
        searchRequestService = mockController.getMock(SearchRequestService.class);
        portalPageService = mockController.getMock(PortalPageService.class);
        permissionMananger = mockController.getMock(PermissionManager.class);
        applicationProperties = mockController.getMock(ApplicationProperties.class);
        userManager = mockController.getMock(UserManager.class);

        searchProvider = mockController.getMock(SearchProvider.class);
        projectManager = mockController.getMock(ProjectManager.class);
        projectRoleService = mockController.getMock(ProjectRoleService.class);
        projectComponentManager = mockController.getMock(ProjectComponentManager.class);
        subscriptionManager = mockController.getMock(SubscriptionManager.class);
        notificationSchemeManager = mockController.getMock(NotificationSchemeManager.class);
        permissionMananger = mockController.getMock(PermissionManager.class);
        userHistoryManager = mockController.getMock(UserHistoryManager.class);
        componentLocator = mockController.getMock(ComponentLocator.class);

        ctx = new JiraServiceContextImpl(testUser);

        jiraLicenseService = mockController.getMock(JiraLicenseService.class);
        final LicenseDetails licenseDetails = mockController.getMock(LicenseDetails.class);
        expect(jiraLicenseService.getLicense()).andStubReturn(licenseDetails);
        expect(licenseDetails.isLicenseSet()).andStubReturn(true);
        expect(licenseDetails.isUnlimitedNumberOfUsers()).andStubReturn(true);

        userUtil = new UserUtilImpl(componentLocator, (IssueSecurityLevelManager) mockIssueSecurityLevelManager.proxy(), globalPermissionManager, StaticCrowdServiceFactory.getCrowdService(), null, permissionMananger, applicationProperties, searchProvider, projectManager, projectRoleService, projectComponentManager,
            subscriptionManager, notificationSchemeManager, userHistoryManager, userManager, null)
        {
            @Override
            public synchronized void clearActiveUserCount()
            {
                clearActiveUsersCallCount.getAndIncrement();
            }

            @Override
            protected SearchRequestService getSearchRequestService()
            {
                return searchRequestService;
            }

            @Override
            protected PortalPageService getPortalPageService()
            {
                return portalPageService;
            }

            @Override
            protected JiraServiceContext getServiceContext(final User user)
            {
                return ctx;
            }
        };

        testGroup = GroupUtils.getGroupSafely("myTestGroup");
        testUser = UtilsForTests.getTestUser("logged-in-user");

        user = OSUserConverter.convertToOSUser(new MockUser("admin"));
        ComponentAccessor.getCrowdService().addUser(user, "admin");
        createdUser = OSUserConverter.convertToOSUser(new MockUser("fflintstone"));
    }

    public void testCreateJiraUserWithPassword() throws PermissionException, CreateException
    {
        final User fred = createUser("fflintstone", "mypassword", "fred@flintstone.com", "Fred Flintstone");
        
//        assertTrue(fred.authenticate("mypassword"));
        final Set groups = userUtil.getGroupNamesForUser(fred.getName());
        assertEquals(1, groups.size());
        assertTrue(groups.contains(testGroup.getName()));
    }

    public void testCreateJiraUserWithNullPassword() throws PermissionException, CreateException
    {
        //lets also try creating a user with a null pasword.  We don't use it anywhere in JIRA.  This should
        //simply generate a random password.
        final User wilma = createUser("wflintstone", null, "wilma@flintstone.com", "Wilma Flintstone");

        //bit of a silly test...lets hope the random password generated is not 'mypassword'
//        assertFalse(wilma.authenticate("mypassword"));
    }

    private User createUser(final String username, final String password, final String emailAddress, final String displayName)
            throws CreateException, PermissionException
    {
        final long directoryId = 1L;
        final User expectedUser = new ImmutableUser(directoryId, username, displayName, emailAddress, true);
        final DirectoryImpl directory = new DirectoryImpl();
        directory.addAllowedOperation(OperationType.CREATE_USER);

        _prepareMocks(2);
        expect(componentLocator.getComponentInstanceOfType(JiraLicenseService.class)).andStubReturn(jiraLicenseService);
        expect(userManager.getDirectory(directoryId)).andStubReturn(directory);
        expect(userManager.canUpdateUserPassword(expectedUser)).andReturn(true);
        mockController.replay();

        final UserService userService = new DefaultUserService(userUtil, permissionMananger, null, null, null);

        //first check the user doesn't exist.
        User fred = userUtil.getUser(username);
        assertNull(fred);

        UserService.CreateUserValidationResult validationResult = new UserService.CreateUserValidationResult(username, password, emailAddress, displayName);

        userService.createUserWithNotification(validationResult);
        fred = userUtil.getUser(username);

        assertEquals(username, fred.getName());
        assertEquals(displayName, fred.getDisplayName());
        assertEquals(emailAddress, fred.getEmailAddress());
        
        return fred;
    }

    private void _prepareMocks(int userCount) {
        for (int i=0;i<userCount;i++)
        {
            globalPermissionManager.getGroups(Permissions.USE);
            mockController.setReturnValue(EasyList.build(testGroup));
            globalPermissionManager.getGroups(Permissions.ADMINISTER);
            mockController.setReturnValue(EasyList.build());
            globalPermissionManager.getGroups(Permissions.SYSTEM_ADMIN);
            mockController.setReturnValue(EasyList.build());
        }
     }

    public void testCreateUserNullResult() throws PermissionException, CreateException
    {
        final UserService userService = new DefaultUserService(null, null, null, null, null);
        try
        {
            userService.createUserWithNotification(null);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("You can not create a user, validation result should not be null!", e.getMessage());

        }
    }

    public void testCreateUserInvalidResult() throws PermissionException, CreateException
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("Something went wrong");
        final UserService.CreateUserValidationResult result = new UserService.CreateUserValidationResult(errors);

        final UserService userService = new DefaultUserService(userUtil, permissionMananger, null, null, null);
        try
        {
            userService.createUserWithNotification(result);
            fail();
        }
        catch (final IllegalStateException e)
        {
            assertEquals("You can not create a user with an invalid validation result.", e.getMessage());

        }
    }

    public void testValidateCreateJiraUserForAdminPasswordRequired()
    {
        final UserService userService = new DefaultUserService(userUtil, permissionMananger, applicationProperties, null, null);

        permissionMananger.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        mockController.setReturnValue(false);
        mockController.replay();

        final UserService.CreateUserValidationResult result = userService.validateCreateUserForAdminPasswordRequired(user, "fflintstone",
            "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone");

        assertTrue(result.isValid());
        assertFalse(result.getErrorCollection().hasAnyErrors());
        assertEquals("fflintstone", result.getUsername());
        assertEquals("mypassword", result.getPassword());
        assertEquals("Fred Flintstone", result.getFullname());
        assertEquals("fred@flintstone.com", result.getEmail());

        mockController.verify();
    }

    public void testValidateCreateJiraUserForAdminNoPermission()
    {
        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);

        final UserService userService = new DefaultUserService(userUtil, permissionMananger, applicationProperties, null, null)
        {

            @Override
            I18nHelper getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        permissionMananger.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(false);
        mockI18nBean.getText("admin.errors.user.no.permission.to.create");
        mockController.setReturnValue("error message");
        mockController.replay();

        final UserService.CreateUserValidationResult result = userService.validateCreateUserForAdminPasswordRequired(user, "fflintstone",
            "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone");

        assertFalse(result.isValid());
        assertTrue(result.getErrorCollection().hasAnyErrors());
        assertEquals("error message", result.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
    }

    public void testValidateCreateJiraUserForAdminExternalAdmin()
    {
        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);
        final Mock mockJiraContactHelper = new Mock(JiraContactHelper.class);
        mockJiraContactHelper.expectAndReturn("getAdministratorContactMessage", P.ANY_ARGS, "please contact your JIRA administrators");

        final UserService userService = new DefaultUserService(userUtil, permissionMananger, applicationProperties, null, (JiraContactHelper) mockJiraContactHelper.proxy())
        {

            @Override
            I18nHelper getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        permissionMananger.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        mockController.setReturnValue(true);
        mockI18nBean.getText("admin.errors.cannot.add.user.external.managment", "please contact your JIRA administrators");
        mockController.setReturnValue("error message");
        mockController.replay();

        final UserService.CreateUserValidationResult result = userService.validateCreateUserForAdminPasswordRequired(user, "fflintstone",
            "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone");

        assertFalse(result.isValid());
        assertTrue(result.getErrorCollection().hasAnyErrors());
        assertEquals("error message", result.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
    }

    public void testValidateCreateJiraUserForSetupOk()
    {
        final UserService userService = new DefaultUserService(userUtil, permissionMananger, applicationProperties, null, null);

        applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        mockController.setReturnValue(false);
        mockController.replay();

        final UserService.CreateUserValidationResult result = userService.validateCreateUserForSignupOrSetup(user, "fflintstone", "mypassword",
            "mypassword", "fred@flintstone.com", "Fred Flintstone");

        assertTrue(result.isValid());
        assertFalse(result.getErrorCollection().hasAnyErrors());

        mockController.verify();
    }

    public void testValidateCreateJiraUserForSetupNoExtMgmnt()
    {
        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);
        final Mock mockJiraContactHelper = new Mock(JiraContactHelper.class);
        mockJiraContactHelper.expectAndReturn("getAdministratorContactMessage", P.ANY_ARGS, "please contact your JIRA administrators");

        final UserService userService = new DefaultUserService(userUtil, permissionMananger, applicationProperties, null, (JiraContactHelper) mockJiraContactHelper.proxy())
        {

            @Override
            I18nHelper getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        mockController.setReturnValue(true);
        mockI18nBean.getText("admin.errors.cannot.add.user.external.managment", "please contact your JIRA administrators");
        mockController.setReturnValue("error message");
        mockController.replay();

        final UserService.CreateUserValidationResult result = userService.validateCreateUserForSignupOrSetup(user, "fflintstone", "mypassword",
            "mypassword", "fred@flintstone.com", "Fred Flintstone");

        assertFalse(result.isValid());
        assertTrue(result.getErrorCollection().hasAnyErrors());
        assertEquals("error message", result.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
    }

    public void testValidateCreateJiraUserForSetupNoPassword()
    {
        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);

        final UserService userService = new DefaultUserService(userUtil, permissionMananger, applicationProperties, null, null)
        {

            @Override
            I18nHelper getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        mockController.setReturnValue(false);
        mockI18nBean.getText("signup.error.password.required");
        mockController.setReturnValue("error message");
        mockController.replay();

        final UserService.CreateUserValidationResult result = userService.validateCreateUserForSignupOrSetup(user, "fflintstone", "", "",
            "fred@flintstone.com", "Fred Flintstone");

        assertFalse(result.isValid());
        assertTrue(result.getErrorCollection().hasAnyErrors());
        assertEquals("error message", result.getErrorCollection().getErrors().get("password"));

        mockController.verify();
    }

    public void testValidateCreateJiraUserForAdmin()
    {
        final UserService userService = new DefaultUserService(userUtil, permissionMananger, applicationProperties, userManager, null);

        permissionMananger.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        mockController.setReturnValue(false);
        userManager.getWritableDirectories();
        mockController.setReturnValue(Collections.singletonList(new DirectoryImpl()));
        mockController.replay();

        final UserService.CreateUserValidationResult result = userService.validateCreateUserForAdmin(user, "fflintstone", null, null,
            "fred@flintstone.com", "Fred Flintstone");

        assertTrue(result.isValid());
        assertFalse(result.getErrorCollection().hasAnyErrors());

        mockController.verify();
    }

    public void testValidateCreateJiraUserForAdmin_nameDirectory()
    {
        final UserService userService = new DefaultUserService(userUtil, permissionMananger, applicationProperties, userManager, null);

        permissionMananger.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        mockController.setReturnValue(false);
        userManager.findUserInDirectory("fflintstone", 1L);
        mockController.setReturnValue(null);
        userManager.getDirectory(1L);
        final DirectoryImpl directory = new DirectoryImpl();
        directory.addAllowedOperation(OperationType.CREATE_USER);
        mockController.setReturnValue(directory);
        mockController.replay();

        final UserService.CreateUserValidationResult result = userService.validateCreateUserForAdmin(user, "fflintstone", null, null,
            "fred@flintstone.com", "Fred Flintstone", 1L);

        assertTrue(result.isValid());
        assertFalse(result.getErrorCollection().hasAnyErrors());

        mockController.verify();
    }

    public void testValidateCreateJiraUserForAdminNoPermissionNoPassword()
    {
        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);

        final UserService userService = new DefaultUserService(userUtil, permissionMananger, applicationProperties, null, null)
        {

            @Override
            I18nHelper getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        permissionMananger.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(false);
        mockI18nBean.getText("admin.errors.user.no.permission.to.create");
        mockController.setReturnValue("error message");
        mockController.replay();

        final UserService.CreateUserValidationResult result = userService.validateCreateUserForAdmin(user, "fflintstone", null, null,
            "fred@flintstone.com", "Fred Flintstone");

        assertFalse(result.isValid());
        assertTrue(result.getErrorCollection().hasAnyErrors());
        assertEquals("error message", result.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
    }

    public void testValidateCreateJiraUserForAdminNoPermissionNoExtMgmnt()
    {
        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);
        final Mock mockJiraContactHelper = new Mock(JiraContactHelper.class);
        mockJiraContactHelper.expectAndReturn("getAdministratorContactMessage", P.ANY_ARGS, "please contact your JIRA administrators");

        final UserService userService = new DefaultUserService(userUtil, permissionMananger, applicationProperties, null, (JiraContactHelper) mockJiraContactHelper.proxy())
        {

            @Override
            I18nHelper getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        permissionMananger.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        mockController.setReturnValue(true);
        mockI18nBean.getText("admin.errors.cannot.add.user.external.managment", "please contact your JIRA administrators");
        mockController.setReturnValue("error message");
        mockController.replay();

        final UserService.CreateUserValidationResult result = userService.validateCreateUserForAdmin(user, "fflintstone", null, null,
            "fred@flintstone.com", "Fred Flintstone");

        assertFalse(result.isValid());
        assertTrue(result.getErrorCollection().hasAnyErrors());
        assertEquals("error message", result.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
    }

    public void testValidateCreateJiraUserPasswordNotMatch()
    {
        checkCreateUserValues("fflintstone", "mypassword", "mypassword2", "fred@flintstone.com", "Fred Flintstone",
            "signup.error.password.mustmatch", "confirm");
    }

    public void testValidateCreateJiraUserNoEmail()
    {
        checkCreateUserValues("fflintstone", "mypassword", "mypassword", null, "Fred Flintstone", "signup.error.email.required", "email");
    }

    public void testValidateCreateJiraUserEmailExceeds255()
    {
        checkCreateUserValues("testuser543", "mypassword", "mypassword", StringUtils.repeat("a", 256), "Fred Flintstone", "signup.error.email.greater.than.max.chars", "email");
    }

    public void testValidateCreateJiraUserWrongEmail()
    {
        checkCreateUserValues("fflintstone", "mypassword", "mypassword", "fred", "Fred Flintstone", "signup.error.email.valid", "email");
    }

    public void testValidateCreateJiraUserNoUsername()
    {
        checkCreateUserValues("", "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone", "signup.error.username.required", "username");
    }

    public void testValidateCreateJiraUserUsernameExceeds255()
    {
        checkCreateUserValues(StringUtils.repeat("a", 256), "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone", "signup.error.username.greater.than.max.chars", "username");
    }

    public void testValidateCreateJiraUserUppercase()
    {
        checkCreateUserValues("FFlintstone", "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone",
            "signup.error.username.allLowercase", "username");
    }

    public void testValidateCreateJiraUserExists()
    {
        checkCreateUserValues("logged-in-user", "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone", "signup.error.username.exists",
            "username");
    }

    public void testValidateCreateJiraUserNoFullname()
    {
        checkCreateUserValues("fflintstone", "mypassword", "mypassword", "fred@flintstone.com", null, "signup.error.fullname.required", "fullname");
    }

    public void testValidateCreateJiraUserFullnameExceeds255()
    {
        checkCreateUserValues("testuser654", "mypassword", "mypassword", "fred@flintstone.com", StringUtils.repeat("a", 256), "signup.error.full.name.greater.than.max.chars", "fullname");
    }

    public void testValidateCreateJiraUserNullPassword()
    {
        checkCreateUserValues("fflintstone", null, null, "fred@flintstone.com", "Fred Flintstone", "signup.error.password.required", "password");
    }

    private void checkCreateUserValues(final String username, final String password, final String confirm, final String email, final String fullname, final String erroriI18n, final String errorField)
    {
        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);

        final UserService userService = new DefaultUserService(userUtil, permissionMananger, applicationProperties, null, null)
        {

            I18nHelper getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        permissionMananger.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        mockController.setReturnValue(false);
        mockI18nBean.getText(erroriI18n);
        mockController.setReturnValue("error message");
        mockController.replay();

        final UserService.CreateUserValidationResult result = userService.validateCreateUserForAdminPasswordRequired(user, username, password,
            confirm, email, fullname);

        assertFalse(result.isValid());
        assertTrue(result.getErrorCollection().hasAnyErrors());
        assertEquals("error message", result.getErrorCollection().getErrors().get(errorField));

        mockController.verify();
    }

    public void testRemoveUserNullResult()
    {
        final UserService userService = new DefaultUserService(userUtil, permissionMananger, null, null, null);

        try
        {
            userService.removeUser(UserUtils.getUser("logged-in-user"), null);
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
            // ok
        }
        catch (final EntityNotFoundException e)
        {
            fail();
        }
    }

    public void testRemoveUserResultWithError()
    {
        final UserService userService = new DefaultUserService(userUtil, permissionMananger, null, null, null);

        final ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("error");
        try
        {
            final UserService.DeleteUserValidationResult result = new UserService.DeleteUserValidationResult(errors);
            userService.removeUser(UserUtils.getUser("logged-in-user"), result);
            fail();
        }
        catch (final IllegalStateException e)
        {
            //ok
        }
        catch (final EntityNotFoundException e)
        {
            fail();
        }
    }

    public void testRemoveUser() throws Exception
    {
        assertNotNull(UserUtils.getUser("logged-in-user"));

        searchRequestService.deleteAllFiltersForUser(ctx, (com.opensymphony.user.User) testUser);
        portalPageService.deleteAllPortalPagesForUser((com.opensymphony.user.User) testUser);
        projectRoleService.removeAllRoleActorsByNameAndType(testUser.getName(), UserRoleActorFactory.TYPE);// .removeAllRoleActorsByNameAndType(testUser, UserRoleActorFactory.TYPE);
        permissionMananger.removeUserPermissions("logged-in-user");
        applicationProperties.getOption("jira.option.watching");
        mockController.setReturnValue(true);
        applicationProperties.getOption("jira.option.voting");
        mockController.setReturnValue(true);
        subscriptionManager.deleteSubscriptionsForUser((com.opensymphony.user.User) testUser);
        notificationSchemeManager.removeEntities("Single_User", "logged-in-user");
        mockController.setReturnValue(true);
        projectComponentManager.findComponentsByLead("logged-in-user");
        mockController.setReturnValue(new ArrayList());
        final User user1 = UserUtils.getUser("logged-in-user");

        userHistoryManager.removeHistoryForUser(OSUserConverter.convertToOSUser(user1));

        mockController.replay();

        final UserService userService = new DefaultUserService(userUtil, permissionMananger, null, null, null);

        final UserService.DeleteUserValidationResult result = new UserService.DeleteUserValidationResult(testUser);
        userService.removeUser(user1, result);

        assertEquals(1, clearActiveUsersCallCount.get());
        mockController.verify();

        try
        {
            UserUtils.getUser("logged-in-user");
            fail("User should not exist any longer");
        }
        catch (final EntityNotFoundException e)
        {}
    }

    public void testValidateRemoveJiraUserForAdminNoPermissionNoPassword()
    {
        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);

        final UserService userService = new DefaultUserService(null, permissionMananger, applicationProperties, null, null)
        {

            I18nHelper getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        permissionMananger.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(false);
        mockI18nBean.getText("admin.errors.users.delete.no.permission");
        mockController.setReturnValue("error message");
        mockController.replay();

        final UserService.DeleteUserValidationResult result = userService.validateDeleteUser(user, "fflintstone");

        assertFalse(result.isValid());
        assertTrue(result.getErrorCollection().hasAnyErrors());
        assertEquals("error message", result.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
    }

    public void testValidateRemoveJiraUserForAdminNoPermissionNoExtMgmnt()
    {
        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);
        final Mock mockJiraContactHelper = new Mock(JiraContactHelper.class);
        mockJiraContactHelper.expectAndReturn("getAdministratorContactMessage", P.ANY_ARGS, "please contact your JIRA administrators");

        final UserService userService = new DefaultUserService(null, permissionMananger, applicationProperties, null, (JiraContactHelper) mockJiraContactHelper.proxy())
        {

            I18nHelper getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        permissionMananger.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        mockController.setReturnValue(true);
        mockI18nBean.getText("admin.errors.users.cannot.delete.due.to.external.user.mgmt", "please contact your JIRA administrators");
        mockController.setReturnValue("error message");
        mockController.replay();

        final UserService.DeleteUserValidationResult result = userService.validateDeleteUser(user, "fflintstone");

        assertFalse(result.isValid());
        assertTrue(result.getErrorCollection().hasAnyErrors());
        assertEquals("error message", result.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
    }

    public void testValidateRemoveJiraUserForNullUsername()
    {
        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);

        final UserService userService = new DefaultUserService(null, permissionMananger, applicationProperties, null, null)
        {

            I18nHelper getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        permissionMananger.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        mockController.setReturnValue(false);
        mockI18nBean.getText("admin.errors.users.cannot.delete.due.to.invalid.username");
        mockController.setReturnValue("error message");
        mockController.replay();

        final UserService.DeleteUserValidationResult result = userService.validateDeleteUser(user, null);

        assertFalse(result.isValid());
        assertTrue(result.getErrorCollection().hasAnyErrors());
        assertEquals("error message", result.getErrorCollection().getErrors().get("username"));

        mockController.verify();
    }

    public void testValidateRemoveJiraUserCurrentlyLoggedIn()
    {
        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);

        final UserService userService = new DefaultUserService(null, permissionMananger, applicationProperties, null, null)
        {

            I18nHelper getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        permissionMananger.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        mockController.setReturnValue(false);
        mockI18nBean.getText("admin.errors.users.cannot.delete.currently.logged.in");
        mockController.setReturnValue("error message");
        mockController.replay();

        final UserService.DeleteUserValidationResult result = userService.validateDeleteUser(user, user.getName());

        assertFalse(result.isValid());
        assertTrue(result.getErrorCollection().hasAnyErrors());
        assertEquals("error message", result.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
    }

    public void testValidateRemoveJiraUserNotExists()
    {
        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);

        final UserService userService = new DefaultUserService(userUtil, permissionMananger, applicationProperties, null, null)
        {

            I18nHelper getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        permissionMananger.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        mockController.setReturnValue(false);
        mockI18nBean.getText("admin.errors.users.user.does.not.exist");
        mockController.setReturnValue("error message");
        mockController.replay();

        final UserService.DeleteUserValidationResult result = userService.validateDeleteUser(user, "non-existing-user");

        assertFalse(result.isValid());
        assertTrue(result.getErrorCollection().hasAnyErrors());
        assertEquals("error message", result.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
    }

    public void testValidateCreateAndValidateRemoveUserOk() throws Exception
    {
        checkDeleteValidation(false, true, 0, 0, new ArrayList(), null);
    }

    private void checkDeleteValidation(final boolean deleteUserPerm, final boolean loggedUserPerm, final long assignedIssues, final long createdIssues, final Collection projectsLeaded, final String errorCode)
            throws SearchException, PermissionException, CreateException
    {
        final long directoryId = 1L;
        final String username = "fflintstone";
        final String displayName = "Fred Flintstone";
        final String emailAddress = "fred@flintstone.com";
        final User expectedUser = new ImmutableUser(directoryId, username, displayName, emailAddress, true);
        final DirectoryImpl directory = new DirectoryImpl();
        directory.addAllowedOperation(OperationType.CREATE_USER);

        final Mock mockGlobalPermissionsManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionsManager.expectAndReturn("getGroups", new Constraint[] { P.eq(Permissions.USE) }, EasyList.build(testGroup));

        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);
        final UserService userService = new DefaultUserService(userUtil, permissionMananger, applicationProperties, null, null)
        {

            I18nHelper getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        permissionMananger.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        mockController.setReturnValue(false);
        _prepareMocks(1);        
        permissionMananger.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        mockController.setReturnValue(false);

        final Query query = JqlQueryBuilder.newBuilder().where().reporterUser(username).endWhere().buildQuery();
        searchProvider.searchCountOverrideSecurity(query, user);
        mockController.setReturnValue(createdIssues);

        final Query query2 = JqlQueryBuilder.newBuilder().where().assigneeUser(username).endWhere().buildQuery();
        searchProvider.searchCountOverrideSecurity(query2, user);
        mockController.setReturnValue(assignedIssues);

        projectManager.getProjectsLeadBy(createdUser);
        mockController.setReturnValue(projectsLeaded);

        permissionMananger.hasPermission(Permissions.SYSTEM_ADMIN, (com.atlassian.crowd.embedded.api.User) createdUser);
        mockController.setReturnValue(deleteUserPerm);

        if (deleteUserPerm)
        {

            permissionMananger.hasPermission(Permissions.SYSTEM_ADMIN, (com.atlassian.crowd.embedded.api.User) user);
            mockController.setReturnValue(loggedUserPerm);

        }

        if (errorCode != null)
        {
            if (!deleteUserPerm)
            {
                mockI18nBean.getText(errorCode, "'" + username + "'", "1");
                mockController.setReturnValue("expected message");
            }
            else
            {
                mockI18nBean.getText(errorCode);
                mockController.setReturnValue("expected message");
            }
        }
        expect(componentLocator.getComponentInstanceOfType(JiraLicenseService.class)).andReturn(jiraLicenseService);
        expect(userManager.getDirectory(directoryId)).andStubReturn(directory);
        expect(userManager.canUpdateUserPassword(expectedUser)).andReturn(true);
        mockController.replay();

        //first check the user doesn't exist.
        User fred = userUtil.getUser(username);
        assertNull(fred);

        final UserService.CreateUserValidationResult validationResult = userService.validateCreateUserForAdminPasswordRequired(user, username,
            "mypassword", "mypassword", emailAddress, displayName);

        userService.createUserWithNotification(validationResult);
        fred = userUtil.getUser(username);

        assertEquals(username, fred.getName());
        assertEquals(displayName, fred.getDisplayName());
        assertEquals(emailAddress, fred.getEmailAddress());
//        assertTrue(fred.authenticate("mypassword"));

        final UserService.DeleteUserValidationResult result = userService.validateDeleteUser(user, username);

        if (errorCode == null)
        {
            assertTrue(result.isValid());
            assertFalse(result.getErrorCollection().hasAnyErrors());
        }
        else
        {
            assertFalse(result.isValid());
            assertTrue(result.getErrorCollection().hasAnyErrors());
            assertEquals("expected message", result.getErrorCollection().getErrorMessages().iterator().next());
        }

        mockController.verify();
    }

    public void testValidateCreateAndValidateRemoveUserAssignedIssues() throws Exception
    {
        checkDeleteValidation(false, true, 1, 0, new ArrayList(), "admin.errors.users.cannot.delete.due.to.assigned.issues");
    }

    public void testValidateCreateAndValidateRemoveUserReportedIssues() throws Exception
    {
        checkDeleteValidation(false, true, 0, 1, new ArrayList(), "admin.errors.users.cannot.delete.due.to.reported.issues");
    }

    public void testValidateCreateAndValidateRemoveUserProjectLead() throws Exception
    {
        checkDeleteValidation(false, true, 0, 0, EasyList.build("project"), "admin.errors.users.cannot.delete.due.to.project.lead");
    }

    public void testValidateCreateAndValidateRemoveAdminPerm() throws Exception
    {
        checkDeleteValidation(true, false, 0, 0, new ArrayList(), "admin.errors.users.cannot.delete.due.to.sysadmin");
    }

    public void testValidateCreateAndValidateRemoveNoAdminByAdmin() throws Exception
    {
        checkDeleteValidation(true, true, 0, 0, new ArrayList(), null);
    }

    protected void tearDown() throws Exception
    {
        CoreTransactionUtil.setUseTransactions(false);

        super.tearDown();
    }

}
