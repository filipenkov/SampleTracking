package com.atlassian.jira.webtest.selenium.favourites;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import junit.framework.Test;

@WebTest({Category.SELENIUM_TEST })
public class TestManageFiltersFavourites extends JiraSeleniumTest
{

    public static Test suite()
    {
        return suiteFor(TestManageFiltersFavourites.class);
    }


    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestManageFilters.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testRows()
    {
        getNavigator().gotoPage("secure/ManageFilters.jspa", true);

        assertThat.elementDoesNotContainText("id=mf_10000", "zebra");
        assertThat.elementDoesNotContainText("id=mf_10020", "zebra");

        assertThat.elementNotVisible("id=undo_div");

        client.click("id=fav_a_mf_favourites_SearchRequest_10001");
        assertThat.notVisibleByTimeout("id=mf_10001", 10000);

        assertThat.elementDoesNotContainText("id=mf_10000", "zebra");
        assertThat.elementDoesNotContainText("id=mf_10010", "zebra");
        assertThat.elementNotVisible("id=mf_10001");

        assertThat.elementVisible("id=undo_div");
        assertThat.elementHasText("id=undo_div", "'All My' removed from Favourite Filters list.");

        client.click("id=fav_a_mf_favourites_SearchRequest_10020");
        assertThat.notVisibleByTimeout("id=mf_10020", 10000);

        assertThat.elementDoesNotContainText("id=mf_10000", "zebra");
        assertThat.elementNotVisible("id=mf_10020");
        assertThat.elementNotVisible("id=mf_10001");

        assertThat.elementVisible("id=undo_div");
        assertThat.elementHasText("id=undo_div", "'All My' removed from Favourite Filters list.");
        assertThat.elementHasText("id=undo_div", "'New Features' removed from Favourite Filters list.");


        client.click("id=fav_a_mf_favourites_SearchRequest_10000");
        assertThat.notVisibleByTimeout("id=mf_10000", 10000);

        assertThat.elementDoesNotContainText("id=mf_10010", "zebra");
        assertThat.elementNotVisible("id=mf_10000");
        assertThat.elementNotVisible("id=mf_10020");
        assertThat.elementNotVisible("id=mf_10001");
        assertThat.elementNotVisible("id=mf_favourites_empty");

        assertThat.elementVisible("id=undo_div");
        assertThat.elementHasText("id=undo_div", "'All My' removed from Favourite Filters list.");
        assertThat.elementHasText("id=undo_div", "New Features' removed from Favourite Filters list.");
        assertThat.elementHasText("id=undo_div", "'All' removed from Favourite Filters list.");

        client.click("id=fav_a_mf_favourites_SearchRequest_10010");
        assertThat.notVisibleByTimeout("id=mf_10010", 10000);

        assertThat.elementNotVisible("id=mf_10000");
        assertThat.elementNotVisible("id=mf_10010");
        assertThat.elementNotVisible("id=mf_10020");
        assertThat.elementNotVisible("id=mf_10001");
        assertThat.elementVisible("id=mf_favourites_empty");

        assertThat.elementVisible("id=undo_div");
        assertThat.elementHasText("id=undo_div", "'All My' removed from Favourite Filters list.");
        assertThat.elementHasText("id=undo_div", "'New Features' removed from Favourite Filters list.");
        assertThat.elementHasText("id=undo_div", "'All' removed from Favourite Filters list.");
        assertThat.elementHasText("id=undo_div", "'Nick' removed from Favourite Filters list.");


        client.click("id=fav_undo_a_mf_10001");
        assertThat.visibleByTimeout("id=mf_10001", 10000);

        assertThat.elementNotVisible("id=mf_10000");
        assertThat.elementNotVisible("id=mf_10010");
        assertThat.elementNotVisible("id=mf_10020");

        assertThat.elementVisible("id=mf_10001");
        assertThat.elementDoesNotContainText("id=mf_10001", "zebra");

        assertThat.elementNotVisible("id=mf_favourites_empty");

        client.click("id=fav_undo_a_mf_10000");
        client.click("id=fav_undo_a_mf_10010");
        client.click("id=fav_undo_a_mf_10020");

        assertThat.notVisibleByTimeout("id=undo_div", 10000);
        assertThat.elementNotVisible("id=undo_div");
        assertThat.elementVisible("id=mf_10000");
        assertThat.elementDoesNotContainText("id=mf_10000", "zebra");
        assertThat.elementVisible("id=mf_10001");
        assertThat.elementVisible("id=mf_10020");
        assertThat.elementDoesNotContainText("id=mf_10020", "zebra");
        assertThat.elementVisible("id=mf_10010");


        client.click("id=fav_a_mf_favourites_SearchRequest_10001");
        client.click("id=fav_a_mf_favourites_SearchRequest_10020");
        waitFor(2000);

        getNavigator().gotoPage("secure/ManageFilters.jspa", true);

        assertThat.elementNotVisible("id=undo_div");
        assertThat.elementVisible("id=mf_10000");
        assertThat.elementDoesNotContainText("id=mf_10000","zebra");
        assertThat.elementVisible("id=mf_10010");

        assertThat.elementNotPresent("id=mf_10001");
        assertThat.elementNotPresent("id=mf_10020");

        getNavigator().gotoPage("secure/ManageFilters.jspa?filterView=my", true);

        client.click("id=fav_a_mf_owned_SearchRequest_10000");
        waitFor(2000);

        assertThat.elementNotVisible("id=undo_div");
        assertThat.elementVisible("id=mf_10000");
    }

