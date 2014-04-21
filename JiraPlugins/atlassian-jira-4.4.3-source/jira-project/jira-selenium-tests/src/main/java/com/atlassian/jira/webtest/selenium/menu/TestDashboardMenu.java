package com.atlassian.jira.webtest.selenium.menu;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.Quarantine;
import com.atlassian.jira.webtest.selenium.framework.model.Mouse;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import com.atlassian.webtest.ui.keys.KeyEventType;
import com.atlassian.webtest.ui.keys.KeySequence;
import com.atlassian.webtest.ui.keys.SpecialKeys;
import junit.framework.Test;

/**
 * @since v4.00
 */
@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
@Quarantine
public class TestDashboardMenu extends JiraSeleniumTest
{
    private static final long TIMEOUT = 30000;

    private final String CONTEXT = getEnvironmentData().getContext();
    private static final String DASHBOARD_DROPDOWN_LINK = "home_link_drop";
    private static final String MANAGE_DASHBOARD_LINK = "manage_dash_link_lnk";
    private static final String MAIN_BODY = "jira";
    private static final String DASHBOARD_LINK = "home_link";
    private static final String ESC_ASCII = "\\27";
    private static final String VIEW_SYSTEM_DASHBOARD = "dash_lnk_system";
    private static final String DASHBOARD_DROPDOWN_ACTIVE_XPATH = "css=#home_link_drop_drop.active";

     private static final KeySequence ENTER = SpecialKeys.ENTER.withEvents(KeyEventType.KEYDOWN, KeyEventType.KEYPRESS);

