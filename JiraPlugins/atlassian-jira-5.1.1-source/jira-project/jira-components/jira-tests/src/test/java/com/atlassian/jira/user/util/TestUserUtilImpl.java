package com.atlassian.jira.user.util;

import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.core.util.StaticCrowdServiceFactory;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.user.UserEventType;
import com.atlassian.jira.exception.AddException;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.studio.MockStudioHooks;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.seraph.spi.rememberme.RememberMeTokenDao;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.easymock.MockControl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * Unit test for UserUtilImpl
 */
public class TestUserUtilImpl extends LegacyJiraMockTestCase
{

    private UserUtilImpl userUtil;
    private Group testGroup;
    private Group testGroup2;
    private User testUser;
    private User testUser2;
    private AtomicInteger clearActiveUsersCallCount;
    private SearchRequestService searchRequestService;
    private PortalPageService portalPageService;
    private JiraServiceContextImpl ctx;
    private MockController mockController;
    private PermissionManager permissionMananger;
    private ApplicationProperties applicationProperties;
    private SearchProvider searchProvider;
    private ProjectManager projectManager;
    private ProjectRoleService projectRoleService;
    private ProjectComponentManager projectComponentManager;
    private SubscriptionManager subscriptionManager;
    private NotificationSchemeManager notificationSchemeManager;
    private UserManager userManager;
    private JiraLicenseService jiraLicenseService;
    private ComponentLocator componentLocator;
    private CrowdService crowdService;
    private RememberMeTokenDao rememberMeTokenDao;
    private LoginManager loginManager;

    public TestUserUtilImpl(final String s)
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
        componentLocator = mockController.getMock(ComponentLocator.class);
        rememberMeTokenDao = mockController.getMock(RememberMeTokenDao.class);
        loginManager = mockController.getMock(LoginManager.class);
        ctx = new JiraServiceContextImpl(testUser);


        jiraLicenseService = mockController.getMock(JiraLicenseService.class);
        final LicenseDetails licenseDetails = mockController.getMock(LicenseDetails.class);
        expect(jiraLicenseService.getLicense()).andStubReturn(licenseDetails);
        expect(licenseDetails.isLicenseSet()).andStubReturn(true);
        expect(licenseDetails.isUnlimitedNumberOfUsers()).andStubReturn(true);

