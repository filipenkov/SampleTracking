package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.UserProfile;
import com.atlassian.jira.functest.framework.UserProfileImpl;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigator.IssueTypeCondition;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearchBuilder;
import com.atlassian.jira.functest.framework.navigator.QuerySearchCondition;
import com.atlassian.jira.functest.framework.parser.filter.FilterItem;
import com.atlassian.jira.functest.framework.parser.filter.FilterList;
import com.atlassian.jira.functest.framework.parser.filter.FilterParser;
import com.atlassian.jira.functest.framework.sharing.GlobalTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.GroupTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.ProjectTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.SimpleTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.json.TestJSONException;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Do a test of the Save and SaveAs functionality of navigator. This test focuses on a Sharing functionality.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestSaveAsFilterSharing extends FuncTestCase
{
    private static final String NO_SHARE_USER = "user_cant_share_filters";
    private static final String SHARE_USER = "user_can_share_filters";

    private static final String SHARE_GROUP = "group_share_filters_with_me";

    private static final String ADMINISTRATOR_GROUP = "jira-administrators";
    private static final long HOMOSAPIEN_PROJECT_ID = 10000;
    private static final long SHARE_ROLE_ID = 10004;

    private static final long HOMOSAPIEN_ROLE_ID = 10005;
    private static final NavigatorSearch NAV_SEARCH2;

    private static final NavigatorSearch NAV_SEARCH1;
    private static final long PRIVATE_CANT_SHARE_ID = 10022;
    private static final NavigatorSearch PRIVATE_CANT_SHARE;

    private static final SharedEntityInfo PRIVATE_CANT_SHARE_INFO = new SharedEntityInfo("PrivateHumanCantShare", "Cant share this.", true, null);
    private static final long PUBLIC_BUG_SHARE_ID = 10020;
    private static final NavigatorSearch PUBLIC_BUG_SHARE;

    private static final SharedEntityInfo PUBLIC_BUG_SHARE_INFO = new SharedEntityInfo("PublicBugShare", null, false, null);
    private static final long PRIVATE_BUG_SHARE_ID = 10021;
    private static final NavigatorSearch PRIVATE_BUG_SHARE;
    private static final SharedEntityInfo PRIVATE_BUG_SHARE_INFO = new SharedEntityInfo("PrivateBugShare", null, true, null);
    private static final Pattern FILTER_PATTERN = Pattern.compile("requestId=(\\d+)");
    private static final int NOBROWSE_PROJECT_ID = 10002;
    private static final int NON_EXISTENT_ROLE_ID = 629;
    private static final int NON_EXISTENT_PROJECT_ID = 496;

    static
    {
        NavigatorSearchBuilder builder = new NavigatorSearchBuilder();
        builder.addIssueType(IssueTypeCondition.IssueType.BUG);
        builder.addQueryString("NAV_SEARCH1").addQueryField(QuerySearchCondition.QueryField.SUMMARY);

        NAV_SEARCH1 = builder.createSearch();

        builder = new NavigatorSearchBuilder();
        builder.addQueryString("NAV_SEARCH2").addQueryField(QuerySearchCondition.QueryField.ENVIRONMENT).addQueryField(QuerySearchCondition.QueryField.COMMENTS);

        NAV_SEARCH2 = builder.createSearch();

        builder = new NavigatorSearchBuilder();
        builder.addProject("homosapien");
        builder.addQueryString("human").addQueryField(QuerySearchCondition.QueryField.SUMMARY).addQueryField(QuerySearchCondition.QueryField.COMMENTS);

        PRIVATE_CANT_SHARE = builder.createSearch();

        builder = new NavigatorSearchBuilder();
        builder.addIssueType(IssueTypeCondition.IssueType.BUG);

        PUBLIC_BUG_SHARE = builder.createSearch();

        builder = new NavigatorSearchBuilder();
        builder.addIssueType(IssueTypeCondition.IssueType.BUG);

        PRIVATE_BUG_SHARE = builder.createSearch();
    }

    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("sharedfilters/TestFilterSharing.xml");
    }

    /**
     * Test to make sure that the the sharing option is not displayed on standard.
     */
    public void testSharesOption()
    {
        navigation.login(SHARE_USER);

        navigation.issueNavigator().displayAllIssues();
        tester.clickLinkWithText("Save");
        text.assertTextPresent(new WebPageLocator(tester), "Shares");
    }

    /** Test to ensure that a user without permission to share can still save. */
    public void testSaveNoSharePermission()
    {
        final SharedEntityInfo info = new SharedEntityInfo("TestFilter", null, true, null);

        navigation.login(NO_SHARE_USER);

        navigation.issueNavigator().createSearch(NAV_SEARCH1);
        tester.clickLinkWithText("Save");

        text.assertTextNotPresent(new WebPageLocator(tester), "Shares");
        tester.assertFormElementNotPresent("shareValues");

        tester.setFormElement("filterName", info.getName());
        tester.submit("saveasfilter_submit");

        assertSearchSavedCorrectly(info, NAV_SEARCH1, null);
    }

    /** Test to ensure that a user without permission to share can still save. */
    public void testSaveAsNoSharePermission()
    {
        final SharedEntityInfo info = new SharedEntityInfo("testSaveAsNoSharePermission", null, true, null);

        navigation.login(NO_SHARE_USER);

        navigation.issueNavigator().loadFilter(PRIVATE_CANT_SHARE_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navigation.issueNavigator().modifySearch(NAV_SEARCH2);
        tester.clickLinkWithText("Save as");

        text.assertTextNotPresent(new WebPageLocator(tester), "Shares");
        tester.assertFormElementNotPresent("shareValues");

        tester.setFormElement("filterName", info.getName());
        tester.submit("saveasfilter_submit");

        assertSearchSavedCorrectly(info, NAV_SEARCH2, null);

        navigation.issueNavigator().loadFilter(PRIVATE_CANT_SHARE_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        assertSearchSavedCorrectly(PRIVATE_CANT_SHARE_INFO, PRIVATE_CANT_SHARE, null);
    }

    /**
     * Test to ensure user can copy a shared filter.
     */
    public void testAllowedToSaveSharedFilter()
    {
        final SharedEntityInfo info = new SharedEntityInfo("testAllowedToSaveSharedFilter", null, true, null);

        navigation.login(NO_SHARE_USER);

        navigation.issueNavigator().loadFilter(PUBLIC_BUG_SHARE_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.clickLink("copyasnewfilter");
        tester.assertLinkNotPresentWithText("Save");
        tester.assertLinkNotPresentWithText("Save as");

        text.assertTextNotPresent(new WebPageLocator(tester), "Shares");
        tester.assertFormElementNotPresent("shareValues");

        tester.setFormElement("filterName", info.getName());
        tester.submit("saveasfilter_submit");

        assertSearchSavedCorrectly(info, PUBLIC_BUG_SHARE, null);
    }

    /**
     * Test to make sure the user cannot URL hack to enable sharing when not allowed.
     */
    public void testCannotURLHackSharingSaveAs()
    {
        final SharedEntityInfo info = new SharedEntityInfo("testCannotURLHackSharingSaveAs", null, false, null);

        navigation.login(NO_SHARE_USER);

        navigation.issueNavigator().loadFilter(PRIVATE_CANT_SHARE_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navigation.issueNavigator().modifySearch(NAV_SEARCH2);

        final Set<TestSharingPermission> globalSet = Collections.<TestSharingPermission>singleton(GlobalTestSharingPermission.GLOBAL_PERMISSION);

        tester.gotoPage(createSaveUrl(info.getName(), null, false, globalSet));

        text.assertTextPresent(new WebPageLocator(tester), "You do not have permission to share. All shares are invalid.");
        assertSaveScreenCorrect(info, globalSet);

        navigation.issueNavigator().loadFilter(PRIVATE_CANT_SHARE_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        assertSearchSavedCorrectly(PRIVATE_CANT_SHARE_INFO, PRIVATE_CANT_SHARE, null);

        assertFilterNotOnManage(info);
    }

    /**
     * Test to make sure the user cannot URL hack to enable sharing when not allowed.
     */
    public void testCannotURLHackSharingSave()
    {
        final SharedEntityInfo info = new SharedEntityInfo("testCannotURLHackSharing", null, false, null);

        navigation.login(NO_SHARE_USER);

        navigation.issueNavigator().displayAllIssues();

        final Set<TestSharingPermission> globalSet = new HashSet<TestSharingPermission>();
        globalSet.add(GlobalTestSharingPermission.GLOBAL_PERMISSION);
        globalSet.add(new GroupTestSharingPermission(SHARE_GROUP));

        tester.gotoPage(createSaveUrl(info.getName(), null, false, globalSet));

        text.assertTextPresent(new WebPageLocator(tester), "You do not have permission to share. All shares are invalid.");
        assertSaveScreenCorrect(info, globalSet);

        assertFilterNotOnManage(info);
    }

    /**
     * Test to make sure that empty share list can be saved when you don't have share permissions.
     */
    public void testSaveEmptyShares()
    {
        navigation.login(NO_SHARE_USER);

        final SharedEntityInfo info = new SharedEntityInfo("testSaveEmptyShares", null, false, null);

        createAndCheckFilter(info, NAV_SEARCH1, null);
    }

    /**
     * Test to make sure that empty share list can be saved when you don't have share permissions.
     */
    public void testSaveAsEmptyShares()
    {
        navigation.login(NO_SHARE_USER);

        navigation.issueNavigator().loadFilter(PRIVATE_CANT_SHARE_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navigation.issueNavigator().modifySearch(NAV_SEARCH1);

        final SharedEntityInfo info = new SharedEntityInfo("testSaveAsEmptyShares", null, false, null);

        saveFilter(info, Collections.<TestSharingPermission>emptySet());
        assertSearchSavedCorrectly(info, NAV_SEARCH1, null);

        navigation.issueNavigator().loadFilter(PRIVATE_CANT_SHARE_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        assertSearchSavedCorrectly(PRIVATE_CANT_SHARE_INFO, PRIVATE_CANT_SHARE, null);
    }

    /**
     * Make sure that the default permission is used when the user has no preferences.
     */
    public void testSaveDefaultPriavtePermissions()
    {
        final UserProfile profile = new UserProfileImpl(tester, environmentData, navigation);
        navigation.login(ADMIN_USERNAME);

        profile.changeDefaultSharingType(false);

        navigation.login(SHARE_USER);

        navigation.issueNavigator().createSearch(NAV_SEARCH1);
        tester.clickLinkWithText("Save");

        assertSaveScreenCorrect(new SharedEntityInfo(null, null, true, null), Collections.<TestSharingPermission>emptySet());
    }

    /**
     * Make sure that the default permission is used when the user has no preferences.
     */
    public void testSaveAsDefaultPublicPermissions()
    {
        final UserProfile profile = new UserProfileImpl(tester, environmentData, navigation);
        navigation.login(ADMIN_USERNAME);

        profile.changeDefaultSharingType(true);

        navigation.login(SHARE_USER);

        navigation.issueNavigator().loadFilter(PUBLIC_BUG_SHARE_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navigation.issueNavigator().modifySearch(NAV_SEARCH2);

        tester.clickLinkWithText("Save as");

        assertSaveScreenCorrect(new SharedEntityInfo(null, null, true, null), TestSharingPermissionUtils.createPublicPermissions());

        navigation.issueNavigator().loadFilter(PUBLIC_BUG_SHARE_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        assertSearchSavedCorrectly(PUBLIC_BUG_SHARE_INFO, PUBLIC_BUG_SHARE, TestSharingPermissionUtils.createPublicPermissions());
    }

    /**
     * Test to make sure that public permissions are displayed for a user with sharing by default.
     */
    public void testSaveUserPublicPermissions()
    {
        final UserProfile profile = new UserProfileImpl(tester, environmentData, navigation);
        navigation.login(ADMIN_USERNAME);

        profile.changeDefaultSharingType(false);

        navigation.logout();

        navigation.login(SHARE_USER);

        profile.changeUserSharingType(true);

        navigation.issueNavigator().createSearch(NAV_SEARCH1);
        tester.clickLinkWithText("Save");

        assertSaveScreenCorrect(new SharedEntityInfo(null, null, true, null), TestSharingPermissionUtils.createPublicPermissions());
    }

    /**
     * Test to make sure that private permissions are displayed for a user with private by default.
     */
    public void testSaveAsUserPrivatePermissions()
    {
        final UserProfile profile = new UserProfileImpl(tester, environmentData, navigation);
        navigation.login(ADMIN_USERNAME);

        profile.changeDefaultSharingType(true);

        navigation.login(SHARE_USER);

        profile.changeUserSharingType(false);

        navigation.issueNavigator().loadFilter(PUBLIC_BUG_SHARE_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navigation.issueNavigator().modifySearch(NAV_SEARCH2);

        tester.clickLinkWithText("Save as");

        assertSaveScreenCorrect(new SharedEntityInfo(null, null, true, null), Collections.<TestSharingPermission>emptySet());

        navigation.issueNavigator().loadFilter(PUBLIC_BUG_SHARE_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        assertSearchSavedCorrectly(PUBLIC_BUG_SHARE_INFO, PUBLIC_BUG_SHARE, TestSharingPermissionUtils.createPublicPermissions());
    }

    /**
     * Test to make sure that we can share globally.
     */
    public void testSaveGlobalPermission()
    {
        navigation.login(SHARE_USER);

        final SharedEntityInfo info = new SharedEntityInfo("testSaveGlobalPermission", null, false, null);

        createAndCheckFilter(info, NAV_SEARCH2, Collections.<TestSharingPermission>singleton(GlobalTestSharingPermission.GLOBAL_PERMISSION));
    }

    /**
     * Test to make sure that we can share globally.
     */

    public void testSaveAsGlobalPermission()
    {
        navigation.login(SHARE_USER);

        final SharedEntityInfo info = new SharedEntityInfo("testSaveAsGlobalPermission", null, true, null);

        modifyAndCheckFilter(info, NAV_SEARCH2, Collections.<TestSharingPermission>singleton(GlobalTestSharingPermission.GLOBAL_PERMISSION));
    }

    /**
     * Test to make sure that we can share with group.
     */
    public void testSaveGroupPermission()
    {
        navigation.login(SHARE_USER);

        final SharedEntityInfo info = new SharedEntityInfo("testSaveGroupPermission", null, false, null);

        createAndCheckFilter(info, NAV_SEARCH2, Collections.<TestSharingPermission>singleton(new GroupTestSharingPermission(SHARE_GROUP)));
    }

    /**
     * Test to make sure that we can share with group.
     */

    public void testSaveAsGroupPermission()
    {
        navigation.login(SHARE_USER);

        final SharedEntityInfo info = new SharedEntityInfo("testSaveAsGroupPermission", null, false, null);

        modifyAndCheckFilter(info, NAV_SEARCH2, Collections.<TestSharingPermission>singleton(new GroupTestSharingPermission(SHARE_GROUP)));
    }

    /**
     * Test to make sure we can save project permissions.
     */
    public void testSaveProjectPermission()
    {
        navigation.login(SHARE_USER);

        final SharedEntityInfo info = new SharedEntityInfo("testSaveProjectPermission", null, false, null);

        createAndCheckFilter(info, NAV_SEARCH2, Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(HOMOSAPIEN_PROJECT_ID)));
    }

    /**
     * Test to make sure we can save project permissions.
     */
    public void testSaveAsProjectPermission()
    {
        navigation.login(SHARE_USER);

        final SharedEntityInfo info = new SharedEntityInfo("testSaveAsProjectPermission", null, false, null);

        modifyAndCheckFilter(info, NAV_SEARCH2, Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(HOMOSAPIEN_PROJECT_ID)));
    }

    /**
     * Test to make sure we can save role permissions.
     */
    public void testSaveRolePermission()
    {
        navigation.login(SHARE_USER);

        final SharedEntityInfo info = new SharedEntityInfo("testSaveRolePermission", null, false, null);

        createAndCheckFilter(info, NAV_SEARCH2, Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(HOMOSAPIEN_PROJECT_ID, SHARE_ROLE_ID)));
    }

    /**
     * Test to make sure we can save role permissions.
     */
    public void testSaveAsRolePermission()
    {
        navigation.login(SHARE_USER);

        final SharedEntityInfo info = new SharedEntityInfo("testSaveAsRolePermission", null, true, null);

        modifyAndCheckFilter(info, NAV_SEARCH2, Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(HOMOSAPIEN_PROJECT_ID, SHARE_ROLE_ID)));
    }

    /**
     * Test to make sure we can save multiple permissions.
     */
    public void testSaveMultiplePermission()
    {
        navigation.login(SHARE_USER);

        Set<TestSharingPermission> shares = new HashSet<TestSharingPermission>();
        shares.add(new ProjectTestSharingPermission(HOMOSAPIEN_PROJECT_ID));
        shares.add(new ProjectTestSharingPermission(HOMOSAPIEN_PROJECT_ID, SHARE_ROLE_ID));

        final SharedEntityInfo info = new SharedEntityInfo("testSaveMultiplePermission", null, false, null);

        createAndCheckFilter(info, NAV_SEARCH2, shares);
    }

    /**
     * Test to make sure we can save multiple permissions.
     */
    public void testSaveAsMultiplePermission()
    {
        navigation.login(SHARE_USER);

        Set<TestSharingPermission> shares = new HashSet<TestSharingPermission>();
        shares.add(new ProjectTestSharingPermission(HOMOSAPIEN_PROJECT_ID));
        shares.add(new ProjectTestSharingPermission(HOMOSAPIEN_PROJECT_ID, SHARE_ROLE_ID));
        shares.add(new GroupTestSharingPermission(SHARE_GROUP));

        final SharedEntityInfo info = new SharedEntityInfo("testSaveAsMultiplePermission", null, true, null);

        modifyAndCheckFilter(info, NAV_SEARCH2, shares);
    }

    /**
     * Test to make sure that JIRA does not allow global permission with other permissions.
     */
    public void testSaveInvalidGlobal()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = new HashSet<TestSharingPermission>();
        shares.add(new GroupTestSharingPermission(SHARE_GROUP));
        shares.add(GlobalTestSharingPermission.GLOBAL_PERMISSION);

        checkCannotSaveInvalidPermissions(shares, "Permission type 'global' must not be included with other permissions.");
    }

    /**
     * Test to make sure that JIRA does not allow global permission with other permissions.
     */
    public void testSaveAsInvalidGlobal()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = new HashSet<TestSharingPermission>();
        shares.add(new GroupTestSharingPermission(SHARE_GROUP));
        shares.add(GlobalTestSharingPermission.GLOBAL_PERMISSION);

        checkCannotSaveAsInvalidPermissions(shares, "Permission type 'global' must not be included with other permissions.");
    }

    /**
     * Test to make sure that JIRA does not accept invalid group permissions.
     */
    public void testSaveInvalidGroup()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new GroupTestSharingPermission("qqqqqaaa"));

        checkCannotSaveInvalidPermissions(shares, "Group: 'qqqqqaaa' does not exist.");
    }

    /**
     * Test to make sure that JIRA does not accept invalid group permissions.
     */
    public void testSaveAsInvalidGroup()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new GroupTestSharingPermission(""));

        checkCannotSaveAsInvalidPermissions(shares, "Group permission is not valid: Invalid group name ''");
    }

    /**
     * Test to make sure that JIRA does not allow to share with group not a member of.
     */

    public void testSaveNotMemberGroup()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new GroupTestSharingPermission(ADMINISTRATOR_GROUP));

        checkCannotSaveAsInvalidPermissions(shares, "You do not have permission to share with Group: 'jira-administrators'.");
    }

    /**
     * Test to make sure that JIRA does not allow to share with group not a member of.
     */
    public void testSaveAsNotMemberGroup()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new GroupTestSharingPermission(ADMINISTRATOR_GROUP));

        checkCannotSaveAsInvalidPermissions(shares, "You do not have permission to share with Group: 'jira-administrators'.");
    }

    /**
     * Test to make sure that JIRA does not allow sharing with invalid projects.
     */

    public void testSaveInvalidProjectId()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new SimpleTestSharingPermission("project", "abc", null));

        checkCannotSaveInvalidPermissions(shares, "Project permission is not valid: Invalid project identifier 'abc'.");
    }

    /**
     * Test to make sure that JIRA does not allow sharing with invalid projects.
     */

    public void testSaveAsInvalidProjectId()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(-1));

        checkCannotSaveAsInvalidPermissions(shares, "Project permission is not valid: Invalid project identifier ''.");
    }

    /**
     * Test to make sure that JIRA does not allow project that does not exist.
     */
    public void testSaveProjectDoesNotExist()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(NON_EXISTENT_PROJECT_ID));

        checkCannotSaveInvalidPermissions(shares, "Selected project does not exist.");
    }

    /**
     * Test to make sure that JIRA does not allow project that does not exist.
     */
    public void testSaveAsProjectDoesNotExist()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(NON_EXISTENT_PROJECT_ID));

        checkCannotSaveAsInvalidPermissions(shares, "Selected project does not exist.");
    }

    /**
     * Test to make sure that you can't share with a project without browse permission.
     */
    public void testSaveProjectNoBrowse()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(NOBROWSE_PROJECT_ID));

        checkCannotSaveInvalidPermissions(shares, "You do not have permission to share with Project: '");
    }

    /**
     * Test to make sure that you can't share with a project without browse permission.
     */
    public void testSaveAsProjectNoBrowse()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(NOBROWSE_PROJECT_ID));

        checkCannotSaveAsInvalidPermissions(shares, "You do not have permission to share with Project: '");
    }

    /**
     * Test to make sure JIRA deals with Invalid roles.
     */
    public void testSaveInvalidRoleId()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new SimpleTestSharingPermission("project", "" + HOMOSAPIEN_PROJECT_ID, "abc"));

        checkCannotSaveInvalidPermissions(shares, "Project permission is not valid: Invalid role identifier 'abc'.");
    }

    /**
     * Test to make sure JIRA deals with Invalid roles.
     */
    public void testSaveAsInvalidRoleId()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new SimpleTestSharingPermission("project", "" + HOMOSAPIEN_PROJECT_ID, "-300s"));

        checkCannotSaveAsInvalidPermissions(shares, "Project permission is not valid: Invalid role identifier '-300s'.");
    }

    /**
     * Test to make sure JIRA does not allow you to share with roles that don't exist.
     */
    public void testSaveRoleDoesNotExist()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(HOMOSAPIEN_PROJECT_ID, NON_EXISTENT_ROLE_ID));

        checkCannotSaveInvalidPermissions(shares, "Selected role does not exist.");
    }

    /**
     * Test to make sure JIRA does not allow you to share with roles that don't exist.
     */
    public void testSaveAsRoleDoesNotExist()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(HOMOSAPIEN_PROJECT_ID, NON_EXISTENT_ROLE_ID));

        checkCannotSaveAsInvalidPermissions(shares, "Selected role does not exist.");
    }

    /**
     * Test to make sure JIRA does not allow you to share with role user is not a member of.
     */
    public void testSaveRoleNotMember()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(HOMOSAPIEN_PROJECT_ID, HOMOSAPIEN_ROLE_ID));

        checkCannotSaveInvalidPermissions(shares, "You do not have permission to share with Project:");
    }

    /**
     * Test to make sure JIRA does not allow you to share with role user is not a member of.
     */
    public void testSaveAsRoleNotMember()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(HOMOSAPIEN_PROJECT_ID, HOMOSAPIEN_ROLE_ID));

        checkCannotSaveAsInvalidPermissions(shares, "You do not have permission to share with Project:");
    }

    /**
     * Make sure JIRA handles JSON object without type.
     */
    public void testSaveInvalidShareType()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = new HashSet<TestSharingPermission>();
        shares.add(new SimpleTestSharingPermission((String)null));
        shares.add(GlobalTestSharingPermission.GLOBAL_PERMISSION);

        checkCannotSaveInvalidPermissions(shares, "Unable to parse shares.", Collections.<TestSharingPermission>emptySet());
    }

    /**
     * Make sure JIRA handles JSON object without type.
     */
    public void testSaveAsInvalidShareType()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = new HashSet<TestSharingPermission>();
        shares.add(new SimpleTestSharingPermission((String)null));
        shares.add(GlobalTestSharingPermission.GLOBAL_PERMISSION);

        checkCannotSaveAsInvalidPermissions(shares, "Unable to parse shares.", Collections.<TestSharingPermission>emptySet());
    }

    /**
     * Make sure JIRA handles invalid JSON share types.
     */
    public void testSaveInvalidJSON()
    {
        navigation.login(SHARE_USER);

        navigation.issueNavigator().displayAllIssues();

        tester.gotoPage("secure/SaveAsFilter.jspa?filterName=testInvalidJSON&shareValues=dsajsksdja");

        text.assertTextPresent(new WebPageLocator(tester), "Unable to parse shares.");
    }

    /**
     * Make sure JIRA handles invalid JSON share types.
     */
    public void testSaveAsInvalidJSON()
    {
        navigation.login(SHARE_USER);

        navigation.issueNavigator().loadFilter(PUBLIC_BUG_SHARE_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navigation.issueNavigator().modifySearch(NAV_SEARCH1);

        tester.gotoPage("secure/SaveAsFilter.jspa?filterName=testInvalidJSON&shareValues=dsajsksdja");

        text.assertTextPresent(new WebPageLocator(tester), "Unable to parse shares.");

        navigation.issueNavigator().loadFilter(PUBLIC_BUG_SHARE_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        assertSearchSavedCorrectly(PUBLIC_BUG_SHARE_INFO, PUBLIC_BUG_SHARE,
                TestSharingPermissionUtils.createPublicPermissions());
    }

    /**
     * Make sure JIRA handles ShareType that does not exist.
     */
    public void testSaveInvalidShareTypeDoesNotExist()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new SimpleTestSharingPermission("blah", null, null));

        checkCannotSaveInvalidPermissions(shares, "Share permission of type 'blah' is unknown.");
    }

    /**
     * Make sure JIRA handles a ShareType that does not exist.
     */
    public void testSaveAsInvalidShareTypeDoesNotExist()
    {
        navigation.login(SHARE_USER);

        final Set<TestSharingPermission> shares = Collections.<TestSharingPermission>singleton(new SimpleTestSharingPermission("blah", null, null));

        checkCannotSaveAsInvalidPermissions(shares, "Share permission of type 'blah' is unknown.");
    }

    /**
     * Try and save a filter with the passed shares. This save should fail with the passed error message.
     *
     * @param shares the shares to try and save.
     * @param assertText the error message to assert exists.
     */

    public void checkCannotSaveInvalidPermissions(final Set<TestSharingPermission> shares, final String assertText)
    {
        checkCannotSaveInvalidPermissions(shares, assertText, shares);
    }

    /**
     * Try and save a filter with the passed shares. This save should fail with the passed error message.
     *
     * @param shares the shares to try and save.
     * @param assertText the error message to assert exists.
     * @param expectedPermissions the permissions that should be displayed on the page.
     */
    private void checkCannotSaveInvalidPermissions(final Set<TestSharingPermission> shares, final String assertText,
            final Set<TestSharingPermission> expectedPermissions)
    {
        final SharedEntityInfo info = new SharedEntityInfo("cannotSaveInvalidPermissions", null, true, null);

        navigation.issueNavigator().createSearch(NAV_SEARCH1);
        saveFilterNoId(info, shares);

        text.assertTextPresent(new WebPageLocator(tester), assertText);
        assertEquals(expectedPermissions, parsePermissions());

        assertFilterNotOnManage(info);
    }

    /**
     * Try and save a modified filter with the passed shares. This save should fail with the passed error message.
     *
     * @param shares the shares to try and save.
     * @param assertText the error message to assert exists.
     */

    private void checkCannotSaveAsInvalidPermissions(final Set<TestSharingPermission> shares, final String assertText)
    {
        checkCannotSaveAsInvalidPermissions(shares, assertText, shares);
    }

    /**
     * Try and save a modified filter with the passed shares. This save should fail with the passed error message.
     *
     * @param shares the shares to try and save.
     * @param assertText the error message to assert exists.
     * @param expectedShares the shares that are expected to be returned.
     */

    private void checkCannotSaveAsInvalidPermissions(final Set<TestSharingPermission> shares,
            final String assertText, final Set<TestSharingPermission> expectedShares)
    {
        final SharedEntityInfo info = new SharedEntityInfo("cannotSaveAsInvalidPermissions", null, true, null);

        navigation.issueNavigator().loadFilter(PRIVATE_BUG_SHARE_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navigation.issueNavigator().modifySearch(NAV_SEARCH2);
        saveAsFilterNoId(info, shares);

        text.assertTextPresent(new WebPageLocator(tester), assertText);
        assertSaveScreenCorrect(info, expectedShares);
        assertFilterNotOnManage(info);

        navigation.issueNavigator().loadFilter(PRIVATE_BUG_SHARE_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        assertSearchSavedCorrectly(PRIVATE_BUG_SHARE_INFO, PRIVATE_BUG_SHARE, Collections.<TestSharingPermission>emptySet());
    }

    /**
     * Modify the filter and ensure it was saved correctly. Also validates that the original filter remains unchanged.
     *
     * @param info the search information.
     * @param search the search to create and check.
     * @param permissions the permissions to associate with the new filter.
     * @return the id of the filter created.
     */
    private long modifyAndCheckFilter(final SharedEntityInfo info, final NavigatorSearch search,
            final Set <TestSharingPermission> permissions)
    {
        navigation.issueNavigator().loadFilter(PRIVATE_BUG_SHARE_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navigation.issueNavigator().modifySearch(search);
        final long id = saveAsFilter(info, permissions);
        assertSearchSavedCorrectly(info, search, permissions);

        navigation.issueNavigator().loadFilter(PRIVATE_BUG_SHARE_ID, null);
        assertSearchSavedCorrectly(PRIVATE_BUG_SHARE_INFO, PRIVATE_BUG_SHARE, Collections.<TestSharingPermission>emptySet());

        return id;
    }

    /**
     * Create the filter and ensure that it was added correctly.
     *
     * @param info the search info.
     * @param search the search to create and check.
     * @param permissions the permissions to associate with the new filter.
     * @return the id of the filter created.
     */
    private long createAndCheckFilter(final SharedEntityInfo info, final NavigatorSearch search,
            final Set <TestSharingPermission> permissions)
    {
        navigation.issueNavigator().createSearch(search);
        long id = saveFilter(info, permissions);
        assertSearchSavedCorrectly(info, search, permissions);
        return id;
    }

    /**
     * Save the current filter. It does not validate that the saved worked.
     *
     * @param search the info that will be used to save the search.
     * @param permissions the permissions to save along with the filter.
     */
    private void saveFilterNoId(final SharedEntityInfo search, final Set<TestSharingPermission> permissions)
    {
        saveAsFilterNoId(search.getName(), search.getDescription(), search.isFavourite(), permissions, "Save");
    }

    /**
     * Save the current filter. It does not validate that the save worked.
     * @param search the info that will be used to save the search.
     * @param permissions the permissions to save along with the filter.
     */
    private void saveAsFilterNoId(final SharedEntityInfo search, final Set<TestSharingPermission> permissions)
    {
        saveAsFilterNoId(search.getName(), search.getDescription(), search.isFavourite(), permissions, "Save as");
    }

    /**
      * Save the passed navigator search.
      *
      * @param search the search to save.
      * @param permissions the permissions to save along with the filter.
      * @return the id of the search to save.
      */
     private long saveFilter(final SharedEntityInfo search, final Set<TestSharingPermission> permissions)
     {
         saveFilterNoId(search, permissions);
         return getFilterIdAfterSave();
     }

     /**
      * Save the passed navigator search.
      *
      * @param search the search to save.
      * @param permissions the permissions to save along with the filter.
      * @return the id of the search to save.
      */
     private long saveAsFilter(final SharedEntityInfo search, final Set<TestSharingPermission> permissions)
     {
         saveAsFilterNoId(search, permissions);
         return getFilterIdAfterSave();
     }

     /**
      * Save the current filter. Assumes you are on the issue navigator screen.
      *
      * @param name the name of the filter.
      * @param description the name of the description.
      * @param favourite should be filter be a favourite.
      * @param permissions the permissions to save.
      * @param linkText the link to click to save the filter.
      */
     private void saveAsFilterNoId(final String name, final String description, final boolean favourite,
             final Set<TestSharingPermission> permissions, final String linkText)
     {
         if (favourite && (permissions == null || permissions.isEmpty()))
         {
             tester.clickLinkWithText(linkText);
             tester.setFormElement("filterName", name);
             if (!StringUtils.isBlank(description))
             {
                 tester.setFormElement("filterDescription", description);
             }
             tester.submit("Save");
         }
         else
         {
             //This is a hack to get around the fact that JWebUnit does not support
             //setting hidden fields. Ahhhh.....

             saveUsingPut(name, description, favourite, permissions);
         }
     }

     /**
      * Save the filter directly using a GET. This gets around the problem where JWebUnit cannot change hidden
      * fields.
      *
      * @param name the name of the search.
      * @param description the description of the search.
      * @param favourite should the filter be saved as a favourite.
      * @param permissions the permissions to save.
      */
     private void saveUsingPut(final String name, final String description, final boolean favourite,
             final Set<TestSharingPermission> permissions)
     {
         tester.gotoPage(createSaveUrl(name, description, favourite, permissions));
     }

     /**
      * Create the URL that can be used to perform a filter save.
      *
      * @param name the name of the search.
      * @param description the description of the search.
      * @param favourite should the filter be saved as a favourite.
      * @param permissions the permissions to save.
      * @return the URL for the filter save.
      */
     private String createSaveUrl(String name, String description, boolean favourite,
             final Set<TestSharingPermission> permissions)
     {
         StringBuilder buffer = new StringBuilder("secure/SaveAsFilter.jspa?submit=Save");
         if (!StringUtils.isBlank(name))
         {
             buffer.append("&filterName=").append(encode(name));
         }
         if (!StringUtils.isBlank(description))
         {
             buffer.append("&filterDescription=").append(encode(description));
         }
         if (permissions != null)
         {
             buffer.append("&shareValues=").append(encode(TestSharingPermissionUtils.createJsonString(permissions)));
         }
         buffer.append("&favourite=").append(String.valueOf(favourite));

         return buffer.toString();
     }

    /**
     * HTML encode the argument.
     *
     * @param data string to encode.
     * @return the encoded string.
     */
     private String encode(String data)
     {
         try
         {
             return URLEncoder.encode(data, "UTF-8");
         }
         catch (UnsupportedEncodingException e)
         {
             throw new RuntimeException(e);
         }
     }

     /**
      * Parse the URL after the filter has been saved to find the new filter id.
      *
      * @return the filter
      */
     private long getFilterIdAfterSave()
     {
         URL url = tester.getDialog().getResponse().getURL();
         if (url.getQuery() == null)
         {
             fail("Unable to save filter: Not redirected to navigator.");
         }
         else
         {
             if (url.getPath() == null || !url.getPath().endsWith("IssueNavigator.jspa"))
             {
                fail("Unable to save filter: Not redirected to navigator.");
             }
             else
             {
                 Matcher matcher = FILTER_PATTERN.matcher(url.getQuery());
                 if (matcher.find())
                 {
                     return Long.parseLong(matcher.group(1));
                 }
                 else
                 {
                     fail("Unable to save filter: Not redirected to navigator.");
                 }
             }
         }
         return Long.MIN_VALUE;
     }

    /**
     * Assert that the current search matches the passed search.
     *
     * @param info the search information to check.
     * @param expectedSearch the search to check.
     * @param expectedPermissions the permissions that should be associated with the search.
     */
    private void assertSearchSavedCorrectly(final SharedEntityInfo info, final NavigatorSearch expectedSearch,
        final Set<TestSharingPermission> expectedPermissions)
    {
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        assertions.getIssueNavigatorAssertions().assertSimpleSearch(expectedSearch, tester);
        assertions.getIssueNavigatorAssertions().assertSearchInfo(info);

        //we need to goto this page to see the shares.
        tester.gotoPage("secure/EditFilter!default.jspa");
        assertEquals("Expected and actual permissions did not match.", expectedPermissions, parsePermissions());

        assertFilterOnManage(info);
    }

    /**
     * Make sure that the passed search is on the manage filter page.
     *
     * @param info the information to check.
     */
    private void assertFilterOnManage(final SharedEntityInfo info)
    {
        navigation.manageFilters().myFilters();
        FilterList list = parse.filter().parseFilterList(FilterParser.TableId.OWNED_TABLE);
        for (FilterItem item : list.getFilterItems())
        {
            if (info.getName().equals(item.getName()))
            {
                if (info.getDescription() == null)
                {
                    assertNull(item.getDescription());
                }
                else
                {
                    assertEquals(info.getDescription(), item.getDescription());
                }
                assertEquals(info.isFavourite(), item.isFav().booleanValue());
                return;
            }
        }
        fail("Filter not displayed on manage filters.");
    }

    /**
     * Make sure that the passed search is not on the manage filter page.
     *
     * @param info the information to check.
     */
    private void assertFilterNotOnManage(final SharedEntityInfo info)
    {
        navigation.manageFilters().myFilters();
        FilterList list = parse.filter().parseFilterList(FilterParser.TableId.OWNED_TABLE);
        for (FilterItem item : list.getFilterItems())
        {
            if (info.getName().equals(item.getName()))
            {
                fail("Filter should not be on the manage filters page.");
            }
        }
    }

    /**
     * Assert that the page contains the correct values on error.
     *
     * @param expectedInfo the information that should currently be on the edit screen.
     * @param expectedPermissions the permission that should be one the screen.
     */
    private void assertSaveScreenCorrect(final SharedEntityInfo expectedInfo,
            final Set<TestSharingPermission> expectedPermissions)
    {
        tester.assertFormElementEquals("filterName", expectedInfo.getName());
        tester.assertFormElementEquals("filterDescription", expectedInfo.getDescription());
        tester.assertFormElementEquals("favourite", String.valueOf(expectedInfo.isFavourite()));
        assertEquals(expectedPermissions, parsePermissions());
    }

    /**
     * Return the current share permissions for the filter in the session.
     *
     * @return the permissions for the current filter.
     */

    private Set<TestSharingPermission> parsePermissions()
    {
        Locator xpath = new XPathLocator(tester, "//span[@id='shares_data']");
        String value = xpath.getText();
        try
        {
            return TestSharingPermissionUtils.parsePermissions(value);
        }
        catch (TestJSONException e)
        {
            fail("Unable to parse shares: " + e.getMessage());
            return null;
        }
    }
}