    /*
     TODO - once we get the fitler searching in place come back to this test and make it work as expected

     For now its commented out
     
    public void testFavCount()
    {

        getNavigator().gotoPage("secure/RemoveFavourite.jspa?entityId=" + 10001 + "&entityType=SearchRequest");
        getNavigator().gotoPage("secure/RemoveFavourite.jspa?entityId=" + 10020 + "&entityType=SearchRequest");

        getNavigator().gotoPage("secure/ManageFilters.jspa?filterView=all");
        selenium.waitForPageToLoad(PAGE_LOAD_WAIT);

        //Checking enabled to disabled

        assertThat.elementVisible("id=fav_enabled_div_mf_all_SearchRequest_10000");
        assertThat.elementVisible("id=fav_count_enabled_mf_all_SearchRequest_10000");
        assertThat.elementNotVisible("id=fav_count_disabled_mf_all_SearchRequest_10000");
        assertThat.elementHasText("id=fav_count_enabled_mf_all_SearchRequest_10000", "1");
        assertThat.elementHasText("id=fav_count_disabled_mf_all_SearchRequest_10000", "0");

        selenium.click("id=fav_enabled_a_mf_all_SearchRequest_10000");
        assertThat.notVisibleByTimeout("id=fav_count_enabled_mf_all_SearchRequest_10000", 10000);

        assertThat.elementVisible("id=fav_disabled_div_mf_all_SearchRequest_10000");
        assertThat.elementNotVisible("id=fav_count_enabled_mf_all_SearchRequest_10000");
        assertThat.elementVisible("id=fav_count_disabled_mf_all_SearchRequest_10000");
        assertThat.elementHasText("id=fav_count_enabled_mf_all_SearchRequest_10000", "1");
        assertThat.elementHasText("id=fav_count_disabled_mf_all_SearchRequest_10000", "0");

        selenium.click("id=fav_disabled_a_mf_all_SearchRequest_10000");
        assertThat.notVisibleByTimeout("id=fav_count_disabled_mf_all_SearchRequest_10000", 10000);

        assertThat.elementVisible("id=fav_enabled_div_mf_all_SearchRequest_10000");
        assertThat.elementVisible("id=fav_count_enabled_mf_all_SearchRequest_10000");
        assertThat.elementNotVisible("id=fav_count_disabled_mf_all_SearchRequest_10000");
        assertThat.elementHasText("id=fav_count_enabled_mf_all_SearchRequest_10000", "1");
        assertThat.elementHasText("id=fav_count_disabled_mf_all_SearchRequest_10000", "0");


        //Checking disabled to enabled

        assertThat.elementVisible("id=fav_disabled_div_mf_all_SearchRequest_10001");
        assertThat.elementNotVisible("id=fav_count_enabled_mf_all_SearchRequest_10001");
        assertThat.elementVisible("id=fav_count_disabled_mf_all_SearchRequest_10001");
        assertThat.elementHasText("id=fav_count_enabled_mf_all_SearchRequest_10001", "3");
        assertThat.elementHasText("id=fav_count_disabled_mf_all_SearchRequest_10001", "2");

        selenium.click("id=fav_disabled_a_mf_all_SearchRequest_10001");
        assertThat.notVisibleByTimeout("id=fav_count_disabled_mf_all_SearchRequest_10001", 10000);

        assertThat.elementVisible("id=fav_enabled_div_mf_all_SearchRequest_10001");
        assertThat.elementVisible("id=fav_count_enabled_mf_all_SearchRequest_10001");
        assertThat.elementNotVisible("id=fav_count_disabled_mf_all_SearchRequest_10001");
        assertThat.elementHasText("id=fav_count_enabled_mf_all_SearchRequest_10001", "3");
        assertThat.elementHasText("id=fav_count_disabled_mf_all_SearchRequest_10001", "2");

        selenium.click("id=fav_enabled_a_mf_all_SearchRequest_10001");
        assertThat.notVisibleByTimeout("id=fav_count_enabled_mf_all_SearchRequest_10001", 10000);

        assertThat.elementVisible("id=fav_disabled_div_mf_all_SearchRequest_10001");
        assertThat.elementNotVisible("id=fav_count_enabled_mf_all_SearchRequest_10001");
        assertThat.elementVisible("id=fav_count_disabled_mf_all_SearchRequest_10001");
        assertThat.elementHasText("id=fav_count_enabled_mf_all_SearchRequest_10001", "3");
        assertThat.elementHasText("id=fav_count_disabled_mf_all_SearchRequest_10001", "2");
    }
    */
}