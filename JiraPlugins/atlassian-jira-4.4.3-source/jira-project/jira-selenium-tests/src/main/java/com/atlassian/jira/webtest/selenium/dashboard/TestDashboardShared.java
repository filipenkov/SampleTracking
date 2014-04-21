package com.atlassian.jira.webtest.selenium.dashboard;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.harness.util.Dashboard;

@WebTest({Category.SELENIUM_TEST })
public class TestDashboardShared extends JiraSeleniumTest
{
    private static final String QUICK_LINKS = "Quick Links";

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestSharedDashboard.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testViewDashboardSharedWithAllUsersLoggedOut()
    {
        //first view the gadget logged in
        final Dashboard dashboard = getNavigator().dashboard("10031");
        dashboard.view();

        assertThat.elementHasText("//ul[contains(@class, 'tabs')]/li[contains(@class, 'active')]", "Shared");
        assertThat.textPresent(QUICK_LINKS);

        getNavigator().logout(getXsrfToken());
        dashboard.view();
        //check that there's no tabs
        assertThat.elementNotPresentByTimeout("//ul[contains(@class, 'tabs')]/li[contains(@class, 'active')]", 5000);
        //the dashboard should still show the assigned to me portlet though
        assertThat.textPresent(QUICK_LINKS);

        getNavigator().gotoHome();
        //check we're now viewing the system dashboard, which shouldn't have the Assigned to me portlet.
        assertThat.textNotPresentByTimeout(QUICK_LINKS, 5000);
    }
}
