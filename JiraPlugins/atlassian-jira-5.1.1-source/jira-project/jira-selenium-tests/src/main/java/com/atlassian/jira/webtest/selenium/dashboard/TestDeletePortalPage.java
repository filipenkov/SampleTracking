package com.atlassian.jira.webtest.selenium.dashboard;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.auidialog.AbstractAuiDialogTest;
import com.atlassian.jira.webtest.selenium.framework.SeleniumClosure;
import com.atlassian.jira.webtest.selenium.framework.Window;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

/**
 * Selenium test for delete portal page AUI dialog.
 *
 * @since v4.2
 */
@SkipInBrowser(browsers={Browser.IE}) // Error opening new window - Responsibility: Hamish
@WebTest({Category.SELENIUM_TEST })
public class TestDeletePortalPage extends AbstractAuiDialogTest
{
    private static final int[] ALL_TEST_DASHBOARD_IDS = {10010, 10011, 10012, 10013};
    private static final int TEST_DASHBOARD_ID = 10010;
    private static final int FAVOURITE_DASHBOARD_ID = 10012;
    private static final int SUPER_DASHBOARD_ID = 10013;

    private static final String DELETE_DASHBOARD_BUTTON_ID = "delete-portal-page-submit";

    // I know this is not very deterministic but, the dashboards shorten the text on the tabs in a non-deterministic
    // way which we don't understand very well.
    private static final String ANOTHER_DASHBOARDS_FIRST_ACTIVE_TAB_LOCATOR =
            "jquery=#dashboard .tabs .active.first strong :contains('Another Das')"; 

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestDashboardPermissions.xml");
    }

    public void testOpensDialogsCorrespondingToDashboards()
    {
        gotoManageDashboards();
        goToMyTab();
        for (int dashboardId : ALL_TEST_DASHBOARD_IDS)
        {
            openDeleteDashboardDialog(dashboardId);
            assertOnDeleteDashboardDialog(dashboardId);
            closeDialogByClickingCancel();
        }
    }

    public void testDeleteConfirmDialog()
    {
        shouldOpenDeleteConfirmDialogOnPageEntry();
        shouldNotDeleteDashboardGivenDialogClosedUsingCancel();
        shouldDeleteDashboardGivenDialogSubmit();
        shouldOpenDeleteConfirmDialogOnFavouriteTab();
    }

    public void testDeleteNotExistingDashboard() throws Exception
    {
        gotoManageDashboards();
        goToMyTab();
        deleteDashboardInBackground(TEST_DASHBOARD_ID);
        openDeleteDashboardDialog(TEST_DASHBOARD_ID);
        assertAuiErrorMessage("Dashboard does not exist.");
    }

    public void testDeleteDashboardRedirectsToDashboardsPage()
    {
        gotoDashboard(SUPER_DASHBOARD_ID);
        deleteDashboard();
        assertDashboardNotPresent(SUPER_DASHBOARD_ID);
        assertOnAnotherDashboardPage();
    }

    private void shouldOpenDeleteConfirmDialogOnPageEntry()
    {
        gotoManageDashboards();
        openDeleteDashboardDialog(TEST_DASHBOARD_ID);
        assertOnDeleteDashboardDialog(TEST_DASHBOARD_ID);
    }

    private void shouldNotDeleteDashboardGivenDialogClosedUsingCancel()
    {
        goToDeleteDialogOnMyTabFor(TEST_DASHBOARD_ID);
        closeDialogByClickingCancel();
        assertOnMyDashboardsPage();
        assertDashboardPresent(TEST_DASHBOARD_ID);
    }

    private void shouldDeleteDashboardGivenDialogSubmit()
    {
        goToDeleteDialogOnMyTabFor(TEST_DASHBOARD_ID);
        submitDeleteDialog();
        assertOnMyDashboardsPage();
        assertDashboardNotPresent(TEST_DASHBOARD_ID);
    }

    private void shouldOpenDeleteConfirmDialogOnFavouriteTab()
    {
        gotoManageDashboards();
        assertOnManageDashboardsPage();
        goToFavouriteTab();
        assertOnFavouriteDashboardsPage();
        openDeleteDashboardDialog(FAVOURITE_DASHBOARD_ID);
        assertOnDeleteDashboardDialog(FAVOURITE_DASHBOARD_ID);
    }

    private void goToDeleteDialogOnMyTabFor(int dashboardId)
    {
        gotoManageDashboards();
        assertOnManageDashboardsPage();
        goToMyTab();
        assertOnMyDashboardsPage();
        openDeleteDashboardDialog(dashboardId);
        assertOnDeleteDashboardDialog(dashboardId);
    }

    private void submitDeleteDialog()
    {
        submitDialogAndWaitForReload("delete-portal-page-submit");
    }

    private void assertOnFavouriteDashboardsPage()
    {
        assertOnManageDashboardsPage();
        assertThat.elementHasText("jquery=.content-body h2", "Favourite Dashboards");
    }

    private void assertOnMyDashboardsPage()
    {
        assertOnManageDashboardsPage();
        assertThat.elementHasText("jquery=.content-body h2", "My Dashboards");
    }

    private void assertOnManageDashboardsPage()
    {
        assertThat.elementHasText("jquery=#content > header > h1", "Manage Dashboards");
    }

    private void openDeleteDashboardDialog(int dashboardId)
    {
        assertThat.elementPresentByTimeout(deleteDashboardLinkLocator(dashboardId), DEFAULT_TIMEOUT);
        client.click(deleteDashboardLinkLocator(dashboardId));
    }

    private String deleteDashboardLinkLocator(int dashboardId)
    {
        return String.format("jquery=#pp_%d .delete_dash", dashboardId);
    }

    private void assertOnDeleteDashboardDialog(int dashboardId)
    {
        assertDialogIsOpenAndReady();
        assertDashboardIdOnDialog(dashboardId);
    }

    private void assertOnAnotherDashboardPage()
    {
        assertEquals("Another Dashboard - jWebTest JIRA installation", client.getTitle());
        assertThat.elementPresentByTimeout(ANOTHER_DASHBOARDS_FIRST_ACTIVE_TAB_LOCATOR, DEFAULT_TIMEOUT);
    }

    private void assertDashboardIdOnDialog(final int dashboardId)
    {
        assertThat.attributeContainsValue(pageIdHolderLocatorFor(), "value", Integer.toString(dashboardId));
    }

    private String pageIdHolderLocatorFor()
    {
        return String.format(VISIBLE_DIALOG_CONTENT_SELECTOR + " :input:hidden[name='pageId']");
    }

    private void assertDashboardPresent(int dashboardId)
    {
        assertThat.elementPresentByTimeout(dashboardRowLocator(dashboardId), DEFAULT_TIMEOUT);
    }

    private void assertDashboardNotPresent(int dashboardId)
    {
        assertThat.elementNotPresentByTimeout(dashboardRowLocator(dashboardId), DEFAULT_TIMEOUT);
    }

    private String dashboardRowLocator(int dashboardId)
    {
        return String.format("jquery=tr#pp_%d", dashboardId);
    }

    private void gotoManageDashboards()
    {
        getNavigator().gotoPage("secure/ConfigurePortalPages!default.jspa", true);
    }

    private void goToFavouriteTab()
    {
        assertThat.elementPresentByTimeout("jquery=#favourite-dash-tab");
        client.click("jquery=#favourite-dash-tab");
        assertThat.visibleByTimeout("jquery=h2:contains(Favourite Dashboards)", 500);
    }

    private void goToMyTab()
    {
        assertThat.elementPresentByTimeout("jquery=#my-dash-tab");
        client.click("jquery=#my-dash-tab");
        assertThat.visibleByTimeout("jquery=h2:contains(My Dashboards)", 500);
    }

    private void gotoDashboard(int dashboardId)
    {
        getNavigator().gotoPage("secure/Dashboard.jspa?selectPageId=" + dashboardId, true);
    }

    private void deleteDashboard()
    {
        openDeleteDashboardDialog();
        client.click(DELETE_DASHBOARD_BUTTON_ID);
        client.waitForPageToLoad();
    }

    private void openDeleteDashboardDialog()
    {
        // DODGY - We need to this before clicking on the "delete_dashboard" link because the click event handler is
        // only attached after a "mousedown" on the "tools" menu of the dashboards page.
        client.mouseDown("jquery=a.icon-tools");
        client.click("id=delete_dashboard");
        assertDialogIsOpenAndReady();
    }

    private void deleteDashboardInBackground(final int dashboardId) throws Exception
    {
        Window.withNewWindow(client, "", "delete-task", new SeleniumClosure()
        {
            public void execute() throws Exception
            {
                goToDeleteDialogOnMyTabFor(dashboardId);
                assertOnDeleteDashboardDialog(dashboardId);
                submitDeleteDialog();
                assertOnMyDashboardsPage();
                assertDashboardNotPresent(dashboardId);
            }
        });
    }
}
