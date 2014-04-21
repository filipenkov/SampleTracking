package com.atlassian.jira.webtest.selenium.dashboard;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

/**
 * @since v3.13
 */
@WebTest({Category.SELENIUM_TEST })
public class TestShareDashboardFavourites extends JiraSeleniumTest
{
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestSharedDashboardFavourites.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testUndoKeepsOrderingSimple() throws Exception
    {
        getNavigator().gotoHome();
        // Force going to the right tab
        getNavigator().gotoPage("/secure/ConfigurePortalPages!default.jspa?view=favourites", true);

        // Make sure the pages are in the correct order to begin with
        assertTrue(client.getTable("pp_favourite.1.0").indexOf("Another Dashboard") != -1);
        assertTrue(client.getTable("pp_favourite.2.0").indexOf("Dashboard for Administrator") != -1);
        assertTrue(client.getTable("pp_favourite.3.0").indexOf("My Dashboard") != -1);
        assertTrue(client.getTable("pp_favourite.4.0").indexOf("Super Dashboard") != -1);

        client.click("fav_a_pp_favourite_PortalPage_10010");
        assertThat.visibleByTimeout("//a[@id='fav_undo_a_pp_10010']", 30000);
        assertFalse(client.isVisible("link=Dashboard for Administrator"));

        client.click("fav_undo_a_pp_10010");
        assertThat.visibleByTimeout("link=Dashboard for Administrator", 30000);

        // Now reload the page and make sure that the pages are still in the right order
        getNavigator().gotoPage("/secure/ConfigurePortalPages!default.jspa?view=favourites", true);
        assertTrue(client.getTable("pp_favourite.1.0").indexOf("Another Dashboard") != -1);
        assertTrue(client.getTable("pp_favourite.2.0").indexOf("Dashboard for Administrator") != -1);
        assertTrue(client.getTable("pp_favourite.3.0").indexOf("My Dashboard") != -1);
        assertTrue(client.getTable("pp_favourite.4.0").indexOf("Super Dashboard") != -1);
    }

    public void testUndoKeepsOrderingLotsOfUndos() throws Exception
    {
        getNavigator().gotoHome();
        // Force going to the right tab
        getNavigator().gotoPage("/secure/ConfigurePortalPages!default.jspa?view=favourites", true);

        // Make sure the pages are in the correct order to begin with
        assertTrue(client.getTable("pp_favourite.1.0").indexOf("Another Dashboard") != -1);
        assertTrue(client.getTable("pp_favourite.2.0").indexOf("Dashboard for Administrator") != -1);
        assertTrue(client.getTable("pp_favourite.3.0").indexOf("My Dashboard") != -1);
        assertTrue(client.getTable("pp_favourite.4.0").indexOf("Super Dashboard") != -1);

        client.click("fav_a_pp_favourite_PortalPage_10010");
        assertThat.visibleByTimeout("//a[@id='fav_undo_a_pp_10010']", 30000);
        assertFalse(client.isVisible("link=Dashboard for Administrator"));

        client.click("fav_a_pp_favourite_PortalPage_10012");
        assertThat.visibleByTimeout("//a[@id='fav_undo_a_pp_10012']", 30000);
        assertFalse(client.isVisible("link=My Dashboard"));

        client.click("fav_a_pp_favourite_PortalPage_10013");
        assertThat.visibleByTimeout("//a[@id='fav_undo_a_pp_10013']", 30000);
        assertFalse(client.isVisible("link=Super Dashboard"));

        client.click("fav_a_pp_favourite_PortalPage_10011");
        assertThat.visibleByTimeout("//a[@id='fav_undo_a_pp_10011']", 30000);
        assertFalse(client.isVisible("link=Another Dashboard"));

        assertThat.elementContainsText("jquery=#pp_favourite_empty tr:eq(1)", "You have no favourite dashboards.");

        client.click("fav_undo_a_pp_10010");
        assertThat.visibleByTimeout("link=Dashboard for Administrator", 30000);

        client.click("fav_undo_a_pp_10013");
        assertThat.visibleByTimeout("link=Super Dashboard", 30000);

        client.click("fav_undo_a_pp_10012");
        assertThat.visibleByTimeout("link=My Dashboard", 30000);

        client.click("fav_undo_a_pp_10011");
        assertThat.visibleByTimeout("link=Another Dashboard", 30000);

        // Now reload the page and make sure that the pages are still in the right order
        getNavigator().gotoPage("/secure/ConfigurePortalPages!default.jspa?view=favourites", true);
        assertTrue(client.getTable("pp_favourite.1.0").indexOf("Another Dashboard") != -1);
        assertTrue(client.getTable("pp_favourite.2.0").indexOf("Dashboard for Administrator") != -1);
        assertTrue(client.getTable("pp_favourite.3.0").indexOf("My Dashboard") != -1);
        assertTrue(client.getTable("pp_favourite.4.0").indexOf("Super Dashboard") != -1);
    }

