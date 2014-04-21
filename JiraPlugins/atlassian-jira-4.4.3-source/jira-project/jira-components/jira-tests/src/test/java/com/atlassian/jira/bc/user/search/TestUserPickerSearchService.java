package com.atlassian.jira.bc.user.search;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.User;
import org.easymock.MockControl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

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
    private Mock mockUserUtil;

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        UtilsForTests.cleanUsers();
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mockUserUtil = new Mock(UserManager.class);
        userUtil = (UserManager) mockUserUtil.proxy();
        applicationProperites = ComponentAccessor.getApplicationProperties();

        final Mock mockPM = new Mock(PermissionManager.class);
        mockPM.expectAndReturn("hasPermission", P.args(P.eq(Permissions.USER_PICKER), P.IS_NOT_NULL), Boolean.TRUE);
        final PermissionManager permissionManager = (PermissionManager) mockPM.proxy();

        searchService = new DefaultUserPickerSearchService(userUtil, applicationProperites, permissionManager);

        executingUser = UtilsForTests.getTestUser("Executor");
        jiraCtx = new JiraServiceContextImpl(executingUser, new SimpleErrorCollection());
    }

    public void testUserMatch()
    {
        final User testUser = UtilsForTests.getTestUser("Tester");
        testUser.setFullName("FirstTest LastTest");
        testUser.setEmail("this.tester@atlassian.com");

        assertTrue(searchService.userMatches(testUser, "Test", true));
        assertTrue(searchService.userMatches(testUser, "Tester", true));
        assertFalse(searchService.userMatches(testUser, "Testing", true));
        assertTrue(searchService.userMatches(testUser, "test", true));
        assertTrue(searchService.userMatches(testUser, "TEST", true));
        assertTrue(searchService.userMatches(testUser, "First", true));
        assertTrue(searchService.userMatches(testUser, "Last", true));
        assertTrue(searchService.userMatches(testUser, "LastTest", true));
        assertTrue(searchService.userMatches(testUser, "Lasttest", true));
        assertTrue(searchService.userMatches(testUser, "FirstTest LastTest", true));
        assertTrue(searchService.userMatches(testUser, "this", true));
        assertTrue(searchService.userMatches(testUser, "this.test", true));
        assertTrue(searchService.userMatches(testUser, "this.tester@atlassian.com", true));

        assertFalse(searchService.userMatches(testUser, "not", true));
        assertFalse(searchService.userMatches(testUser, "tester@atlassian.com", true));
        assertFalse(searchService.userMatches(testUser, "atlassian.com", true));
        assertFalse(searchService.userMatches(testUser, ".com", true));
        assertFalse(searchService.userMatches(testUser, "@", true));
        assertFalse(searchService.userMatches(testUser, "atlas", true));

        assertFalse(searchService.userMatches(testUser, "this", false));
        assertFalse(searchService.userMatches(testUser, "this.tester", false));
        assertFalse(searchService.userMatches(testUser, "this.tester@atlassian.com", false));

    }

    public void testEmptyResults()
    {

        createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, });

        mockUserUtil.expectAndReturn("getAllUsers", Collections.emptySet());
        final Collection results = searchService.getResults(jiraCtx, "anything");
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    public void testNullQuery()
    {

        createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, });

        final Collection results = searchService.getResults(jiraCtx, null);
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    public void testEmptyQuery()
    {

        createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, });

        final Collection results = searchService.getResults(jiraCtx, "");
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    public void testWhitespaceOnlyQuery()
    {

        createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, });

        final Collection results = searchService.getResults(jiraCtx, "    ");
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    public void testSingleMatch()
    {
        mockUserUtil.expectAndReturn("getAllUsers", createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, }));

        final Collection results = searchService.getResults(jiraCtx, "User1");
        assertNotNull(results);
        assertEquals(1, results.size());

        final Iterator iter = results.iterator();

        final Object userObj = iter.next();
        assertNotNull(userObj);
        assertTrue(userObj instanceof User);

        final User resultUser = (User) userObj;
        assertEquals("User1", resultUser.getName());
    }

    public void testSingleMatchWithMoreUsers()
    {
        mockUserUtil.expectAndReturn("getAllUsers",
        createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, { "User2", "User2 FullName", "user2@somewhere.com" }, { "User3", "User3 FullName", "user3@somewhere.com" }, }));

        Collection results = searchService.getResults(jiraCtx, "User2");
        assertNotNull(results);
        assertEquals(1, results.size());

        Iterator iter = results.iterator();

        Object userObj = iter.next();
        assertNotNull(userObj);
        assertTrue(userObj instanceof User);
        User resultUser = (User) userObj;
        assertEquals("User2", resultUser.getName());

        results = searchService.getResults(jiraCtx, "User3");
        assertNotNull(results);
        assertEquals(1, results.size());

        iter = results.iterator();

        userObj = iter.next();
        assertNotNull(userObj);
        assertTrue(userObj instanceof User);
        resultUser = (User) userObj;
        assertEquals("User3", resultUser.getName());

        results = searchService.getResults(jiraCtx, "UserNone");
        assertNotNull(results);
        assertEquals(0, results.size());

    }

    public void testMultipleMatches()
    {
        final Collection usersCreated = createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, { "User2", "User2 FullName", "user2@somewhere.com" }, { "User3", "User3 FullName", "user3@somewhere.com" }, });
        mockUserUtil.expectAndReturn("getAllUsers", usersCreated);

        final Collection results = searchService.getResults(jiraCtx, "FullName");
        assertNotNull(results);
        assertEquals(3, results.size());

        assertTrue(results.containsAll(usersCreated));

    }

    public void testAjaxEnabledHasPermission()
    {
        createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, { "User2", "User2 FullName", "user2@somewhere.com" }, { "User3", "User3 FullName", "user3@somewhere.com" }, });

        final Mock mockAP = new Mock(ApplicationProperties.class);
        mockAP.expectAndReturn("getDefaultBackedString", P.args(P.eq(APKeys.JIRA_AJAX_USER_PICKER_LIMIT)), "10");
        final ApplicationProperties applicationProperites = (ApplicationProperties) mockAP.proxy();

        final Mock mockPM = new Mock(PermissionManager.class);
        mockPM.expectAndReturn("hasPermission", P.args(P.eq(Permissions.USER_PICKER), P.eq(executingUser)), Boolean.TRUE);
        final PermissionManager permMgr = (PermissionManager) mockPM.proxy();
        final DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userUtil, applicationProperites, permMgr);
        assertTrue(searchService.canPerformAjaxSearch(jiraCtx));

        mockAP.verify();
        mockPM.verify();
    }

    public void testAjaxEnabledHasntPermission()
    {
        createUsers(new String[][] { { "User1", "User1 FullName", "user1@somewhere.com" }, { "User2", "User2 FullName", "user2@somewhere.com" }, { "User3", "User3 FullName", "user3@somewhere.com" }, });

        final Mock mockAP = new Mock(ApplicationProperties.class);
        mockAP.expectAndReturn("getDefaultBackedString", P.args(P.eq(APKeys.JIRA_AJAX_USER_PICKER_LIMIT)), "10");
        final ApplicationProperties applicationProperites = (ApplicationProperties) mockAP.proxy();

        final Mock mockPM = new Mock(PermissionManager.class);
        mockPM.expectAndReturn("hasPermission", P.args(P.eq(Permissions.USER_PICKER), P.eq(executingUser)), Boolean.FALSE);
        final PermissionManager permMgr = (PermissionManager) mockPM.proxy();
        final DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userUtil, applicationProperites, permMgr);
        assertFalse(searchService.canPerformAjaxSearch(jiraCtx));

        mockAP.verify();
        mockPM.verify();
    }

    public void testAjaxDisabled()
    {
        mockUserUtil.expectAndReturn("getTotalUserCount", 3);

        final Mock mockAP = new Mock(ApplicationProperties.class);
        mockAP.expectAndReturn("getDefaultBackedString", P.args(P.eq(APKeys.JIRA_AJAX_USER_PICKER_LIMIT)), "2");
        final ApplicationProperties applicationProperites = (ApplicationProperties) mockAP.proxy();

        final DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userUtil, applicationProperites, null);
        assertFalse(searchService.canPerformAjaxSearch(null));

        mockAP.verify();
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

    public void testIsAjaxSearchEnabled()
    {
        mockUserUtil.expectAndReturn("getTotalUserCount", 3);

        final MockControl mockCtl = MockControl.createControl(ApplicationProperties.class);
        final ApplicationProperties ap = (ApplicationProperties) mockCtl.getMock();
        ap.getDefaultBackedString(APKeys.JIRA_AJAX_USER_PICKER_LIMIT);
        mockCtl.setReturnValue("5");
        mockCtl.replay();
        final DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userUtil, ap, null);
        assertTrue(searchService.isAjaxSearchEnabled());

        // now lower the threshold to 2 and see that the number of users exceeds the limit and therefore the thing is off
        mockCtl.reset();
        ap.getDefaultBackedString(APKeys.JIRA_AJAX_USER_PICKER_LIMIT);
        mockCtl.setReturnValue("2");
        mockCtl.replay();
        assertFalse(searchService.isAjaxSearchEnabled());
    }

    /* ---------------------------------------------------------------------------- */

    private ApplicationProperties getMockEmailProperties(final String type)
    {
        final MockControl mockCtl = MockControl.createControl(ApplicationProperties.class);
        final ApplicationProperties ap = (ApplicationProperties) mockCtl.getMock();
        ap.getDefaultBackedString(APKeys.JIRA_AJAX_USER_PICKER_LIMIT);
        mockCtl.setReturnValue("999");
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
            final User testUser = UtilsForTests.getTestUser(userDetail[0]);
            testUser.setFullName(userDetail[1]);
            testUser.setEmail(userDetail[2]);

            userList.add(testUser);
        }
        return userList;
    }
}