        userUtil = new UserUtilImpl(componentLocator, (IssueSecurityLevelManager) mockIssueSecurityLevelManager.proxy(), null, StaticCrowdServiceFactory.getCrowdService(),
                null, permissionMananger, applicationProperties, searchProvider, projectManager, projectRoleService, projectComponentManager,
            subscriptionManager, notificationSchemeManager, null, userManager, null, new MockStudioHooks())
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
        crowdService = StaticCrowdServiceFactory.getCrowdService();
        testGroup = createMockGroup("myTestGroup");
        testGroup2 = createMockGroup("mySecondTestGroup");
        testUser = createMockUser("logged-in-user");
        testUser2 = createMockUser("second-logged-in-user");
    }

    public void testAddAndRemoveUserToGroup()
    {
        try
        {
            userUtil.addUserToGroup(testGroup, testUser);

            assertEquals(userUtil.getAllUsersInGroups(Collections.singleton(testGroup)).size(), 1);
            assertEquals(userUtil.getAllUsersInGroups(Collections.singleton(testGroup)).iterator().next().getName(), testUser.getName());

            userUtil.removeUserFromGroup(testGroup, testUser);
            assertEquals(userUtil.getAllUsersInGroups(Collections.singleton(testGroup)).size(), 0);
            assertEquals(2, clearActiveUsersCallCount.get());
        }
        catch (final Exception e)
        {
            fail("unable to add user to group.");
        }
    }

    public void testAddAndRemoveUserToGroups()
    {
        final ArrayList<Group> groups = new ArrayList<Group>();
        groups.add(testGroup);
        groups.add(testGroup2);
        try
        {
            userUtil.addUserToGroups(groups, testUser);
            assertEquals(userUtil.getAllUsersInGroups(Collections.singleton(testGroup)).size(), 1);
            assertEquals(userUtil.getAllUsersInGroups(Collections.singleton(testGroup)).iterator().next().getName(), testUser.getName());
            assertEquals(userUtil.getAllUsersInGroups(Collections.singleton(testGroup2)).size(), 1);
            assertEquals(userUtil.getAllUsersInGroups(Collections.singleton(testGroup2)).iterator().next().getName(), testUser.getName());

            userUtil.removeUserFromGroups(groups, testUser);
            assertEquals(userUtil.getAllUsersInGroups(Collections.singleton(testGroup)).size(), 0);
            assertEquals(userUtil.getAllUsersInGroups(Collections.singleton(testGroup2)).size(), 0);
            assertEquals(4, clearActiveUsersCallCount.get());
        }
        catch (final Exception e)
        {
            fail("unable to add user to group.");
        }
    }

    public void testGetActiveUserCountOneUser() throws PermissionException, AddException
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();
        mockGlobalPermissionManager.getGroupNames(Permissions.USE);
        mockGlobalPermissionManagerControl.setReturnValue(EasyList.build(testGroup.getName()), 1);
        mockGlobalPermissionManager.getGroupNames(Permissions.ADMINISTER);
        mockGlobalPermissionManagerControl.setReturnValue(Collections.EMPTY_LIST, 1);
        mockGlobalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN);
        mockGlobalPermissionManagerControl.setReturnValue(Collections.EMPTY_LIST, 1);
        mockGlobalPermissionManagerControl.replay();

        final UserUtil userUtil = new UserUtilImpl(componentLocator, null, mockGlobalPermissionManager, StaticCrowdServiceFactory.getCrowdService(), null, null, null, null,
            null, null, null, null, null, null, null, null, new MockStudioHooks());
        userUtil.addUserToGroup(testGroup, testUser);
        final int activeUserCount = userUtil.getActiveUserCount();
        assertEquals(1, activeUserCount);

        //calling the method again which should now be cached.  The mock below should verify that we
        //only called the usermanagment code once.
        userUtil.getActiveUserCount();
        mockGlobalPermissionManagerControl.verify();
    }

    public void testClearActiveUserCount() throws PermissionException, AddException
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();
        mockGlobalPermissionManager.getGroupNames(Permissions.USE);
        mockGlobalPermissionManagerControl.setReturnValue(EasyList.build(testGroup.getName()), 2);
        mockGlobalPermissionManager.getGroupNames(Permissions.ADMINISTER);
        mockGlobalPermissionManagerControl.setReturnValue(Collections.EMPTY_LIST, 2);
        mockGlobalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN);
        mockGlobalPermissionManagerControl.setReturnValue(Collections.EMPTY_LIST, 2);
        mockGlobalPermissionManagerControl.replay();

        final UserUtil userUtil = new UserUtilImpl(componentLocator, null, mockGlobalPermissionManager, StaticCrowdServiceFactory.getCrowdService(), null, null, null, null,
            null, null, null, null, null, null, null, null, new MockStudioHooks());
        userUtil.addUserToGroup(testGroup, testUser);
        int activeUserCount = userUtil.getActiveUserCount();
        assertEquals(1, activeUserCount);

        //clearing the cache should mean we need to recalculate the user count
        userUtil.clearActiveUserCount();
        activeUserCount = userUtil.getActiveUserCount();
        assertEquals(1, activeUserCount);

        //calling the method again which should now be cached.  The mock below should verify that we
        //only called the usermanagment code once.
        userUtil.getActiveUserCount();
        mockGlobalPermissionManagerControl.verify();
    }

    public void testSameUserInMultipleGroups() throws PermissionException, AddException
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();
        mockGlobalPermissionManager.getGroupNames(Permissions.USE);
        mockGlobalPermissionManagerControl.setReturnValue(EasyList.build(testGroup.getName(), testGroup2.getName()), 1);
        mockGlobalPermissionManager.getGroupNames(Permissions.ADMINISTER);
        mockGlobalPermissionManagerControl.setReturnValue(Collections.EMPTY_LIST, 1);
        mockGlobalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN);
        mockGlobalPermissionManagerControl.setReturnValue(Collections.EMPTY_LIST, 1);
        mockGlobalPermissionManagerControl.replay();

        final UserUtil userUtil = new UserUtilImpl(componentLocator, null, mockGlobalPermissionManager, StaticCrowdServiceFactory.getCrowdService(), null, null, null, null,
            null, null, null, null, null, null, null, null, new MockStudioHooks());
        userUtil.addUserToGroup(testGroup, testUser);
        userUtil.addUserToGroup(testGroup2, testUser);
        assertEquals(1, userUtil.getActiveUserCount());

        mockGlobalPermissionManagerControl.verify();
    }

    public void testGroupsInMultiplePermissions() throws PermissionException, AddException
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();
        mockGlobalPermissionManager.getGroupNames(Permissions.USE);
        mockGlobalPermissionManagerControl.setReturnValue(EasyList.build(testGroup.getName(), testGroup2.getName()), 1);
        mockGlobalPermissionManager.getGroupNames(Permissions.ADMINISTER);
        mockGlobalPermissionManagerControl.setReturnValue(Collections.EMPTY_LIST, 1);
        mockGlobalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN);
        mockGlobalPermissionManagerControl.setReturnValue(EasyList.build(testGroup.getName()), 1);
        mockGlobalPermissionManagerControl.replay();

        final UserUtil userUtil = new UserUtilImpl(componentLocator, null, mockGlobalPermissionManager, StaticCrowdServiceFactory.getCrowdService(), null, null, null, null,
            null, null, null, null, null, null, null, null, new MockStudioHooks());
        userUtil.addUserToGroup(testGroup, testUser);
        userUtil.addUserToGroup(testGroup2, testUser);
        assertEquals(1, userUtil.getActiveUserCount());

        mockGlobalPermissionManagerControl.verify();
    }

    public void testMultipleUsersAndGroups() throws PermissionException, AddException
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();
        mockGlobalPermissionManager.getGroupNames(Permissions.USE);
        mockGlobalPermissionManagerControl.setReturnValue(EasyList.build(testGroup.getName(), testGroup2.getName()), 1);
        mockGlobalPermissionManager.getGroupNames(Permissions.ADMINISTER);
        mockGlobalPermissionManagerControl.setReturnValue(Collections.EMPTY_LIST, 1);
        mockGlobalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN);
        mockGlobalPermissionManagerControl.setReturnValue(EasyList.build(testGroup.getName()), 1);
        mockGlobalPermissionManagerControl.replay();

        final UserUtil userUtil = new UserUtilImpl(componentLocator, null, mockGlobalPermissionManager, StaticCrowdServiceFactory.getCrowdService(), null, null, null, null,
            null, null, null, null, null, null, null, null, new MockStudioHooks());
        userUtil.addUserToGroup(testGroup, testUser);
        userUtil.addUserToGroup(testGroup2, testUser);
        userUtil.addUserToGroup(testGroup, testUser2);
        assertEquals(2, userUtil.getActiveUserCount());

        mockGlobalPermissionManagerControl.verify();
    }

    public void testEmptyGroups()
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();
        mockGlobalPermissionManager.getGroupNames(Permissions.USE);
        mockGlobalPermissionManagerControl.setReturnValue(EasyList.build(testGroup.getName(), testGroup2.getName()), 1);
        mockGlobalPermissionManager.getGroupNames(Permissions.ADMINISTER);
        mockGlobalPermissionManagerControl.setReturnValue(Collections.EMPTY_LIST, 1);
        mockGlobalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN);
        mockGlobalPermissionManagerControl.setReturnValue(Collections.EMPTY_LIST, 1);
        mockGlobalPermissionManagerControl.replay();

        final UserUtil userUtil = new UserUtilImpl(componentLocator, null, mockGlobalPermissionManager, StaticCrowdServiceFactory.getCrowdService(), null, null, null, null,
            null, null, null, null, null, null, null, null, new MockStudioHooks());
        final int activeUserCount = userUtil.getActiveUserCount();
        assertEquals(0, activeUserCount);

        mockGlobalPermissionManagerControl.verify();
    }

    public void testHasExceededUserLimitWithoutLicenseUserLimit()
    {
        final LicenseDetails licenseDetails = createMock(LicenseDetails.class);
        expect(licenseDetails.isLicenseSet()).andReturn(Boolean.TRUE).anyTimes();
        expect(licenseDetails.isUnlimitedNumberOfUsers()).andReturn(Boolean.TRUE).anyTimes();
        replay(licenseDetails);

        final UserUtilImpl userUtil = new UserUtilImpl(componentLocator, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks())
        {
            @Override
            LicenseDetails getLicenseDetails()
            {
                return licenseDetails;
            }

            @Override
            public int getActiveUserCount()
            {
                fail("Shouldn't call this method as the licensetype does not require a user limit");
                return Integer.MAX_VALUE;
            }
        };

        assertFalse(userUtil.hasExceededUserLimit());
        verify(licenseDetails);
    }

    public void testHasExceededUserLimit()
    {
        final LicenseDetails licenseDetails = createMock(LicenseDetails.class);
        expect(licenseDetails.isLicenseSet()).andReturn(Boolean.TRUE).anyTimes();
        expect(licenseDetails.isUnlimitedNumberOfUsers()).andReturn(Boolean.FALSE).anyTimes();
        expect(licenseDetails.getMaximumNumberOfUsers()).andReturn(3).anyTimes();
        replay(licenseDetails);

        assertFalse(getUserUtilImpl(licenseDetails, 1).hasExceededUserLimit());

        assertFalse(getUserUtilImpl(licenseDetails, 3).hasExceededUserLimit());

        assertTrue(getUserUtilImpl(licenseDetails, 4).hasExceededUserLimit());

        verify(licenseDetails);
    }

    private UserUtilImpl getUserUtilImpl(final LicenseDetails licenseDetails, final int i)
    {
        return new UserUtilImpl(componentLocator, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks())
        {
            @Override
            LicenseDetails getLicenseDetails()
            {
                return licenseDetails;
            }

            @Override
            public int getActiveUserCount()
            {
                //One more than the license limit.  We're in Barney!
                return i;
            }
        };
    }

    public void testCanActivateNumberOfUsers()
    {
        final LicenseDetails licenseDetails = createMock(LicenseDetails.class);
        expect(licenseDetails.isLicenseSet()).andReturn(Boolean.TRUE).anyTimes();
        expect(licenseDetails.isUnlimitedNumberOfUsers()).andReturn(Boolean.FALSE).anyTimes();
        expect(licenseDetails.getMaximumNumberOfUsers()).andReturn(3).anyTimes();
        replay(licenseDetails);

        final UserUtilImpl userUtil = getUserUtilImpl(licenseDetails, 1);

        assertTrue(userUtil.canActivateNumberOfUsers(0));
        assertTrue(userUtil.canActivateNumberOfUsers(1));
        assertTrue(userUtil.canActivateNumberOfUsers(2));
        assertFalse(userUtil.canActivateNumberOfUsers(3));
        verify(licenseDetails);
    }

    public void testActivateNumberOfUsersWhenUnderLimit() throws Exception
    {
        final GlobalPermissionManager mockGlobalPermissionsManager = mockController.getMock(GlobalPermissionManager.class);
        mockGlobalPermissionsManager.getGroupsWithPermission(Permissions.USE);
        mockController.setReturnValue(EasyList.build(testGroup, testGroup2));
        mockGlobalPermissionsManager.getGroupsWithPermission(Permissions.ADMINISTER);
        mockController.setReturnValue(EasyList.build());
        mockGlobalPermissionsManager.getGroupsWithPermission(Permissions.SYSTEM_ADMIN);
        mockController.setReturnValue(EasyList.build());
        mockController.replay();

        final UserUtil mockUserUtil = new UserUtilImpl(componentLocator, null,  mockGlobalPermissionsManager, StaticCrowdServiceFactory.getCrowdService(), null, null,
            null, null, null, null, null, null, null, null, null, null, new MockStudioHooks())
        {
            @Override
            public boolean canActivateNumberOfUsers(final int numUsers)
            {
                return true;
            }
        };

        // attempt to activate a user
        mockUserUtil.addToJiraUsePermission(testUser);

        // assert they now belong to both groups
        assertTrue(crowdService.isUserMemberOfGroup(testUser, testGroup));
        assertTrue(crowdService.isUserMemberOfGroup(testUser, testGroup2));
        mockController.verify();

    }

    public void testActivateNumberOfUsersWhenOverLimit() throws Exception
    {
        final Mock mockGlobalPermissionManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionManager.expectAndReturn("getGroupsWithPermission", new Constraint[] { P.eq(Permissions.USE) }, EasyList.build(testGroup, testGroup2));

        final UserUtil mockUserUtil = new UserUtilImpl(componentLocator, null, (GlobalPermissionManager) mockGlobalPermissionManager.proxy(), StaticCrowdServiceFactory.getCrowdService(), null, null,
            null, null, null, null, null, null, null, null, null, null, new MockStudioHooks())
        {
            @Override
            public boolean canActivateNumberOfUsers(final int numUsers)
            {
                return false;
            }
        };

        // attempt to activate a user
        mockUserUtil.addToJiraUsePermission(testUser);

        // assert they now belong to both groups
        assertFalse(crowdService.isUserMemberOfGroup(testUser, testGroup));
        assertFalse(crowdService.isUserMemberOfGroup(testUser, testGroup2));
    }

    public void testCanActivateUsers() throws OperationNotPermittedException
    {
        final LicenseDetails licenseDetails = createMock(LicenseDetails.class);
        expect(licenseDetails.isLicenseSet()).andReturn(Boolean.TRUE).anyTimes();
        expect(licenseDetails.isUnlimitedNumberOfUsers()).andReturn(Boolean.FALSE).anyTimes();
        expect(licenseDetails.getMaximumNumberOfUsers()).andReturn(3).anyTimes();
        replay(licenseDetails);

        final UserUtilImpl mockUserUtil = new UserUtilImpl(componentLocator, null, null, StaticCrowdServiceFactory.getCrowdService(), null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks())
        {
            @Override
            LicenseDetails getLicenseDetails()
            {
                return licenseDetails;
            }

            @Override
            Set<String> getGroupsWithUsePermission()
            {
                final Set<String> ret = new HashSet<String>();
                ret.add(testGroup.getName());
                ret.add("jira-administrators");
                return ret;
            }

            @Override
            public User getUser(final String userName)
            {
                return testUser;
            }

            @Override
            public int getActiveUserCount()
            {
                return 3;
            }
        };

        //can't activate testuser, because he's not in any groups!
        assertFalse(mockUserUtil.canActivateUsers(EasyList.build(testUser.getName())));

        //now lets add the user to a group that's already got the use permission and try to activate again.
        crowdService.addUserToGroup(testUser, testGroup);
        assertTrue(mockUserUtil.canActivateUsers(EasyList.build(testUser.getName())));

        verify(licenseDetails);
    }

    public void testCanActivateUsersAlreadyOverLicenseLimit() throws OperationNotPermittedException
    {
        final LicenseDetails licenseDetails = createMock(LicenseDetails.class);
        expect(licenseDetails.isLicenseSet()).andReturn(Boolean.TRUE).anyTimes();
        expect(licenseDetails.isUnlimitedNumberOfUsers()).andReturn(Boolean.FALSE).anyTimes();
        expect(licenseDetails.getMaximumNumberOfUsers()).andReturn(3).anyTimes();
        replay(licenseDetails);

        final UserUtilImpl mockUserUtil = new UserUtilImpl(componentLocator, null, null, StaticCrowdServiceFactory.getCrowdService(), null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks())
        {
            @Override
            LicenseDetails getLicenseDetails()
            {
                return licenseDetails;
            }

            @Override
            Set<String> getGroupsWithUsePermission()
            {
                final Set<String> ret = new HashSet<String>();
                ret.add(testGroup.getName());
                ret.add("jira-administrators");
                return ret;
            }

            @Override
            public User getUser(final String userName)
            {
                return testUser;
            }

            @Override
            public synchronized int getActiveUserCount()
            {
                return 4;
            }
        };

        //can't active testuser, because he's not in any groups!
        assertFalse(mockUserUtil.canActivateUsers(EasyList.build(testUser.getName())));

        //now lets add the user to a group that's already got the use permission and try to activate again.
        crowdService.addUserToGroup(testUser, testGroup);
        assertTrue(mockUserUtil.canActivateUsers(EasyList.build(testUser.getName())));
        verify(licenseDetails);
    }

    public void testGetUser()
    {
        String userName = "logged-in-user";
        final UserUtilImpl userUtil = new UserUtilImpl(componentLocator, null, null, StaticCrowdServiceFactory.getCrowdService(), null, null, null, null, null, null, null, null, null,
            null, null, null, new MockStudioHooks());
        final User myTestUser = userUtil.getUser(userName);
        assertEquals(testUser.getName(), myTestUser.getName());
        assertTrue(userUtil.userExists(userName));

        userName = "IDONTEXIST";
        final User nonExistantUser = userUtil.getUser(userName);
        assertNull(nonExistantUser);
        assertFalse(userUtil.userExists(userName));
    }

    public void testGetNullUser()
    {
        // Getting a null user should work with no dependencies. See JRA-15821, CWD-1275
        final UserUtilImpl userUtil = new UserUtilImpl(componentLocator, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, new MockStudioHooks());
        assertNull(userUtil.getUser(null));
    }

    public void testCanActivateNumberOfUsersWithNegative()
    {
        final UserUtilImpl userUtil = new UserUtilImpl(componentLocator, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks());
        try
        {
            userUtil.canActivateNumberOfUsers(-1);
            fail("Expected IllegalArgumentException");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    public void testCanActivateUsersWithNull()
    {
        final UserUtilImpl userUtil = new UserUtilImpl(componentLocator, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks());
        try
        {
            userUtil.canActivateUsers(null);
            fail("Expected NullArgumentException");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    public void testGetAdministrators() throws OperationNotPermittedException
    {
        //this is the admins group
        crowdService.addUserToGroup(testUser, testGroup);
        //some other group not an admin.
        crowdService.addUserToGroup(testUser2, testGroup2);
        Mock mockGlobalPermissionsManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionsManager.expectAndReturn("getGroupsWithPermission", new Constraint[] { P.eq(Permissions.ADMINISTER) }, EasyList.build(testGroup));

        UserUtilImpl userUtil = new UserUtilImpl(componentLocator, null, (GlobalPermissionManager) mockGlobalPermissionsManager.proxy(),
                StaticCrowdServiceFactory.getCrowdService(), null, null, null, null, null, null, null, null, null, null, null, null, null);
        Collection admins = userUtil.getAdministrators();
        assertEquals(1, admins.size());
        assertEquals(testUser, admins.iterator().next());

        //now lets try a test with no users.
        mockGlobalPermissionsManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionsManager.expectAndReturn("getGroupsWithPermission", new Constraint[] { P.eq(Permissions.ADMINISTER) }, Collections.EMPTY_LIST);

        userUtil = new UserUtilImpl(componentLocator, null, (GlobalPermissionManager) mockGlobalPermissionsManager.proxy(), StaticCrowdServiceFactory.getCrowdService(), null, null, null, null, null,
            null, null, null, null, null, null, null, new MockStudioHooks());
        admins = userUtil.getAdministrators();
        assertEquals(0, admins.size());
    }

    public void testGetSystemAdministrators() throws OperationNotPermittedException
    {
        //this is the sys-admins group
        crowdService.addUserToGroup(testUser2, testGroup2);
        //some other group not an admin.
        crowdService.addUserToGroup(testUser, testGroup);
        Mock mockGlobalPermissionsManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionsManager.expectAndReturn("getGroupsWithPermission", new Constraint[] { P.eq(Permissions.SYSTEM_ADMIN) }, EasyList.build(testGroup2));

        UserUtilImpl userUtil = new UserUtilImpl(componentLocator, null, (GlobalPermissionManager) mockGlobalPermissionsManager.proxy(),
                StaticCrowdServiceFactory.getCrowdService(), null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks());
        Collection admins = userUtil.getSystemAdministrators();
        assertEquals(1, admins.size());
        assertEquals(testUser2, admins.iterator().next());

        //now lets try a test with no users.
        mockGlobalPermissionsManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionsManager.expectAndReturn("getGroupsWithPermission", new Constraint[] { P.eq(Permissions.SYSTEM_ADMIN) }, Collections.EMPTY_LIST);

        userUtil = new UserUtilImpl(componentLocator, null, (GlobalPermissionManager) mockGlobalPermissionsManager.proxy(), StaticCrowdServiceFactory.getCrowdService(), null, null, null, null,
            null, null, null, null, null, null, null, null, new MockStudioHooks());
        admins = userUtil.getSystemAdministrators();
        assertEquals(0, admins.size());
    }

    public void testCreateJiraUserWithPassword()
            throws PermissionException, FailedAuthenticationException, CreateException
    {
        final String password = "mypassword";
        final User fred = createUser(password);

        assertNotNull(crowdService.authenticate(fred.getName(), password));

        final SortedSet<Group> groups = userUtil.getGroupsForUser(fred.getName());
        assertEquals(1, groups.size());
        assertTrue(groups.contains(testGroup));
        mockController.verify();
    }

    public void testCreateJiraUserWithBlankPassword()
            throws PermissionException, ExpiredCredentialException, InactiveAccountException, CreateException
    {
        final String password = "";
        final User fred = createUser(password);

        try
        {
            crowdService.authenticate(fred.getName(), password);
            fail("The password is incorrect, this should have not authenticated.");
        }
        catch (FailedAuthenticationException e)
        {
            assertTrue(true);
        }
        // Try with the value we use as a hash.  This should not work either.
        try
        {
            crowdService.authenticate(fred.getName(), "XXXXXXINVALIDCREDENTIALXXXXXX");
            fail("The password is incorrect, this should have not authenticated.");
        }
        catch (FailedAuthenticationException e)
        {
            assertTrue(true);
        }
        mockController.verify();
    }

    private User createUser(final String password) throws CreateException, PermissionException
    {
        final long directoryId = 1L;
        final String username = "fflintstone";
        final String displayName = "Fred Flintstone";
        final String emailAddress = "fred@flintstone.com";
        final User expectedUser = new ImmutableUser(directoryId, username, displayName, emailAddress, true);

        expect(componentLocator.getComponentInstanceOfType(JiraLicenseService.class)).andStubReturn(jiraLicenseService);
        final GlobalPermissionManager mockGlobalPermissionsManager = mockController.getMock(GlobalPermissionManager.class);
        mockGlobalPermissionsManager.getGroupsWithPermission(Permissions.USE);
        mockController.setReturnValue(EasyList.build(testGroup));
        mockGlobalPermissionsManager.getGroupsWithPermission(Permissions.ADMINISTER);
        mockController.setReturnValue(EasyList.build());
        mockGlobalPermissionsManager.getGroupsWithPermission(Permissions.SYSTEM_ADMIN);
        mockController.setReturnValue(EasyList.build());

        final DirectoryImpl directory = new DirectoryImpl();
        directory.addAllowedOperation(OperationType.CREATE_USER);
        expect(userManager.getDirectory(directoryId)).andStubReturn(directory);
        expect(userManager.canUpdateUserPassword(expectedUser)).andReturn(true);

        mockController.replay();

        final UserUtil userUtil = new UserUtilImpl(componentLocator, null, mockGlobalPermissionsManager,
                StaticCrowdServiceFactory.getCrowdService(), null, null, null, null, null, null, null, null, null, null, userManager, null, new MockStudioHooks());

        //first check the user doesn't exist.
        User fred = userUtil.getUser(username);
        assertNull(fred);

        userUtil.createUserWithNotification(username, password, emailAddress, displayName, UserEventType.USER_CREATED);
        fred = userUtil.getUser(username);

        assertEquals(username, fred.getName());
        assertEquals(displayName, fred.getDisplayName());
        assertEquals(emailAddress, fred.getEmailAddress());

        return fred;
    }

    public void testSameUserInMultipleGroupsWithInvalidGroup() throws PermissionException, AddException
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();
        mockGlobalPermissionManager.getGroupNames(Permissions.USE);
        mockGlobalPermissionManagerControl.setReturnValue(EasyList.build(testGroup.getName(), "InvalidGroup", testGroup2.getName()), 1);
        mockGlobalPermissionManager.getGroupNames(Permissions.ADMINISTER);
        mockGlobalPermissionManagerControl.setReturnValue(Collections.EMPTY_LIST, 1);
        mockGlobalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN);
        mockGlobalPermissionManagerControl.setReturnValue(Collections.EMPTY_LIST, 1);
        mockGlobalPermissionManagerControl.replay();

        final UserUtil userUtil = new UserUtilImpl(componentLocator, null, mockGlobalPermissionManager, StaticCrowdServiceFactory.getCrowdService(), null, null, null, null,
            null, null, null, null, null, null, null, null, new MockStudioHooks());
        userUtil.addUserToGroup(testGroup, testUser);
        userUtil.addUserToGroup(testGroup2, testUser);
        assertEquals(1, userUtil.getActiveUserCount());

        mockGlobalPermissionManagerControl.verify();
    }

    public void testAddUserToGroupFail()
    {
        final UserUtil userUtil = new UserUtilImpl(componentLocator, null, null, StaticCrowdServiceFactory.getCrowdService(), null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks())
        {
            @Override
            void doAddUserToGroup(final com.atlassian.crowd.embedded.api.Group group, final com.atlassian.crowd.embedded.api.User userToAdd)
                    throws PermissionException
            {
                throw new PermissionException("Bleh!");
            }
        };

        try
        {
            userUtil.addUserToGroup(null, new MockUser("dude"));
            fail("Should have thrown exception");
        }
        catch (AddException e)
        {
            // true
        }
        catch (PermissionException e)
        {
            // true
        }

    }

    public void testAddUserToGroupFailAndIgnore() throws PermissionException
    {
        final GlobalPermissionManager mockGlobalPermissionsManager = mockController.getMock(GlobalPermissionManager.class);
        mockGlobalPermissionsManager.getGroupsWithPermission(Permissions.USE);
        mockController.setReturnValue(EasyList.build(testGroup,testGroup2));
        mockGlobalPermissionsManager.getGroupsWithPermission(Permissions.ADMINISTER);
        mockController.setReturnValue(EasyList.build());
        mockGlobalPermissionsManager.getGroupsWithPermission(Permissions.SYSTEM_ADMIN);
        mockController.setReturnValue(EasyList.build());
        mockController.replay();

        final AtomicInteger numberOfCalls = new AtomicInteger(0);

        final UserUtil mockUserUtil = new UserUtilImpl(componentLocator, null,  mockGlobalPermissionsManager, StaticCrowdServiceFactory.getCrowdService(), null, null,
            null, null, null, null, null, null, null, null, null, null, new MockStudioHooks())
        {
            @Override
            public boolean canActivateNumberOfUsers(final int numUsers)
            {
                return true;
            }

            @Override
            void doAddUserToGroup(final com.atlassian.crowd.embedded.api.Group group, final com.atlassian.crowd.embedded.api.User userToAdd)
                    throws PermissionException
            {
                numberOfCalls.incrementAndGet();
                throw new PermissionException("Bleh!");
            }
        };

        // attempt to activate a user
        mockUserUtil.addToJiraUsePermission(new MockUser("fred"));

        assertEquals(2, numberOfCalls.get());

        mockController.verify();

    }

    public void testGetDisplayableNameSafelyForNullUser()
    {
        assertNull(userUtil.getDisplayableNameSafely(null));
    }

    public void testGetDisplayableNameSafelyFullNameIsNullOrBlank()
    {
        User dude = new MockUser("mock");
        assertEquals("mock", userUtil.getDisplayableNameSafely(dude));

        dude = new MockUser("mock", "", "");
        assertEquals("mock", userUtil.getDisplayableNameSafely(dude));

        dude = new MockUser("mock", "         ", "");
        assertEquals("mock", userUtil.getDisplayableNameSafely(dude));
    }

    public void testGetDisplayableNameSafelyFullNameIsNotNull()
    {
        final User dude = new MockUser("mock", "Mr Mock", "");
        assertEquals("Mr Mock", userUtil.getDisplayableNameSafely(dude));
    }

    public void testGetUsersInGroupsNullInput()
    {
        // null input first
        try
        {
            userUtil.getUsersInGroupNames(null);
            fail("Should not allow null group names collections");
        }
        catch (final IllegalArgumentException expected)
        {
        }
        try
        {
            userUtil.getUsersInGroups(null);
            fail("Should not allow null group collections");
        }
        catch (final IllegalArgumentException expected)
        {

        }
    }

    public void testGetUserInGroupNames_UnknownGroupNames()
    {
        final List<String> list = new ArrayList<String>();
        list.add("flurg");
        list.add("nurgle");
        list.add(null);

        final Set usersInGroups = userUtil.getUsersInGroupNames(list);
        assertNotNull(usersInGroups);
        assertEquals(0, usersInGroups.size());
    }

    public void testGetUserInGroups_UnknownGroupNames() throws OperationNotPermittedException, InvalidGroupException
    {
        final List<Group> list = new ArrayList<Group>();
        list.add(createMockGroup("flurg"));
        list.add(createMockGroup("nurgle"));
        list.add(null);

        final Set<User> usersInGroups = userUtil.getAllUsersInGroups(list);
        assertNotNull(usersInGroups);
        assertEquals(0, usersInGroups.size());
    }

    public void testGetUserInGroupNames_isOKAndSorted()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException, InvalidGroupException
    {

        final Group players = createMockGroup("TestUserUtilImpl_players");
        final User bradB = createMockUser("TestUserUtilImpl_bradB");
        final User scottH = createMockUser("TestUserUtilImpl_scottH");
        addUserToGroup(scottH, players);
        addUserToGroup(bradB, players);

        final List<String> list = new ArrayList<String>();
        list.add("flurg");
        list.add("nurgle");
        list.add(null);
        list.add(players.getName());

        final SortedSet<User> usersInGroups = userUtil.getUsersInGroupNames(list);
        assertNotNull(usersInGroups);
        assertEquals(2, usersInGroups.size());

        final Iterator iterator = usersInGroups.iterator();
        assertEquals(bradB, iterator.next());
        assertEquals(scottH, iterator.next());
    }

    public void testGetUserInGroups_isOKAndSorted()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException, InvalidGroupException
    {

        final Group players = createMockGroup("TestUserUtilImpl_players");
        final User bradB = createMockUser("TestUserUtilImpl_bradB");
        final User scottH = createMockUser("TestUserUtilImpl_scottH");
        crowdService.addUserToGroup(scottH, players);
        crowdService.addUserToGroup(bradB, players);

        final List<Group> list = new ArrayList<Group>();
        list.add(createMockGroup("flurg"));
        list.add(createMockGroup("nurgle"));
        list.add(null);
        list.add(players);

        final Set<User> usersInGroups = userUtil.getAllUsersInGroups(list);
        assertNotNull(usersInGroups);
        assertEquals(2, usersInGroups.size());

        final Iterator iterator = usersInGroups.iterator();
        assertEquals(bradB, iterator.next());
        assertEquals(scottH, iterator.next());
    }

    public void test_changePassword()
            throws PermissionException, ExpiredCredentialException, InactiveAccountException, OperationNotPermittedException, InvalidCredentialException, InvalidUserException
    {
        final User bradB = createMockUser("TestUserUtilImpl_bradB");
        crowdService.updateUserCredential(bradB, "simple");

        expect(componentLocator.getComponent(RememberMeTokenDao.class)).andStubReturn(rememberMeTokenDao);
        expect(componentLocator.getComponent(LoginManager.class)).andStubReturn(loginManager);
        rememberMeTokenDao.removeAllForUser(bradB.getName()); expectLastCall();
        loginManager.resetFailedLoginCount(bradB); expectLastCall();

        mockController.replay(componentLocator,rememberMeTokenDao, loginManager);

        userUtil.changePassword(bradB, "newPassword");

        try
        {
            assertNotNull(crowdService.authenticate(bradB.getName(), "newPassword"));

            crowdService.authenticate(bradB.getName(), "simple");
            fail("This should not have authenticated");
        }
        catch (FailedAuthenticationException e)
        {
            assertTrue(true);
        }
        mockController.verify();
    }

    @Override
    protected void tearDown() throws Exception
    {
        CoreTransactionUtil.setUseTransactions(false);
        super.tearDown();
    }
}