    public void testSharedDashboardFavourites() throws InterruptedException
    {
        getNavigator().gotoHome();
        // Force going to the right tab
        getNavigator().gotoPage("/secure/ConfigurePortalPages!default.jspa?view=favourites", true);

        // Make sure all the arrow links are shown correctly
        assertTrue(client.isElementPresent("link=Another Dashboard"));
        assertTrue(client.isVisible("//a[@id='pos_down_0']/img"));
        assertTrue(client.isVisible("//a[@id='pos_last_0']/img"));
        assertFalse(client.isVisible("//a[@id='pos_first_0']/img"));
        assertFalse(client.isVisible("//a[@id='pos_up_0']/img"));
        assertTrue(client.isElementPresent("link=Dashboard for Administrator"));
        assertTrue(client.isVisible("//a[@id='pos_first_1']/img"));
        assertTrue(client.isVisible("//a[@id='pos_up_1']/img"));
        assertTrue(client.isVisible("//a[@id='pos_down_1']/img"));
        assertTrue(client.isVisible("//a[@id='pos_last_1']/img"));
        assertTrue(client.isElementPresent("link=My Dashboard"));
        assertTrue(client.isVisible("//a[@id='pos_first_2']/img"));
        assertTrue(client.isVisible("//a[@id='pos_up_2']/img"));
        assertTrue(client.isVisible("//a[@id='pos_down_2']/img"));
        assertTrue(client.isVisible("//a[@id='pos_last_2']/img"));
        assertTrue(client.isElementPresent("link=Super Dashboard"));
        assertTrue(client.isVisible("//a[@id='pos_first_3']/img"));
        assertTrue(client.isVisible("//a[@id='pos_up_3']/img"));
        assertFalse(client.isVisible("//a[@id='pos_down_3']/img"));
        assertFalse(client.isVisible("//a[@id='pos_last_3']/img"));

        // Now lets remove some elements and make sure the links are still correct
        client.click("fav_a_pp_favourite_PortalPage_10013");
        assertThat.notVisibleByTimeout("link=Super Dashboard", 30000);

        assertTrue(client.isElementPresent("link=Another Dashboard"));
        assertTrue(client.isVisible("//a[@id='pos_down_0']/img"));
        assertTrue(client.isVisible("//a[@id='pos_last_0']/img"));
        assertFalse(client.isVisible("//a[@id='pos_first_0']/img"));
        assertFalse(client.isVisible("//a[@id='pos_up_0']/img"));
        assertTrue(client.isElementPresent("link=Dashboard for Administrator"));
        assertTrue(client.isVisible("//a[@id='pos_first_1']/img"));
        assertTrue(client.isVisible("//a[@id='pos_up_1']/img"));
        assertTrue(client.isVisible("//a[@id='pos_down_1']/img"));
        assertTrue(client.isVisible("//a[@id='pos_last_1']/img"));
        assertTrue(client.isElementPresent("link=My Dashboard"));
        assertTrue(client.isVisible("//a[@id='pos_first_2']/img"));
        assertTrue(client.isVisible("//a[@id='pos_up_2']/img"));
        // These should be gone now
        assertFalse(client.isVisible("//a[@id='pos_down_2']/img"));
        assertFalse(client.isVisible("//a[@id='pos_last_2']/img"));
        // This row should no longer be visible
        assertFalse(client.isVisible("link=Super Dashboard"));
        assertFalse(client.isVisible("//a[@id='pos_first_3']/img"));
        assertFalse(client.isVisible("//a[@id='pos_up_3']/img"));
        assertFalse(client.isVisible("//a[@id='pos_down_3']/img"));
        assertFalse(client.isVisible("//a[@id='pos_last_3']/img"));

        // Get rid of the Another Dashboard favourite
        client.click("fav_a_pp_favourite_PortalPage_10011");
        assertThat.notVisibleByTimeout("link=Another Dashboard", 30000);

        // This whole row should not be visible
        assertFalse(client.isVisible("link=Another Dashboard"));
        assertFalse(client.isVisible("//a[@id='pos_down_0']/img"));
        assertFalse(client.isVisible("//a[@id='pos_last_0']/img"));
        assertFalse(client.isVisible("//a[@id='pos_first_0']/img"));
        assertFalse(client.isVisible("//a[@id='pos_up_0']/img"));
        // This should be the top element now
        assertTrue(client.isElementPresent("link=Dashboard for Administrator"));
        assertFalse(client.isVisible("//a[@id='pos_first_1']/img"));
        assertFalse(client.isVisible("//a[@id='pos_up_1']/img"));
        assertTrue(client.isVisible("//a[@id='pos_down_1']/img"));
        assertTrue(client.isVisible("//a[@id='pos_last_1']/img"));
        // This should be the last element now
        assertTrue(client.isElementPresent("link=My Dashboard"));
        assertTrue(client.isVisible("//a[@id='pos_first_2']/img"));
        assertTrue(client.isVisible("//a[@id='pos_up_2']/img"));
        // These should be gone now
        assertFalse(client.isVisible("//a[@id='pos_down_2']/img"));
        assertFalse(client.isVisible("//a[@id='pos_last_2']/img"));
        // This row should no longer be visible
        assertFalse(client.isVisible("link=Super Dashboard"));
        assertFalse(client.isVisible("//a[@id='pos_first_3']/img"));
        assertFalse(client.isVisible("//a[@id='pos_up_3']/img"));
        assertFalse(client.isVisible("//a[@id='pos_down_3']/img"));
        assertFalse(client.isVisible("//a[@id='pos_last_3']/img"));

        // Get rid of the My Dashboard favourite
        client.click("fav_a_pp_favourite_PortalPage_10012");
        assertThat.notVisibleByTimeout("link=My Dashboard", 30000);

        // This whole row should not be visible
        assertFalse(client.isVisible("link=Another Dashboard"));
        assertFalse(client.isVisible("//a[@id='pos_down_0']/img"));
        assertFalse(client.isVisible("//a[@id='pos_last_0']/img"));
        assertFalse(client.isVisible("//a[@id='pos_first_0']/img"));
        assertFalse(client.isVisible("//a[@id='pos_up_0']/img"));
        // This should be the only element now
        assertTrue(client.isElementPresent("link=Dashboard for Administrator"));
        assertFalse(client.isVisible("//a[@id='pos_first_1']/img"));
        assertFalse(client.isVisible("//a[@id='pos_up_1']/img"));
        assertFalse(client.isVisible("//a[@id='pos_down_1']/img"));
        assertFalse(client.isVisible("//a[@id='pos_last_1']/img"));
        // This row should no longer be visible
        assertFalse(client.isVisible("link=My Dashboard"));
        assertFalse(client.isVisible("//a[@id='pos_first_2']/img"));
        assertFalse(client.isVisible("//a[@id='pos_up_2']/img"));
        assertFalse(client.isVisible("//a[@id='pos_down_2']/img"));
        assertFalse(client.isVisible("//a[@id='pos_last_2']/img"));
        // This row should no longer be visible
        assertFalse(client.isVisible("link=Super Dashboard"));
        assertFalse(client.isVisible("//a[@id='pos_first_3']/img"));
        assertFalse(client.isVisible("//a[@id='pos_up_3']/img"));
        assertFalse(client.isVisible("//a[@id='pos_down_3']/img"));
        assertFalse(client.isVisible("//a[@id='pos_last_3']/img"));
    }
}