    public static Test suite()
    {
        return suiteFor(TestDashboardMenu.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestSharedDashboardFavourites.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testNotLoggedIn()
    {
        _testNotLoggedIn();
    }

    public void testDashboardMenu()
    {
        _testArrowKeys();
        _testEscapeKey();
        _testClickDashboard();
        _testEnterKeyItemSelected();
        _testMultiFavouriteOneSelected();
        _testMultiFavouriteNonFavouriteSelected();
        _testFavouriteChangeOrder();
        _testFavouritePresentUnfavourite();
        _testNoFavouritePresentFavourite();
        _testOneFavouriteNotSelected();
        _testOneFavouriteSelected();
    }

    public void testFavourites()
    {
        _testFavouriteDeleted();
        _testFavouritePermissionRemoved();
        _testNoFavourites();
    }

    public void _testNotLoggedIn()
    {
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        assertThat.elementPresent(DASHBOARD_LINK);
        assertThat.elementPresent(DASHBOARD_DROPDOWN_LINK);
        assertThat.elementNotPresentByTimeout(VIEW_SYSTEM_DASHBOARD, TIMEOUT);

        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(VIEW_SYSTEM_DASHBOARD, TIMEOUT);
        assertThat.elementNotPresentByTimeout(MANAGE_DASHBOARD_LINK, TIMEOUT);

    }

    public void _testNoFavourites()
    {
        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        client.click(MANAGE_DASHBOARD_LINK, true);
        assertThat.textPresentByTimeout("Manage Dashboards", TIMEOUT);
        client.click("fav_a_pp_favourite_PortalPage_10011");
        assertThat.notVisibleByTimeout("fav_a_pp_favourite_PortalPage_10011", TIMEOUT);
        client.click("fav_a_pp_favourite_PortalPage_10012");
        assertThat.notVisibleByTimeout("fav_a_pp_favourite_PortalPage_10012", TIMEOUT);
        client.click("fav_a_pp_favourite_PortalPage_10013");
        assertThat.notVisibleByTimeout("fav_a_pp_favourite_PortalPage_10013", TIMEOUT);

        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        visibleByTimeoutWithDelay(VIEW_SYSTEM_DASHBOARD, TIMEOUT);
        assertThat.elementNotPresentByTimeout("dash_lnk_10010", TIMEOUT);
        assertThat.elementNotPresentByTimeout("dash_lnk_10011", TIMEOUT);
        assertThat.elementNotPresentByTimeout("dash_lnk_10012", TIMEOUT);
        assertThat.elementNotPresentByTimeout("dash_lnk_10013", TIMEOUT);


    }

    public void _testOneFavouriteNotSelected()
    {
        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        client.click(MANAGE_DASHBOARD_LINK, true);
        client.click("//a[@href='" + CONTEXT + "/secure/ConfigurePortalPages!default.jspa?view=popular']");
        visibleByTimeoutWithDelay("css=#md_popular_li.active", TIMEOUT);
        visibleByTimeoutWithDelay("//a[@href='" + CONTEXT + "/secure/Dashboard.jspa?selectPageId=10000']", TIMEOUT);
        client.clickLinkWithText("System Dashboard", true);

        client.click(DASHBOARD_DROPDOWN_LINK);
        assertThat.visibleByTimeout(MANAGE_DASHBOARD_LINK, TIMEOUT);
        assertThat.visibleByTimeout("dash_lnk_10011", TIMEOUT);
        assertThat.elementNotPresentByTimeout("css=#dash_lnk_10011.bolded", TIMEOUT);
        assertThat.elementNotPresentByTimeout("css=#dash_lnk_10010.bolded", TIMEOUT);
        assertThat.elementNotPresentByTimeout("css=#dash_lnk_10013.bolded", TIMEOUT);
        assertThat.elementNotPresentByTimeout("css=#dash_lnk_10012.bolded", TIMEOUT);
        assertThat.elementNotPresentByTimeout("css=#dash_lnk_10010.bolded", TIMEOUT);
        assertThat.elementNotPresentByTimeout("css=#dash_lnk_10013.bolded", TIMEOUT);
        assertThat.elementNotPresentByTimeout("css=#dash_lnk_10012.bolded", TIMEOUT);
        client.click(DASHBOARD_DROPDOWN_LINK);

    }

    public void _testOneFavouriteSelected()
    {
        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay("dash_lnk_10011_lnk", TIMEOUT);
        client.click("dash_lnk_10011_lnk", true);

        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        assertThat.visibleByTimeout("dash_lnk_10011", TIMEOUT);
        assertThat.elementNotPresentByTimeout("css=#dash_lnk_10011.aui-list-item.bolded", TIMEOUT);
        assertThat.elementNotPresentByTimeout("dash_lnk_10010", TIMEOUT);
        assertThat.elementNotPresentByTimeout("dash_lnk_10013", TIMEOUT);
        assertThat.elementNotPresentByTimeout("dash_lnk_10012", TIMEOUT);
        assertThat.elementNotPresentByTimeout("css=#dash_lnk_10010.aui-list-item.bolded", TIMEOUT);
        assertThat.elementNotPresentByTimeout("css=#dash_lnk_10013.aui-list-item.bolded", TIMEOUT);
        assertThat.elementNotPresentByTimeout("css=#dash_lnk_10012.aui-list-item.bolded", TIMEOUT);
        client.click(DASHBOARD_DROPDOWN_LINK);

    }


    public void _testMultiFavouriteOneSelected()
    {
        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        visibleByTimeoutWithDelay("dash_lnk_10010", TIMEOUT);
        visibleByTimeoutWithDelay("dash_lnk_10011", TIMEOUT);
        visibleByTimeoutWithDelay("dash_lnk_10012", TIMEOUT);
        visibleByTimeoutWithDelay("dash_lnk_10013", TIMEOUT);
        client.click("dash_lnk_10012_lnk", true);

        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay("css=#dash_lnk_10012.aui-list-item.bolded", TIMEOUT);
        client.click(DASHBOARD_DROPDOWN_LINK);

    }

    public void _testMultiFavouriteNonFavouriteSelected()
    {
        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        client.click(MANAGE_DASHBOARD_LINK, true);


        client.click("//a[@href='" + CONTEXT + "/secure/ConfigurePortalPages!default.jspa?view=popular']");
        visibleByTimeoutWithDelay("css=#md_popular_li.active", TIMEOUT);
        visibleByTimeoutWithDelay("//a[@href='" + CONTEXT + "/secure/Dashboard.jspa?selectPageId=10000']", TIMEOUT);
        client.clickLinkWithText("System Dashboard", true);

        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay("dash_lnk_10010", TIMEOUT);
        visibleByTimeoutWithDelay("dash_lnk_10011", TIMEOUT);
        visibleByTimeoutWithDelay("dash_lnk_10012", TIMEOUT);
        visibleByTimeoutWithDelay("dash_lnk_10013", TIMEOUT);
        assertThat.elementNotPresentByTimeout("css=#dash_lnk_10010.aui-list-item.bolded", TIMEOUT);
        visibleByTimeoutWithDelay("css=#dash_lnk_10012.aui-list-item.bolded", TIMEOUT);
        assertThat.elementNotPresentByTimeout("css=#dash_lnk_10011.aui-list-item.bolded", TIMEOUT);
        assertThat.elementNotPresentByTimeout("css=#dash_lnk_10013.aui-list-item.bolded", TIMEOUT);
        client.click(MANAGE_DASHBOARD_LINK, true);
        client.click("//a[@href='" + CONTEXT + "/secure/ConfigurePortalPages!default.jspa?view=favourites']");
        visibleByTimeoutWithDelay("css=#md_fav_li.active", TIMEOUT);
    }

    public void _testFavouritePresentUnfavourite()
    {
        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        client.click(MANAGE_DASHBOARD_LINK, true);
        client.click("fav_a_pp_favourite_PortalPage_10011");
        assertThat.notVisibleByTimeout("fav_a_pp_favourite_PortalPage_10011", TIMEOUT);

        client.click(DASHBOARD_DROPDOWN_LINK);
        assertThat.elementNotPresentByTimeout("dash_lnk_10011", TIMEOUT);
        assertThat.visibleByTimeout("dash_lnk_10010", TIMEOUT);
        assertThat.visibleByTimeout("dash_lnk_10012", TIMEOUT);
        assertThat.visibleByTimeout("dash_lnk_10013", TIMEOUT);
        client.click(DASHBOARD_DROPDOWN_LINK);

    }

    public void _testNoFavouritePresentFavourite()
    {
        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        client.click(MANAGE_DASHBOARD_LINK, true);
        assertThat.textPresentByTimeout("Manage Dashboards", TIMEOUT);
        client.click("fav_a_pp_favourite_PortalPage_10010");
        assertThat.notVisibleByTimeout("fav_a_pp_favourite_PortalPage_10010", TIMEOUT);
        client.click("fav_a_pp_favourite_PortalPage_10012");
        assertThat.notVisibleByTimeout("fav_a_pp_favourite_PortalPage_10012", TIMEOUT);
        client.click("fav_a_pp_favourite_PortalPage_10013");
        assertThat.notVisibleByTimeout("fav_a_pp_favourite_PortalPage_10013", TIMEOUT);

        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        visibleByTimeoutWithDelay(VIEW_SYSTEM_DASHBOARD, TIMEOUT);
        assertThat.elementNotPresentByTimeout("dash_lnk_10010", TIMEOUT);
        assertThat.elementNotPresentByTimeout("dash_lnk_10011", TIMEOUT);
        assertThat.elementNotPresentByTimeout("dash_lnk_10012", TIMEOUT);
        assertThat.elementNotPresentByTimeout("dash_lnk_10013", TIMEOUT);

        client.click(MANAGE_DASHBOARD_LINK, true);
        client.click("//a[@href='" + CONTEXT + "/secure/ConfigurePortalPages!default.jspa?view=my']");
        visibleByTimeoutWithDelay("css=#md_my_li.active", TIMEOUT);
        visibleByTimeoutWithDelay("fav_a_pp_owned_PortalPage_10011", TIMEOUT);
        client.click("fav_a_pp_owned_PortalPage_10011");
        visibleByTimeoutWithDelay("jquery=#fav_a_pp_owned_PortalPage_10011.enabled", TIMEOUT);


        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        visibleByTimeoutWithDelay("dash_lnk_10011", TIMEOUT);
        assertThat.elementNotPresentByTimeout(VIEW_SYSTEM_DASHBOARD);
        assertThat.elementNotPresentByTimeout("dash_lnk_10010", TIMEOUT);
        assertThat.elementNotPresentByTimeout("dash_lnk_10012", TIMEOUT);
        assertThat.elementNotPresentByTimeout("dash_lnk_10013", TIMEOUT);
        client.click(DASHBOARD_DROPDOWN_LINK);
    }

    public void _testFavouritePermissionRemoved()
    {
        getNavigator().login("fred", "fred");
        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        client.click(MANAGE_DASHBOARD_LINK, true);

        client.click("//a[@href='" + CONTEXT + "/secure/ConfigurePortalPages!default.jspa?view=popular']");
        visibleByTimeoutWithDelay("css=#md_popular_li.active", TIMEOUT);
        visibleByTimeoutWithDelay("fav_a_pp_popular_PortalPage_10013", TIMEOUT);
        client.click("fav_a_pp_popular_PortalPage_10013");

        assertThat.visibleByTimeout(DASHBOARD_DROPDOWN_LINK);
        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        visibleByTimeoutWithDelay("dash_lnk_10013", TIMEOUT);

        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        client.click(MANAGE_DASHBOARD_LINK, true);

        client.click("//a[@href='" + CONTEXT + "/secure/ConfigurePortalPages!default.jspa?view=favourites']");
        visibleByTimeoutWithDelay("css=#md_fav_li.active", TIMEOUT);
        visibleByTimeoutWithDelay("edit_2", TIMEOUT);
        client.click("edit_2", true);
        client.click("//img[@class='shareTrash' and @title='Delete Share']");
        visibleByTimeoutWithDelay("//div[@title='Not shared with any other users']", TIMEOUT);
        client.click("Update", true);

        getNavigator().login("fred", "fred");
        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        visibleByTimeoutWithDelay(VIEW_SYSTEM_DASHBOARD, TIMEOUT);
        assertThat.elementNotPresentByTimeout("dash_lnk_10013", TIMEOUT);
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

    }

    public void _testFavouriteDeleted()
    {
        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        client.click(MANAGE_DASHBOARD_LINK, true);

        client.click("delete_1");
        visibleByTimeoutWithDelay("Delete", TIMEOUT);
        client.click("Delete");
        assertThat.elementNotPresentByTimeout("fav_a_pp_favourite_PortalPage_10010", TIMEOUT);


        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        assertThat.elementNotPresentByTimeout("dash_lnk_10010", TIMEOUT);
        assertThat.visibleByTimeout("dash_lnk_10011", TIMEOUT);
        assertThat.visibleByTimeout("dash_lnk_10012", TIMEOUT);
        assertThat.visibleByTimeout("dash_lnk_10013", TIMEOUT);

    }

    public void _testFavouriteChangeOrder()
    {
        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        assertThat.elementHasText("//ul[@id='dashboard_link_main']/li[1]", "Another Dashboard");
        assertThat.elementHasText("//ul[@id='dashboard_link_main']/li[2]", "Dashboard for Administrator");
        assertThat.elementHasText("//ul[@id='dashboard_link_main']/li[3]", "My Dashboard");
        assertThat.elementHasText("//ul[@id='dashboard_link_main']/li[4]", "Super Dashboard");

        client.click(MANAGE_DASHBOARD_LINK, true);
        client.click("pos_first_3");
        visibleByTimeoutWithDelay("//table[@id='pp_favourite']/tbody/tr[1][@id='pp_10013']", TIMEOUT);

        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        assertThat.elementHasText("//ul[@id='dashboard_link_main']/li[1]", "Super Dashboard");
        assertThat.elementHasText("//ul[@id='dashboard_link_main']/li[2]", "Another Dashboard");
        assertThat.elementHasText("//ul[@id='dashboard_link_main']/li[3]", "Dashboard for Administrator");
        assertThat.elementHasText("//ul[@id='dashboard_link_main']/li[4]", "My Dashboard");

        client.click("pos_down_2");
        visibleByTimeoutWithDelay("//table[@id='pp_favourite']/tbody/tr[4][@id='pp_10010']", TIMEOUT);


        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        assertThat.elementHasText("//ul[@id='dashboard_link_main']/li[1]", "Super Dashboard");
        assertThat.elementHasText("//ul[@id='dashboard_link_main']/li[2]", "Another Dashboard");
        assertThat.elementHasText("//ul[@id='dashboard_link_main']/li[3]", "My Dashboard");
        assertThat.elementHasText("//ul[@id='dashboard_link_main']/li[4]", "Dashboard for Administrator");
        client.click(DASHBOARD_DROPDOWN_LINK);

    }

    public void _testClickDashboard()
    {
        client.click(DASHBOARD_LINK, true);
        final String tabName = client.getText("//ul[contains(@class, 'tabs')]/li[contains(@class, 'active')]");
        final String fullTabName = "Another Dashboard";
        assert (fullTabName.startsWith(tabName.replace("...", "")));

        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        client.click("dash_lnk_10013_lnk", true);

        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        client.click(MANAGE_DASHBOARD_LINK, true);

        client.click(DASHBOARD_LINK, true);
        assertThat.elementHasText("//li[@class='active']", "Super Dashboard");

    }

    //TODO This currently does not work in selenium for unknown reasons
    // should be investigated and either fixed or removed (JS tests?)
    /*public void testCtrlH(){
        client.controlKeyDown();
        client.keyDown("jira", "h");
        assertThat.elementHasText("//li[@class='active']", "Another Dashboard");
        client.controlKeyUp();
        client.keyUp("jira", "h");


        client.click("home_link_drop");
        assertThat.visibleByTimeout("manage_dash_link", TIMEOUT);
        client.click("dash_lnk_10013_lnk", true);

        client.click("home_link_drop");
        assertThat.visibleByTimeout("manage_dash_link", TIMEOUT);
        client.click("manage_dash_link", true);

        client.controlKeyDown();
        client.keyDown("jira", "h");
        client.controlKeyUp();
        client.keyUp("jira", "h");


        client.waitForPageToLoad();
        assertThat.elementHasText("//li[@class='active']", "Super Dashboard");

    }*/

    //TODO: NOT CROSS-PLATFORM
    // should be re-investigated and either fixed or removed (JS tests?)
//    public void _testCtrlD()
//    {
//        client.controlKeyDown();
//        client.keyPress(MAIN_BODY, "d");
//        client.controlKeyUp();
//
//        assertThat.visibleByTimeout("//li[@class='aui-dd-parent lazy dd-allocated active']//a[@id='home_link']", TIMEOUT);
//        assertThat.visibleByTimeout(MANAGE_DASHBOARD_LINK, TIMEOUT);
//        assertThat.visibleByTimeout("dash_lnk_10010", TIMEOUT);
//        assertThat.visibleByTimeout("dash_lnk_10011", TIMEOUT);
//        assertThat.visibleByTimeout("dash_lnk_10012", TIMEOUT);
//        assertThat.visibleByTimeout("dash_lnk_10013", TIMEOUT);
//        client.click(DASHBOARD_DROPDOWN_LINK);
//        waitFor(1000);
//    }

    public void _testArrowKeys()
    {
        client.click(DASHBOARD_DROPDOWN_LINK);
        visibleByTimeoutWithDelay(MANAGE_DASHBOARD_LINK, TIMEOUT);
        context().ui().pressInBody(SpecialKeys.ARROW_DOWN);
        visibleByTimeoutWithDelay("css=#dash_lnk_10011.active", TIMEOUT);
        context().ui().pressInBody(SpecialKeys.ARROW_DOWN);
         visibleByTimeoutWithDelay("css=#dash_lnk_10010.active", TIMEOUT);
        context().ui().pressInBody(SpecialKeys.ARROW_DOWN);
         visibleByTimeoutWithDelay("css=#dash_lnk_10012.active", TIMEOUT);
        context().ui().pressInBody(SpecialKeys.ARROW_DOWN);
         visibleByTimeoutWithDelay("css=#dash_lnk_10013.active", TIMEOUT);
        context().ui().pressInBody(SpecialKeys.ARROW_DOWN);
         visibleByTimeoutWithDelay("css=#manage_dash_link.active", TIMEOUT);
        context().ui().pressInBody(SpecialKeys.ARROW_DOWN);
        visibleByTimeoutWithDelay("css=#dash_lnk_10011.active", TIMEOUT);

        client.simulateKeyPressForSpecialKey("css=body", 38);
        visibleByTimeoutWithDelay("css=#manage_dash_link.active", TIMEOUT);
        client.simulateKeyPressForSpecialKey("css=body", 38);
        visibleByTimeoutWithDelay("css=#dash_lnk_10013.active", TIMEOUT);
        client.simulateKeyPressForSpecialKey("css=body", 38);
        visibleByTimeoutWithDelay("css=#dash_lnk_10012.active", TIMEOUT);
        client.simulateKeyPressForSpecialKey("css=body", 38);
        visibleByTimeoutWithDelay("css=#dash_lnk_10010.active", TIMEOUT);
        client.simulateKeyPressForSpecialKey("css=body", 38);
        visibleByTimeoutWithDelay("css=#dash_lnk_10011.active", TIMEOUT);
        client.click(DASHBOARD_DROPDOWN_LINK);
    }

    public void _testEscapeKey()
    {
        client.click(DASHBOARD_DROPDOWN_LINK);
        assertThat.visibleByTimeout(DASHBOARD_DROPDOWN_ACTIVE_XPATH, TIMEOUT);
        client.keyPress(MAIN_BODY, ESC_ASCII);
        assertThat.elementNotPresentByTimeout(DASHBOARD_DROPDOWN_ACTIVE_XPATH, TIMEOUT);
    }

    public void _testEnterKeyItemSelected()
    {
        client.click(DASHBOARD_DROPDOWN_LINK);
        assertThat.visibleByTimeout(MANAGE_DASHBOARD_LINK, TIMEOUT);
        Mouse.mouseover(client, "dash_lnk_10012");
        context().ui().pressInBody(ENTER);
        client.waitForPageToLoad();
        assertThat.elementHasText("//li[@class='active']", "My Dashboard");

        client.click(DASHBOARD_DROPDOWN_LINK);
        assertThat.visibleByTimeout(MANAGE_DASHBOARD_LINK, TIMEOUT);
        Mouse.mouseover(client, MANAGE_DASHBOARD_LINK);
        context().ui().pressInBody(ENTER);
        client.waitForPageToLoad();
        assertThat.textPresentByTimeout("Manage Dashboards", TIMEOUT);
    }
    
}
