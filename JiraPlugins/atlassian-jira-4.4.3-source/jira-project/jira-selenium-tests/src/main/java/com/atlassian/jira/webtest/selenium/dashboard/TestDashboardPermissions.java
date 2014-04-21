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
public class TestDashboardPermissions extends JiraSeleniumTest
{
    private static final long TIMEOUT = 30000;
    private static final String USER_USERNAME = "fred";
    private static final String USER_PASSWORD = "fred";

    private static final String MANAGE_DASHBOARDS_TITLE = "Manage Dashboards";
    private static final String UPDATE_BUTTON = "Update";


    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestDashboardPermissions.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testDashboardPermissions()
    {
        _testEditOwnedDashboard();
        _testEditDefaultDashboard();
        _testEditSharedDashboard();
        _testViewAndEditNonSharedNonOwnedDashboard();
        _testDragGadgetDashboardHighlighting();
    }

    private void _testEditOwnedDashboard()
    {
        //Editing dashboards you own
        getNavigator().gotoPage("/secure/EditPortalPage!default.jspa?pageId=10010", true);
        client.click(UPDATE_BUTTON, true);
        assertThat.textPresentByTimeout(MANAGE_DASHBOARDS_TITLE, TIMEOUT);

        getNavigator().gotoPage("/secure/EditPortalPage!default.jspa?pageId=10011", true);
        client.click(UPDATE_BUTTON, true);
        assertThat.textPresentByTimeout(MANAGE_DASHBOARDS_TITLE, TIMEOUT);

        getNavigator().gotoPage("/secure/EditPortalPage!default.jspa?pageId=10012", true);
        client.click(UPDATE_BUTTON, true);
        assertThat.textPresentByTimeout(MANAGE_DASHBOARDS_TITLE, TIMEOUT);

        getNavigator().gotoPage("/secure/EditPortalPage!default.jspa?pageId=10013", true);
        client.click(UPDATE_BUTTON, true);
        assertThat.textPresentByTimeout(MANAGE_DASHBOARDS_TITLE, TIMEOUT);
        //Trying to edit dashboards you don't own
        getNavigator().gotoPage("/secure/EditPortalPage!default.jspa?pageId=10014", true);
        assertThat.textPresentByTimeout("You must select a dashboard to edit.", TIMEOUT);
    }

    private void _testEditDefaultDashboard()
    {
        //Try editing the system dashboard via url
        getNavigator().gotoPage("/secure/EditPortalPage!default.jspa?pageId=10000", true);
        assertThat.textPresentByTimeout("You cannot edit the system dashboard.", TIMEOUT);
        //Using admin to edit system dashboard
        getNavigator().gotoPage("/secure/admin/jira/EditDefaultDashboard!default.jspa", true);
        assertThat.textPresentByTimeout("Configure System Dashboard", TIMEOUT);
        //Using non-admin to edit system dashboard via url
        getNavigator().logout(getXsrfToken());
        getNavigator().login(USER_USERNAME, USER_PASSWORD);
        getNavigator().gotoPage("/secure/EditPortalPage!default.jspa?pageId=10000", true);
        assertThat.textPresentByTimeout("Errors", TIMEOUT);
    }

    private void _testEditSharedDashboard()
    {
        //Trying to edit dashboards that are shared but you don't own
        getNavigator().gotoPage("/secure/EditPortalPage!default.jspa?pageId=10010", true);
        client.click(UPDATE_BUTTON, true);
        assertThat.textPresentByTimeout("Errors", TIMEOUT);

        getNavigator().gotoPage("/secure/EditPortalPage!default.jspa?pageId=10011", true);
        client.click(UPDATE_BUTTON, true);
        assertThat.textPresentByTimeout("Errors", TIMEOUT);
    }

    private void _testViewAndEditNonSharedNonOwnedDashboard()
    {
        //Trying to view non-shared and non-owned dashboard via url
        getNavigator().gotoPage("/secure/Dashboard.jspa?selectPageId=10012", true);
        assertThat.elementHasText("jquery=.aui-message.error", "The dashboard you requested either does not exist or you don't have access to it.");

        getNavigator().gotoPage("/secure/Dashboard.jspa?selectPageId=10013", true);
        assertThat.elementHasText("jquery=.aui-message.error", "The dashboard you requested either does not exist or you don't have access to it.");

        //Trying to edit non-shared and non-owned dashboard via url
        getNavigator().gotoPage("/secure/EditPortalPage!default.jspa?pageId=10012", true);
        assertThat.textPresentByTimeout("Errors", TIMEOUT);

        getNavigator().gotoPage("/secure/EditPortalPage!default.jspa?pageId=10013", true);
        assertThat.textPresentByTimeout("Errors", TIMEOUT);


    }

    private void _testDragGadgetDashboardHighlighting()
    {
        getNavigator().gotoPage("/secure/Dashboard.jspa?selectPageId=10020", true);
        client.mouseDown("gadget-10041-title");
        client.mouseMoveAt("gadget-10041-title","10,10");
        assertThat.elementPresentByTimeout("//li[@class='inactive']/a[@href='Dashboard.jspa?selectPageId=10011']", TIMEOUT);
        
    }
}