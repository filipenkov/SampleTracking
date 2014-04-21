package com.atlassian.jira.webtest.selenium.filters;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Condition;
import com.thoughtworks.selenium.Selenium;
import junit.framework.Test;

@WebTest({Category.SELENIUM_TEST })
public class TestManageFiltersControls extends JiraSeleniumTest
{
    private static final int TIMEOUT = 10000;

    public static Test suite()
    {
        return suiteFor(TestManageFiltersControls.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestManageFilters.xml");
    }

    public void testManageDashboardsControls()
    {
        gotoManageFilters();
        _testTabs();
        _testFavs();
        _testSearch();

    }

    private void _testTabs()
    {
        // Test Tab Navigation
        assertThat.elementHasText("css=#content > header > h1", "Manage Filters");
        client.click("css=a#fav-filters-tab");
        assertThat.elementPresentByTimeout("css=table#mf_favourites", TIMEOUT);

        client.click("css=a#my-filters-tab");
        assertThat.elementPresentByTimeout("css=table#mf_owned", TIMEOUT);

        client.click("css=a#popular-filters-tab");
        assertThat.elementPresentByTimeout("css=table#mf_popular", TIMEOUT);

        client.click("css=a#search-filters-tab");
        assertThat.elementPresentByTimeout("css=form#filterSearchForm", TIMEOUT);

        client.click("css=a#fav-filters-tab");
        assertThat.elementPresentByTimeout("css=table#mf_favourites", TIMEOUT);
    }

    private void _testFavs()
    {

        // Test Favs still work
        client.click("css=a#fav_a_mf_favourites_SearchRequest_10000");
        assertThat.notVisibleByTimeout("css=tr#mf_10000", TIMEOUT);
        assertThat.visibleByTimeout("css=a#fav_undo_a_mf_10000", TIMEOUT);

        client.click("css=a#fav_undo_a_mf_10000");
        assertThat.visibleByTimeout("css=tr#mf_10000", TIMEOUT);
        assertThat.elementNotPresentByTimeout("css=a#fav_undo_a_mf_10000", TIMEOUT);

        client.click("css=a#fav_a_mf_favourites_SearchRequest_10000");
        assertThat.notVisibleByTimeout("css=tr#mf_10000", TIMEOUT);
        assertThat.visibleByTimeout("css=a#fav_undo_a_mf_10000", TIMEOUT);

        client.click("css=a#fav_a_mf_favourites_SearchRequest_10001");
        assertThat.notVisibleByTimeout("css=tr#mf_10001", TIMEOUT);
        assertThat.visibleByTimeout("css=a#fav_undo_a_mf_10001", TIMEOUT);

        client.click("css=a#fav_a_mf_favourites_SearchRequest_10020");
        assertThat.notVisibleByTimeout("css=tr#mf_10020", TIMEOUT);
        assertThat.visibleByTimeout("css=a#fav_undo_a_mf_10020", TIMEOUT);

        client.click("css=a#fav_a_mf_favourites_SearchRequest_10010");
        assertThat.notVisibleByTimeout("css=tr#mf_10010", TIMEOUT);
        assertThat.visibleByTimeout("css=a#fav_undo_a_mf_10010", TIMEOUT);

        assertThat.visibleByTimeout("css=table#mf_favourites_empty", TIMEOUT);
        assertThat.notVisibleByTimeout("css=table#mf_favourites", TIMEOUT);
    }

    private void _testSearch()
    {
        // Test Search
        client.click("css=a#search-filters-tab");
        assertThat.elementPresentByTimeout("css=form#filterSearchForm", TIMEOUT);

        client.click("Search");
        assertThat.elementPresentByTimeout("css=table#mf_browse", TIMEOUT);
        assertFirstRow("mf_10000");
        assertThat.elementPresentByTimeout("css=tr#mf_10020", TIMEOUT);
        assertLastRow("mf_10010");

        client.click("css=a#filter_sort_name");

        assertFirstRow("mf_10010");
        assertThat.elementPresentByTimeout("css=tr#mf_10020", TIMEOUT);
        assertLastRow("mf_10000");

        client.click("css=a#find_link_drop");
        assertThat.elementPresentByTimeout("css=a#issues_manage_filters_link_lnk", TIMEOUT);
        assertThat.elementNotPresent("css=a#filter_lnk_10020_lnk");
        assertThat.elementVisible("css=div#fav_count_disabled_mf_browse_SearchRequest_10020");

        client.click("css=a#fav_a_mf_browse_SearchRequest_10020");
        assertThat.visibleByTimeout("css=div#fav_count_enabled_mf_browse_SearchRequest_10020", TIMEOUT);

        client.click("css=a#find_link_drop");
        assertThat.elementPresentByTimeout("css=a#filter_lnk_10020_lnk", TIMEOUT);
    }

    private void assertFirstRow(final String id)
    {
        assertThat.byTimeout(new Condition(){
            public boolean executeTest(Selenium selenium)
            {
                return client.isElementPresent("jquery=#mf_browse tbody tr:first") && id.equals(client.getAttribute("jquery=#mf_browse tbody tr:first@id"));
            }

            public String errorMessage()
            {
                return "First row's id never became: " + id;
            }
        }, TIMEOUT);
    }
    private void assertLastRow(final String id)
    {
        assertThat.byTimeout(new Condition(){
            public boolean executeTest(Selenium selenium)
            {
                return client.isElementPresent("jquery=#mf_browse tbody tr:last") && id.equals(client.getAttribute("jquery=#mf_browse tbody tr:last@id"));
            }

            public String errorMessage()
            {
                return "Last row's id never became: " + id;
            }
        }, TIMEOUT);
    }

    private void gotoManageFilters()
    {
        getNavigator().gotoPage("secure/ManageFilters.jspa", true);
    }


}