package com.atlassian.jira.webtests.ztests.dashboard;

import com.atlassian.jira.functest.framework.Dashboard;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.assertions.DashboardAssertions;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.Arrays;
import java.util.List;

/**
 * Test to ensure that Manage Dashboard display only pages that you have permission to see.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.DASHBOARDS, Category.PERMISSIONS })
public class TestManageDashboardPagePermissions extends FuncTestCase
{
    private static final SharedEntityInfo SYSTEM_PAGE = new SharedEntityInfo(10000L, "System Dashboard", null, false, TestSharingPermissionUtils.createPublicPermissions());
    private static final SharedEntityInfo PUBLIC_PAGE = new SharedEntityInfo(10011L, "Global", null, false, TestSharingPermissionUtils.createPublicPermissions());
    private static final SharedEntityInfo PRIVATE_PAGE = new SharedEntityInfo(10010L, "Private", "Copy of 'System Dashboard'", false, TestSharingPermissionUtils.createPrivatePermissions());
    private static final SharedEntityInfo SHARED_WITH_ADMINS_PAGE = new SharedEntityInfo(10016L, "Shared with Admins", null, false, TestSharingPermissionUtils.createProjectPermissions(0, 0, "Dev Role Browse", "Administrators"));
    private static final SharedEntityInfo SHARED_WITH_DEVELOPERS_PAGE = new SharedEntityInfo(10015L, "Shared with Developers", null, false, TestSharingPermissionUtils.createProjectPermissions(0, 0, "Dev Role Browse", "Developers"));
    private static final SharedEntityInfo SHARED_WITH_JIRA_ADMIN_PAGE = new SharedEntityInfo(10014L, "Shared with jira-admin", null, false, TestSharingPermissionUtils.createGroupPermissions("jira-administrators"));
    private static final SharedEntityInfo SHARED_WITH_JIRA_DEVELOPER_PAGE = new SharedEntityInfo(10013L, "Shared with jira-developer", null, false, TestSharingPermissionUtils.createGroupPermissions("jira-developers"));
    private static final SharedEntityInfo SHARED_WITH_JIRA_USER_PAGE = new SharedEntityInfo(10012L, "Shared with jira-user", null, false, TestSharingPermissionUtils.createGroupPermissions("jira-user"));
    private static final SharedEntityInfo SHARED_WITH_PROJ_ADMINS_PAGE = new SharedEntityInfo(10019L, "Shared with proj Admins", null, false, TestSharingPermissionUtils.createProjectPermissions(0, 0, "Admin Role Browse", null));
    private static final SharedEntityInfo SHARED_WITH_PROJ_DEVELOPERS_PAGE = new SharedEntityInfo(10018L, "Shared with proj Developers", null, false, TestSharingPermissionUtils.createProjectPermissions(0, 0, "Dev Role Browse", null));
    private static final SharedEntityInfo SHARED_WITH_PROJ_JIRA_DEV_PAGE = new SharedEntityInfo(10017L, "Shared with Proj jira-dev", null, false, TestSharingPermissionUtils.createProjectPermissions(0, 0, "monkey", null));


    protected void setUpTest()
    {
        administration.restoreData("DashboardPagePermissions.xml");
    }

    public void testCorrectDashboardsOnTabs()
    {
        final DashboardAssertions dashboardAssertions = assertions.getDashboardAssertions();

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                PUBLIC_PAGE,
                PRIVATE_PAGE,
                SHARED_WITH_ADMINS_PAGE,
                SHARED_WITH_DEVELOPERS_PAGE,
                SHARED_WITH_JIRA_ADMIN_PAGE,
                SHARED_WITH_JIRA_DEVELOPER_PAGE,
                SHARED_WITH_JIRA_USER_PAGE,
                SHARED_WITH_PROJ_ADMINS_PAGE,
                SHARED_WITH_PROJ_DEVELOPERS_PAGE,
                SHARED_WITH_PROJ_JIRA_DEV_PAGE,
                SYSTEM_PAGE), Dashboard.Table.POPULAR);

        navigation.logout();
        navigation.login("developer");

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                PUBLIC_PAGE,
                SHARED_WITH_DEVELOPERS_PAGE,
                SHARED_WITH_JIRA_DEVELOPER_PAGE,
                SHARED_WITH_JIRA_USER_PAGE,
                SHARED_WITH_PROJ_DEVELOPERS_PAGE,
                SHARED_WITH_PROJ_JIRA_DEV_PAGE,
                SYSTEM_PAGE), Dashboard.Table.POPULAR);

        navigation.logout();
        navigation.login(FRED_USERNAME);

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                PUBLIC_PAGE,
                SHARED_WITH_JIRA_USER_PAGE,
                SYSTEM_PAGE), Dashboard.Table.POPULAR);
    }

    public void testCleanupOfPermissionsOnRoleDelete()
    {
        // delete Developers
        navigation.gotoAdmin();
        administration.roles().delete(10001);
        SHARED_WITH_DEVELOPERS_PAGE.setSharingPermissions(TestSharingPermissionUtils.createPrivatePermissions());

        final DashboardAssertions dashboardAssertions = assertions.getDashboardAssertions();

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                PUBLIC_PAGE,
                PRIVATE_PAGE,
                SHARED_WITH_ADMINS_PAGE,
                SHARED_WITH_DEVELOPERS_PAGE,
                SHARED_WITH_JIRA_ADMIN_PAGE,
                SHARED_WITH_JIRA_DEVELOPER_PAGE,
                SHARED_WITH_JIRA_USER_PAGE,
                SHARED_WITH_PROJ_ADMINS_PAGE,
                SHARED_WITH_PROJ_DEVELOPERS_PAGE,
                SHARED_WITH_PROJ_JIRA_DEV_PAGE,
                SYSTEM_PAGE), Dashboard.Table.POPULAR);

        navigation.logout();
        navigation.login("developer");

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                PUBLIC_PAGE,
                SHARED_WITH_JIRA_DEVELOPER_PAGE,
                SHARED_WITH_JIRA_USER_PAGE,
                SHARED_WITH_PROJ_JIRA_DEV_PAGE,
                SYSTEM_PAGE), Dashboard.Table.POPULAR);

        navigation.logout();
        navigation.login(FRED_USERNAME);

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                PUBLIC_PAGE,
                SHARED_WITH_JIRA_USER_PAGE,
                SYSTEM_PAGE), Dashboard.Table.POPULAR);

        //set back
        SHARED_WITH_DEVELOPERS_PAGE.setSharingPermissions(TestSharingPermissionUtils.createProjectPermissions(0, 0, "Dev Role Browse", "Developers"));

    }

    public void testCleanupOfPermissionsOnGroupDelete()
    {
        // delete jira-developers
        administration.usersAndGroups().deleteGroup("jira-developers");
        SHARED_WITH_JIRA_DEVELOPER_PAGE.setSharingPermissions(TestSharingPermissionUtils.createPrivatePermissions());

        final DashboardAssertions dashboardAssertions = assertions.getDashboardAssertions();

        navigation.dashboard().navigateToPopular();
        List<SharedEntityInfo> list = Arrays.asList(
                PUBLIC_PAGE,
                PRIVATE_PAGE,
                SHARED_WITH_ADMINS_PAGE,
                SHARED_WITH_DEVELOPERS_PAGE,
                SHARED_WITH_JIRA_ADMIN_PAGE,
                SHARED_WITH_JIRA_DEVELOPER_PAGE,
                SHARED_WITH_JIRA_USER_PAGE,
                SHARED_WITH_PROJ_ADMINS_PAGE,
                SHARED_WITH_PROJ_DEVELOPERS_PAGE,
                SHARED_WITH_PROJ_JIRA_DEV_PAGE,
                SYSTEM_PAGE
        );
        dashboardAssertions.assertDashboardPages(list, Dashboard.Table.POPULAR);

        navigation.logout();
        navigation.login("developer");

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                PUBLIC_PAGE,
                SHARED_WITH_JIRA_USER_PAGE,
                SYSTEM_PAGE), Dashboard.Table.POPULAR);

        navigation.logout();
        navigation.login(FRED_USERNAME);

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                PUBLIC_PAGE,
                SHARED_WITH_JIRA_USER_PAGE,
                SYSTEM_PAGE), Dashboard.Table.POPULAR);

        SHARED_WITH_JIRA_DEVELOPER_PAGE.setSharingPermissions(TestSharingPermissionUtils.createGroupPermissions("jira-developers"));

    }


    public void testCleanupOfPermissionsOnProjectDelete()
    {
        administration.project().deleteProject(10010);
        SHARED_WITH_ADMINS_PAGE.setSharingPermissions(TestSharingPermissionUtils.createPrivatePermissions());
        SHARED_WITH_DEVELOPERS_PAGE.setSharingPermissions(TestSharingPermissionUtils.createPrivatePermissions());
        SHARED_WITH_PROJ_DEVELOPERS_PAGE.setSharingPermissions(TestSharingPermissionUtils.createPrivatePermissions());

        final DashboardAssertions dashboardAssertions = assertions.getDashboardAssertions();

        navigation.dashboard().navigateToPopular();
        List<SharedEntityInfo> list = Arrays.asList(
                PUBLIC_PAGE,
                PRIVATE_PAGE,
                SHARED_WITH_ADMINS_PAGE,
                SHARED_WITH_DEVELOPERS_PAGE,
                SHARED_WITH_JIRA_ADMIN_PAGE,
                SHARED_WITH_JIRA_DEVELOPER_PAGE,
                SHARED_WITH_JIRA_USER_PAGE,
                SHARED_WITH_PROJ_ADMINS_PAGE,
                SHARED_WITH_PROJ_DEVELOPERS_PAGE,
                SHARED_WITH_PROJ_JIRA_DEV_PAGE,
                SYSTEM_PAGE);
        dashboardAssertions.assertDashboardPages(list, Dashboard.Table.POPULAR);

        navigation.logout();
        navigation.login("developer");

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                PUBLIC_PAGE,
                SHARED_WITH_JIRA_DEVELOPER_PAGE,
                SHARED_WITH_JIRA_USER_PAGE,
                SHARED_WITH_PROJ_JIRA_DEV_PAGE,
                SYSTEM_PAGE), Dashboard.Table.POPULAR);

        navigation.logout();
        navigation.login(FRED_USERNAME);

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                PUBLIC_PAGE,
                SHARED_WITH_JIRA_USER_PAGE,
                SYSTEM_PAGE), Dashboard.Table.POPULAR);

        SHARED_WITH_JIRA_DEVELOPER_PAGE.setSharingPermissions(TestSharingPermissionUtils.createGroupPermissions("jira-developers"));

    }

}