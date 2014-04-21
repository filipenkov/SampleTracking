package com.atlassian.jira.bc.user.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.easymock.MockControl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * This is an ugly JiraMockTestCase.
 *
 * Try to put future tests in {@link TestDefaultUserPickerSearchService} without the kitchen sink!
 */
public class TestUserPickerSearchService extends LegacyJiraMockTestCase
{
    private UserManager userUtil;
    private ApplicationProperties applicationProperites;
    private DefaultUserPickerSearchService searchService;
    private JiraServiceContext jiraCtx;
    private User executingUser;
    private Mock mockUserManager;

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mockUserManager = new Mock(UserManager.class);
        userUtil = (UserManager) mockUserManager.proxy();
        applicationProperites = ComponentAccessor.getApplicationProperties();

        final Mock mockPM = new Mock(PermissionManager.class);
        mockPM.expectAndReturn("hasPermission", P.args(P.eq(Permissions.USER_PICKER), P.IS_NOT_NULL), Boolean.TRUE);
        final PermissionManager permissionManager = (PermissionManager) mockPM.proxy();

        searchService = new DefaultUserPickerSearchService(userUtil, applicationProperites, permissionManager);

        executingUser = createMockUser("Executor");
        jiraCtx = new JiraServiceContextImpl(executingUser, new SimpleErrorCollection());
    }

    public void testEmptyResults()
    {

        createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, });

        mockUserManager.expectAndReturn("getUsers", Collections.emptySet());
        final List<User> results = searchService.findUsers(jiraCtx, "anything");
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    public void testNullQuery()
    {

        createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, });

        final List<User> results = searchService.findUsers(jiraCtx, null);
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    public void testEmptyQuery()
    {

        createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, });

        final List<User> results = searchService.findUsers(jiraCtx, "");
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    public void testWhitespaceOnlyQuery()
    {

        createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, });

        final List<User> results = searchService.findUsers(jiraCtx, "    ");
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    public void testSingleMatch()
    {
        mockUserManager.expectAndReturn("getUsers", createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, }));

        final List<User> results = searchService.findUsers(jiraCtx, "User1");
        assertNotNull(results);
        assertEquals(1, results.size());

        final Iterator<User> iter = results.iterator();

        final User resultUser = iter.next();
        assertNotNull(resultUser);
        assertEquals("User1", resultUser.getName());
    }

    public void testSingleMatchWithMoreUsers()
    {
        mockUserManager.expectAndReturn("getUsers",
        createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, { "User2", "User2 FullName", "user2@somewhere.com" }, { "User3", "User3 FullName", "user3@somewhere.com" }, }));

        List<User> results = searchService.findUsers(jiraCtx, "User2");
        assertNotNull(results);
        assertEquals(1, results.size());

        Iterator<User> iter = results.iterator();

        User resultUser = iter.next();
        assertNotNull(resultUser);
        assertEquals("User2", resultUser.getName());

        results = searchService.findUsers(jiraCtx, "User3");
        assertNotNull(results);
        assertEquals(1, results.size());

        iter = results.iterator();

        resultUser = iter.next();
        assertNotNull(resultUser);
        assertEquals("User3", resultUser.getName());

        results = searchService.findUsers(jiraCtx, "UserNone");
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    public void testMultipleMatches()
    {
        final Collection usersCreated = createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, { "User2", "User2 FullName", "user2@somewhere.com" }, { "User3", "User3 FullName", "user3@somewhere.com" }, });
        mockUserManager.expectAndReturn("getUsers", usersCreated);

        final Collection results = searchService.findUsers(jiraCtx, "FullName");
        assertNotNull(results);
        assertEquals(3, results.size());

        assertTrue(results.containsAll(usersCreated));

    }

    public void testAjaxEnabledHasPermission()
    {
        createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, { "User2", "User2 FullName", "user2@somewhere.com" }, { "User3", "User3 FullName", "user3@somewhere.com" }, });

        final Mock mockPM = new Mock(PermissionManager.class);
        mockPM.expectAndReturn("hasPermission", P.args(P.eq(Permissions.USER_PICKER), P.eq(executingUser)), Boolean.TRUE);
        final PermissionManager permMgr = (PermissionManager) mockPM.proxy();
        final DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userUtil, applicationProperites, permMgr);
        assertTrue(searchService.canPerformAjaxSearch(jiraCtx));

        mockPM.verify();
    }

    public void testAjaxEnabledHasntPermission()
    {
        createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, { "User2", "User2 FullName", "user2@somewhere.com" }, { "User3", "User3 FullName", "user3@somewhere.com" }, });

        final Mock mockPM = new Mock(PermissionManager.class);
        mockPM.expectAndReturn("hasPermission", P.args(P.eq(Permissions.USER_PICKER), P.eq(executingUser)), Boolean.FALSE);
        final PermissionManager permMgr = (PermissionManager) mockPM.proxy();
        final DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userUtil, applicationProperites, permMgr);
        assertFalse(searchService.canPerformAjaxSearch(jiraCtx));

        mockPM.verify();
    }
    
    public void testCanShowEmailAddressesWhenSHOW()
    {
        final ApplicationProperties ap = getMockEmailProperties("show");
        final PermissionManager pm = getMockPermissionsManager(true);
        final DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userUtil, ap, pm);
        assertTrue(searchService.canShowEmailAddresses(jiraCtx));
    }

    public void testCanShowEmailAddressesWhenMASK()
    {
        final ApplicationProperties ap = getMockEmailProperties("mask");
        final PermissionManager pm = getMockPermissionsManager(true);
        final DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userUtil, ap, pm);
        assertTrue(searchService.canShowEmailAddresses(jiraCtx));
    }

    public void testCanShowEmailAddressesWhenUSER()
    {
        final ApplicationProperties ap = getMockEmailProperties("user");
        final PermissionManager pm = getMockPermissionsManager(true);
        final DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userUtil, ap, pm);
        assertTrue(searchService.canShowEmailAddresses(jiraCtx));
    }

    public void testCannotShowEmailAddressesWhenHIDDEN()
    {
        final ApplicationProperties ap = getMockEmailProperties("hidden");
        final PermissionManager pm = getMockPermissionsManager(true);
        final DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userUtil, ap, pm);
        assertFalse(searchService.canShowEmailAddresses(jiraCtx));
    }

    public void testCannotShowEmailAddressesWhenJUNK()
    {
        final ApplicationProperties ap = getMockEmailProperties(null);
        final PermissionManager pm = getMockPermissionsManager(true);
        final DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userUtil, ap, pm);
        assertFalse(searchService.canShowEmailAddresses(jiraCtx));
    }

    /* ---------------------------------------------------------------------------- */

    private ApplicationProperties getMockEmailProperties(final String type)
    {
        final MockControl mockCtl = MockControl.createControl(ApplicationProperties.class);
        final ApplicationProperties ap = (ApplicationProperties) mockCtl.getMock();
        ap.getDefaultBackedString(APKeys.JIRA_OPTION_EMAIL_VISIBLE);
        mockCtl.setReturnValue(type);
        mockCtl.replay();

        return ap;
    }

    private PermissionManager getMockPermissionsManager(final boolean canPickUsers)
    {
        final Mock mockPM = new Mock(PermissionManager.class);
        mockPM.expectAndReturn("hasPermission", P.args(P.eq(Permissions.USER_PICKER), P.eq(executingUser)), new Boolean(canPickUsers));
        final PermissionManager permMgr = (PermissionManager) mockPM.proxy();
        return permMgr;
    }

    private Collection<User> createUsers(final String[][] userDetails)
    {
        final Collection<User> userList = new HashSet<User>();
        for (final String[] userDetail : userDetails)
        {
            final String username = userDetail[0];
            final String fullName = userDetail[1];
            final String email = userDetail[2];
            final User testUser = new MockUser(username, fullName, email);
            
            userList.add(testUser);
        }
        return userList;
    }
}
