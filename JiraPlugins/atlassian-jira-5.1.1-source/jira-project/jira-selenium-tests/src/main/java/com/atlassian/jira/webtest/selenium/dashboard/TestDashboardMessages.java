package com.atlassian.jira.webtest.selenium.dashboard;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

/**
 * @since v4.00
 */
@WebTest({Category.SELENIUM_TEST })
public class TestDashboardMessages extends JiraSeleniumTest
{
    private static final long TIMEOUT = 30000;

    private static final String USER_USERNAME = "fred";
    private static final String USER_PASSWORD = "fred";
    private static final String NO_FAVOURITES = "You are viewing the 'System Default' dashboard, as you have not added any dashboards as favourites. To display your preferred dashboard here, click Manage Dashboard and add one or more dashboards as favourites.";
    private static final String TEMP_VIEW = "The dashboard that you are viewing will only be displayed temporarily, as you do not have it added as a favourite. To display this dashboard permanently, add it as a favourite.";
    private static final String SHARED_EMPTY = "There are no gadgets configured for this dashboard. You may wish to remove this dashboard as a favourite.";
    private static final String MANAGE_DASHBOARDS_POPULAR = "/secure/ConfigurePortalPages!default.jspa?view=popular";
    private static final String FAVOURITE_MY_DASHBOARD = "fav_a_pp_popular_PortalPage_10012";
    private static final String NO_GADGETS_LOGGED_OUT = "This dashboard does not contain any gadgets or you do not have permission to view them. Perhaps you need to log in to see them.";
    private static final String NO_GADGETS_LOGGED_IN = "This dashboard does not contain any gadgets or you do not have permission to view them. If you think this is incorrect, please contact your JIRA administrators.";

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestDashboardMessages.xml");
        getNavigator().login(USER_USERNAME, USER_PASSWORD);
    }

    public void testDashboardMessages()
    {
        //Testing dashboard messages for the following cases
        _testOwnedButNonFavourited();
        _testNotCurrentlyFavourited();
        _testSharedEmpty();
        _testNoGadgets();
    }

    private void _testOwnedButNonFavourited()
    {
        assertThat.textPresentByTimeout(NO_FAVOURITES, TIMEOUT);
    }

    private void _testNotCurrentlyFavourited()
    {
        getNavigator().gotoPage(MANAGE_DASHBOARDS_POPULAR, true);
        client.clickLinkWithText("Another Dashboard", true);
        assertThat.textPresentByTimeout(TEMP_VIEW, TIMEOUT);
    }

    private void _testSharedEmpty()
    {
        getNavigator().gotoPage(MANAGE_DASHBOARDS_POPULAR, true);
        client.click(FAVOURITE_MY_DASHBOARD);
        //wait for the page to be favourited
        waitFor(5000);
        getNavigator().gotoHome();
        assertThat.textPresentByTimeout(SHARED_EMPTY, TIMEOUT);
    }

    private void _testNoGadgets()
    {
        getNavigator().logout(getXsrfToken());
        getNavigator().dashboard("10030").view();
        assertThat.textPresentByTimeout(NO_GADGETS_LOGGED_OUT, TIMEOUT);
        getNavigator().login(USER_USERNAME, USER_PASSWORD);
        getNavigator().dashboard("10030").view();
        //need to favourite the page first.
        client.clickLinkWithText("add it as a favourite", true);
        assertThat.textPresentByTimeout(NO_GADGETS_LOGGED_IN, TIMEOUT);
    }
}