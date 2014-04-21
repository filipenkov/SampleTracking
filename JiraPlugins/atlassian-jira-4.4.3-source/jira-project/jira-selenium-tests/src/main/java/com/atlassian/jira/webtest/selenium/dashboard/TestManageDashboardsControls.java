package com.atlassian.jira.webtest.selenium.dashboard;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import junit.framework.Test;

@WebTest({Category.SELENIUM_TEST })
public class TestManageDashboardsControls extends JiraSeleniumTest
{
    private static final int TIMEOUT = 10000;

    public static Test suite()
    {
        return suiteFor(TestManageDashboardsControls.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestDashboardPermissions.xml");
    }

    public void testManageDashboardsControls()
    {
        gotoManageDashboards();
        _testTabs();
        _testFavs();
        _testSearch();
    }

    private void _testTabs()
    {
        // Test Tab Navigation
        assertThat.elementHasText("css=div#content-top", "Manage Dashboards");
        client.click("css=a#favourite-dash-tab");
        assertThat.elementPresentByTimeout("css=table#pp_favourite", TIMEOUT);

        client.click("css=a#my-dash-tab");
        assertThat.elementPresentByTimeout("css=table#pp_owned", TIMEOUT);

        client.click("css=a#popular-dash-tab");
        assertThat.elementPresentByTimeout("css=table#pp_popular", TIMEOUT);

        client.click("css=a#search-dash-tab");
        assertThat.elementPresentByTimeout("css=form#pageSearchForm", TIMEOUT);

        client.click("css=a#favourite-dash-tab");
        assertThat.elementPresentByTimeout("css=table#pp_favourite", TIMEOUT);
    }

    private void _testFavs()
    {

        // Test Favs still work
        client.click("css=a#fav_a_pp_favourite_PortalPage_10011");
        assertThat.notVisibleByTimeout("css=tr#pp_10011", TIMEOUT);
        assertThat.visibleByTimeout("css=a#fav_undo_a_pp_10011", TIMEOUT);

        client.click("css=a#fav_undo_a_pp_10011");
        assertThat.visibleByTimeout("css=tr#pp_10011", TIMEOUT);
        assertThat.elementNotPresentByTimeout("css=a#fav_undo_a_pp_10011", TIMEOUT);

        client.click("css=a#fav_a_pp_favourite_PortalPage_10011");
        assertThat.notVisibleByTimeout("css=tr#pp_10011", TIMEOUT);
        assertThat.visibleByTimeout("css=a#fav_undo_a_pp_10011", TIMEOUT);

        client.click("css=a#fav_a_pp_favourite_PortalPage_10013");
        assertThat.notVisibleByTimeout("css=tr#pp_10011", TIMEOUT);
        assertThat.visibleByTimeout("css=a#fav_undo_a_pp_10013", TIMEOUT);


        client.click("css=a#fav_a_pp_favourite_PortalPage_10012");
        assertThat.notVisibleByTimeout("css=tr#pp_10011", TIMEOUT);
        assertThat.visibleByTimeout("css=a#fav_undo_a_pp_10012", TIMEOUT);


        client.click("css=a#fav_a_pp_favourite_PortalPage_10010");
        assertThat.notVisibleByTimeout("css=tr#pp_10011", TIMEOUT);
        assertThat.visibleByTimeout("css=a#fav_undo_a_pp_10010", TIMEOUT);

        assertThat.visibleByTimeout("css=table#pp_favourite_empty", TIMEOUT);
        assertThat.notVisibleByTimeout("css=table#pp_favourite", TIMEOUT);
    }

    private void _testSearch()
    {
        // Test Search
        client.click("css=a#search-dash-tab");
        assertThat.elementPresentByTimeout("css=form#pageSearchForm", TIMEOUT);

        client.click("Search");
        assertThat.elementPresentByTimeout("css=table#pp_browse", TIMEOUT);
        assertThat.elementPresentByTimeout("css=tr#pp_10011", TIMEOUT);
        assertThat.elementPresentByTimeout("css=tr#pp_10010", TIMEOUT);
        assertThat.elementPresentByTimeout("css=tr#pp_10012", TIMEOUT);
        assertThat.elementPresentByTimeout("css=tr#pp_10013", TIMEOUT);
        assertThat.elementPresentByTimeout("css=tr#pp_10000", TIMEOUT);

        client.click("css=a#page_sort_name");
        assertThat.elementPresentByTimeout("css=tr#pp_10000", TIMEOUT);
        assertThat.elementPresentByTimeout("css=tr#pp_10010", TIMEOUT);
        assertThat.elementPresentByTimeout("css=tr#pp_10013", TIMEOUT);
        assertThat.elementPresentByTimeout("css=tr#pp_10012", TIMEOUT);
        assertThat.elementPresentByTimeout("css=tr#pp_10011", TIMEOUT);

        client.click("css=a#home_link_drop");
        assertThat.elementPresentByTimeout("css=a#manage_dash_link_lnk", TIMEOUT);
        assertThat.elementNotPresent("css=a#dash_lnk_10012_lnk");
        assertThat.elementVisible("css=div#fav_count_disabled_pp_browse_PortalPage_10012");

        client.click("css=a#fav_a_pp_browse_PortalPage_10012");
        assertThat.visibleByTimeout("css=div#fav_count_enabled_pp_browse_PortalPage_10012", TIMEOUT);

        client.click("css=a#home_link_drop");
        assertThat.elementPresentByTimeout("css=a#dash_lnk_10012_lnk", TIMEOUT);
    }

    private void gotoManageDashboards()
    {
        getNavigator().gotoPage("secure/ConfigurePortalPages!default.jspa", true);
    }

}
