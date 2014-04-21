package com.atlassian.jira.webtest.selenium.dashboard;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

/**
 * @since v4.00
 */
@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestDashboardGeneral extends JiraSeleniumTest
{
    private static final long TIMEOUT = 30000;
    private final String CONTEXT = getEnvironmentData().getContext();
    private static final String MANAGE_DASHBOARDS_FAVOURITE = "/secure/ConfigurePortalPages!default.jspa?view=favourites";
    private static final String DASHBOARD_HOME = "/secure/Dashboard.jspa";
    private static final String EDIT_DASHBOARD = "/secure/EditPortalPage!default.jspa?pageId=<ID>";
    private static final String EXAMPLE_SCRIPT = "<script>alert('blah');</script>";

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestDashboardGeneral.xml");
    }

    public void testDashboardGeneral()
    {
        _testEditTitle();
        _testEscapeCharacters();
        _testTabOrder();
        _testNoTabIfNotMultipleFavouriteDashboards();
        _testFavouriteDashboards();
    }

    public void _testEditTitle()
    {
        //Changing the dashboard title
        getNavigator().gotoHome();
        final String tabName = client.getText("//ul[contains(@class, 'tabs')]/li[contains(@class, 'active')]");
        final String fullTabName = "Another Dashboard";
        assert (fullTabName.startsWith(tabName.replace("...", "")));
        getNavigator().gotoPage(MANAGE_DASHBOARDS_FAVOURITE, true);
        assertThat.elementVisibleContainsText("//a[@href='" + CONTEXT + "/secure/Dashboard.jspa?selectPageId=10011']", "Another Dashboard");
        client.click("edit_0", true);
        client.typeInElementWithName("portalPageName", "Changed");
        client.click("submit", true);
        assertThat.elementVisibleContainsText("//a[@href='" + CONTEXT + "/secure/Dashboard.jspa?selectPageId=10011']", "Changed");
        //Make sure the change is carried over to the dashboard tabs
        getNavigator().gotoPage(DASHBOARD_HOME, true);
        assertThat.elementVisibleContainsText("//li[@class='active first']", "Changed");
    }

    public void _testEscapeCharacters()
    {
        //Try to put script in a dashboard name
        getNavigator().gotoPage(EDIT_DASHBOARD.replace("<ID>", "10011"), true);
        client.typeInElementWithName("portalPageName", EXAMPLE_SCRIPT);
        client.click("submit", true);
        assertThat.elementContainsText("//a[@href='" + CONTEXT + "/secure/Dashboard.jspa?selectPageId=10011']", EXAMPLE_SCRIPT);
        getNavigator().gotoPage(DASHBOARD_HOME, true);
        final String tabName = client.getText("//li[@class='active first']");
        assert (EXAMPLE_SCRIPT.startsWith(tabName.replace("...", "")));
    }


    public void _testTabOrder()
    {
        //Confirming the initial order
        getNavigator().gotoPage(MANAGE_DASHBOARDS_FAVOURITE, true);
        assertThat.visibleByTimeout("//table[@id='pp_favourite']//tbody//tr[1][@id='pp_10011']", TIMEOUT);
        assertThat.visibleByTimeout("//table[@id='pp_favourite']//tbody//tr[2][@id='pp_10010']", TIMEOUT);
        assertThat.visibleByTimeout("//table[@id='pp_favourite']//tbody//tr[3][@id='pp_10012']", TIMEOUT);
        assertThat.visibleByTimeout("//table[@id='pp_favourite']//tbody//tr[4][@id='pp_10013']", TIMEOUT);

        getNavigator().gotoPage("/secure/Dashboard.jspa?selectPageId=10013", true);
        assertThat.visibleByTimeout("//ul[@class='vertical tabs']//li[1]//a[@href='Dashboard.jspa?selectPageId=10011']", TIMEOUT);
        assertThat.visibleByTimeout("//ul[@class='vertical tabs']//li[2]//a[@href='Dashboard.jspa?selectPageId=10010']", TIMEOUT);
        assertThat.visibleByTimeout("//ul[@class='vertical tabs']//li[3]//a[@href='Dashboard.jspa?selectPageId=10012']", TIMEOUT);
        assertThat.visibleByTimeout("//ul[@class='vertical tabs']//li[4]//strong/span[@title='Super Dashboard']", TIMEOUT);

        //Changing the order
        getNavigator().gotoPage(MANAGE_DASHBOARDS_FAVOURITE, true);
        client.click("pos_first_2");

        //Make sure the changes took effect
        assertThat.visibleByTimeout("//table[@id='pp_favourite']//tbody//tr[1][@id='pp_10012']", TIMEOUT);
        assertThat.visibleByTimeout("//table[@id='pp_favourite']//tbody//tr[2][@id='pp_10011']", TIMEOUT);
        assertThat.visibleByTimeout("//table[@id='pp_favourite']//tbody//tr[3][@id='pp_10010']", TIMEOUT);
        assertThat.visibleByTimeout("//table[@id='pp_favourite']//tbody//tr[4][@id='pp_10013']", TIMEOUT);

        getNavigator().gotoPage("/secure/Dashboard.jspa?selectPageId=10012", true);
        assertThat.visibleByTimeout("//ul[@class='vertical tabs']//li[1]//strong/span[@title='My Dashboard']", TIMEOUT);
        assertThat.visibleByTimeout("//ul[@class='vertical tabs']//li[2]//a[@href='Dashboard.jspa?selectPageId=10011']", TIMEOUT);
        assertThat.visibleByTimeout("//ul[@class='vertical tabs']//li[3]//a[@href='Dashboard.jspa?selectPageId=10010']", TIMEOUT);
        assertThat.visibleByTimeout("//ul[@class='vertical tabs']//li[4]//a[@href='Dashboard.jspa?selectPageId=10013']", TIMEOUT);

    }

    public void _testNoTabIfNotMultipleFavouriteDashboards()
    {
        //Removing favourite dashboards
        getNavigator().gotoPage(MANAGE_DASHBOARDS_FAVOURITE, true);
        client.click("fav_a_pp_favourite_PortalPage_10012");
        assertThat.notVisibleByTimeout("fav_div_pp_favourite_PortalPage_10012", TIMEOUT);
        client.click("fav_a_pp_favourite_PortalPage_10011");
        assertThat.notVisibleByTimeout("fav_div_pp_favourite_PortalPage_10011", TIMEOUT);
        client.click("fav_a_pp_favourite_PortalPage_10013");
        assertThat.notVisibleByTimeout("fav_div_pp_favourite_PortalPage_10013", TIMEOUT);

        //Check there are no tabs
        getNavigator().gotoPage(DASHBOARD_HOME, true);
        assertThat.elementNotPresentByTimeout("//ul[@class='vertical tabs']", TIMEOUT);

    }

    public void _testFavouriteDashboards()
    {
        //Check there are no tabs
        getNavigator().gotoHome();
        assertThat.elementNotPresentByTimeout("//ul[@class='vertical tabs']", TIMEOUT);

        //Favourite dashboard
        getNavigator().gotoPage("/secure/ConfigurePortalPages!default.jspa?view=my", true);
        client.click("fav_a_pp_owned_PortalPage_10012");
        assertThat.visibleByTimeout("css=#fav_a_pp_owned_PortalPage_10012.fav-link.enabled", TIMEOUT);

        //Make sure the tabs appear
        getNavigator().gotoPage(DASHBOARD_HOME, true);
        assertThat.visibleByTimeout("//ul[@class='vertical tabs']//li[1]//strong/span[@title='Dashboard for Administrator']", TIMEOUT);
        assertThat.visibleByTimeout("//ul[@class='vertical tabs']//li[2]//a[@href='Dashboard.jspa?selectPageId=10012']", TIMEOUT);
    }

    public void _testViewAndEditNonFavouritedOwnedDashboard()
    {
        //View via url works
        getNavigator().gotoPage("/secure/Dashboard.jspa?selectPageId=10013", true);
        assertThat.elementVisibleContainsText("//li[@class='active']", "Super Dashboard");

        //Checking no errors when trying to edit
        getNavigator().gotoPage("/secure/EditPortalPage!default.jspa?pageId=10013", true);
        assertThat.textPresentByTimeout("Edit and Share Dashboard", TIMEOUT);
        assertThat.textNotPresentByTimeout("Errors", TIMEOUT);

    }
}