package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.Dashboard;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.Arrays;

@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestDeleteUserSharedEntities extends FuncTestCase
{
    private static final SharedEntityInfo SYSTEM_PAGE = new SharedEntityInfo(10000L, "System Dashboard", null, true, TestSharingPermissionUtils.createPublicPermissions());
    private static final SharedEntityInfo DASHBOARD_2 = new SharedEntityInfo(10011L, "Dashboard 2", null, true, TestSharingPermissionUtils.createPublicPermissions());
    private static final SharedEntityInfo DASHBOARD_FOR_ADMIN = new SharedEntityInfo(10010L, "Dashboard for " + ADMIN_FULLNAME, "Copy of 'System Dashboard'", true, TestSharingPermissionUtils.createPublicPermissions());

    protected void setUpTest()
    {
        administration.restoreData("TestDeleteUserForSharedEntity.xml");
    }


    public void testDeleteUser()
    {
        administration.usersAndGroups().deleteUserConfirm(FRED_USERNAME);
        Locator locator = new XPathLocator(tester, "//span[@id='numberOfFilters']");
        assertEquals("3", locator.getText().trim());

        locator = new XPathLocator(tester, "//span[@id='numberOfOtherFavouritedFilters']");
        assertEquals("2", locator.getText().trim());

        locator = new XPathLocator(tester, "//span[@id='numberOfNonPrivatePortalPages']");
        assertEquals("3", locator.getText().trim());

        locator = new XPathLocator(tester, "//span[@id='numberOfOtherFavouritedPortalPages']");
        assertEquals("2", locator.getText().trim());

        tester.submit("Delete");

        navigation.dashboard().navigateToPopular();

        SYSTEM_PAGE.setFavCount(1);
        DASHBOARD_2.setFavCount(1);
        DASHBOARD_FOR_ADMIN.setFavCount(1);

        assertions.getDashboardAssertions().assertDashboardPages(Arrays.asList(DASHBOARD_2, DASHBOARD_FOR_ADMIN, SYSTEM_PAGE), Dashboard.Table.POPULAR);

        navigation.manageFilters().popularFilters();

        text.assertTextPresent(new WebPageLocator(tester), "There are no filters in the system that you can view.");


    }
}
