package com.atlassian.jira.webtest.selenium.navigator;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

/**
 * @since v4.0
 */

@WebTest({Category.SELENIUM_TEST })
public class TestChartsView extends JiraSeleniumTest
{
    private static final int TIMEOUT = 60000;
    private static final String CHARTS_CONTAINER = "jquery=div#charts-dialog";
    private static final String FILTER_NAME = "my wonky filter name";
    private static final String DEFAULT_DASHBOARD_MESSAGE =
            "You are currently using the default dashboard. Saving this gadget will create a personalised dashboard for you.";
    private static final String SAVE_TO_DASHBOARD_TITLE = "Save gadget to a Dashboard";

    public void onSetUp()
    {
        super.onSetUp();
        //just need some data with issues in it.
        restoreData("TestAjaxIssuePicker.xml");
    }

    @Override
    protected void onTearDown() throws Exception
    {
        selectMainFrame();
        super.onTearDown();
    }

    public void testOnDashboardPopup() throws Exception
    {
        runSearchAndOpenOnDashboardDialog();

        assertDialogTabs("Filter Results");

        assertThat.elementPresentByTimeout("css=iframe#gadget-0", DROP_DOWN_WAIT);
        client.selectFrame("gadget-0");
        client.waitForAjaxWithJquery(TIMEOUT);
        // assert default configuration of gadget
        assertRowsInResultTable(10, 19);
        assertResultTableHeadings("T", "Key", "Summary", "P");

        // edit the configuration to display some errors
        clickEditGadgetConfiguration();
        assertThat.elementPresentByTimeout("num", DROP_DOWN_WAIT);
        client.type("num", "xxx");
        clickSaveGadgetConfiguration();

        // assert errors present
        assertThat.elementContainsText("jquery=div.field-group:first div.error", "The value must be an integer greater than 0 and less than or equal to 50");

        // enter correct input
        client.type("num", "9");
        client.selectOption("columnNames", "Assignee");
        client.addSelection("columnNames", "Created");
        client.addSelection("columnNames", "Reporter");
        clickSaveGadgetConfiguration();

        assertRowsInResultTable(9, 19);
        assertResultTableHeadings("Assignee", "Created", "Reporter");

        selectMainFrame();
        clickSaveToDashboardButton();
        assertThat.textPresentByTimeout(DEFAULT_DASHBOARD_MESSAGE, TIMEOUT);

        //first try saving without naming a filter
        client.click("save-btn1");
        assertThat.textPresentByTimeout("You must specify a name to save this filter as.", TIMEOUT);
        client.type("filterName", FILTER_NAME);
        client.click("save-btn1", true);

        //wait for dashboard to load
        client.waitForAjaxWithJquery(TIMEOUT);

        // assert the gadget appears as configured
        client.selectFrame("gadget-10010");
        assertRowsInResultTable(9, 19);
        assertResultTableHeadings("Assignee", "Created", "Reporter");
    }

    public void testOnDashboardPopupWithEscapedCharactersInJql() throws Exception
    {
        runSearchAndOpenOnDashboardDialog("project = mky and (summary ~ '&' OR summary ~ '\"monkey\"')");

        assertDialogTabs("Filter Results");

        client.selectFrame("gadget-0");
        client.waitForAjaxWithJquery(TIMEOUT);
        // assert default configuration of gadget
        assertRowsInResultTable(3, 3);
        assertResultTableHeadings("T", "Key", "Summary", "P");

        // edit the configuration to display some errors
        clickEditGadgetConfiguration();
        client.type("num", "xxx");
        clickSaveGadgetConfiguration();

        // assert errors present
        assertThat.elementContainsText("jquery=div.field-group:first div.error", "The value must be an integer greater than 0 and less than or equal to 50");

        // enter correct input
        client.type("num", "2");
        client.selectOption("columnNames", "Assignee");
        client.addSelection("columnNames", "Created");
        client.addSelection("columnNames", "Reporter");
        clickSaveGadgetConfiguration();

        assertRowsInResultTable(2, 3);
        assertResultTableHeadings("Assignee", "Created", "Reporter");

        selectMainFrame();
        clickSaveToDashboardButton();
        assertThat.textPresentByTimeout(DEFAULT_DASHBOARD_MESSAGE, TIMEOUT);

        //first try saving without naming a filter
        client.click("save-btn1");
        assertThat.textPresentByTimeout("You must specify a name to save this filter as.", TIMEOUT);
        client.type("filterName", FILTER_NAME);
        client.click("save-btn1", true);

        //wait for dashboard to load
        client.waitForAjaxWithJquery(TIMEOUT);

        // assert the gadget appears as configured
        client.selectFrame("gadget-10010");
        assertRowsInResultTable(2, 3);
        assertResultTableHeadings("Assignee", "Created", "Reporter");
    }

    public void testChartsPopup()
    {
        runSearchAndOpenChartsDialog();

        assertDialogTabs("Average Age Chart");

        //cycle through all the gadgets and make sure the iframe appears
        for (int i = 0 ; i < 8; i++)
        {
            clickDialogTab(i);
        }

        clickSaveToDashboardButton();
        assertThat.textPresentByTimeout(DEFAULT_DASHBOARD_MESSAGE, TIMEOUT);

        //first try saving without naming a filter
        client.click("save-btn1");
        assertThat.textPresentByTimeout("You must specify a name to save this filter as.", TIMEOUT);
        client.type("filterName", FILTER_NAME);
        client.click("save-btn1", true);

        //wait for dashboard to load
        waitFor(10000);
        assertThat.textPresentByTimeout("Time Since Chart: " + FILTER_NAME, TIMEOUT);

        getNavigator().gotoHome();
        runSearchAndOpenChartsDialog();

        //now click Save to dashboard
        clickSaveToDashboardButton();
        assertThat.textPresentByTimeout(SAVE_TO_DASHBOARD_TITLE, TIMEOUT);
        assertThat.textPresentByTimeout("Select dashboard", TIMEOUT);

        //try saving the same filter again and check that the validation picks it up!
        client.type("filterName", FILTER_NAME);
        client.click("save-btn1");
        assertThat.textPresentByTimeout("Filter with same name already exists.", TIMEOUT);

        //now add another dashboard so we can try adding a chart to that dashboard specifically
        getNavigator().gotoPage("secure/ConfigurePortalPages!default.jspa?view=favourite", true);
        client.click("create_page", true);
        client.type("portalPageName", "Chart Dashboard");
        client.click("submit", true);

        runSearchAndOpenChartsDialog();

        clickSaveToDashboardButton();
        assertThat.textPresentByTimeout(SAVE_TO_DASHBOARD_TITLE, TIMEOUT);
        assertThat.textPresentByTimeout("Select dashboard", TIMEOUT);

        //select the dashbaord we just created and save it to that dashboard
        client.select("portalId", "label=Chart Dashboard");
        client.type("filterName", "Another wonky filter");
        client.click("save-btn1");

        //wait for dashboard to load
        waitFor(10000);
        assertThat.textPresentByTimeout("Average Age Chart: Another wonky filter", TIMEOUT);
        final String tabName = client.getText("jquery=#dashboard ul.tabs li.active");
        assert("Chart Dashboard".startsWith(tabName.replace("...", "")));

        //JRA-18909: Also check the current filter in the issues dropdown is the newly added filter
        client.click("find_link_drop");
        assertThat.textPresentByTimeout("Current Filter", TIMEOUT);
        assertThat.elementContainsText("jquery=#issues_current_main a", "Another wonky filter");
    }

    public void testChartsPopupUsingConfiguredGadget()
    {
        runSearchAndOpenChartsDialog();

        assertDialogTabs("Average Age Chart");

        // select the Average Age Chart
        clickDialogTab(1);

        // configure the gadget
        client.selectFrame("gadget-1");
        clickEditGadgetConfiguration();

        // set the Period
        client.selectOption("periodName", "Quarterly");
        clickSaveGadgetConfiguration();

        //now click Save to dashboard
        selectMainFrame();
        clickSaveToDashboardButton();
        assertThat.textPresentByTimeout(DEFAULT_DASHBOARD_MESSAGE, TIMEOUT);
        client.type("filterName", FILTER_NAME);
        client.click("save-btn1", true);

        //wait for dashboard to load
        waitFor(10000);
        assertThat.textPresentByTimeout("Average Age Chart: " + FILTER_NAME, TIMEOUT);

        // assert chart is configured
        client.selectFrame("gadget-10010");
        assertThat.textPresentByTimeout("Quarterly", TIMEOUT);
    }

    private void selectMainFrame()
    {
        client.selectFrame("relative=top");
    }

    private void clickEditGadgetConfiguration()
    {
        client.click("//div[@class='footer']/div[1]/a[1]");
        client.click("css=div.footer li.configure a");
    }

    private void clickSaveGadgetConfiguration()
    {
        client.click("jquery=div.buttons > input.save");
        client.waitForAjaxWithJquery(TIMEOUT);
    }

    private void runSearchAndOpenChartsDialog()
    {
        runSearchAndOpenDialog("project = HSP", "charts");
    }

    private void runSearchAndOpenOnDashboardDialog()
    {
        runSearchAndOpenOnDashboardDialog("project = HSP");
    }

    private void runSearchAndOpenOnDashboardDialog(final String jqlSearch)
    {
        runSearchAndOpenDialog(jqlSearch, "onDashboard");
    }

    private void runSearchAndOpenDialog(final String jqlSearch, final String linkId)
    {
        getNavigator().findIssuesWithJql(jqlSearch);
        client.click("jquery=#viewOptions span");
        client.click(linkId);
        visibleByTimeoutWithDelay(CHARTS_CONTAINER, TIMEOUT);
    }

    private void assertDialogTabs(final String selectedGadget)
    {
        assertThat.elementContainsText(CHARTS_CONTAINER, "Filter Results");
        assertThat.elementContainsText(CHARTS_CONTAINER, "Average Age Chart");
        assertThat.elementContainsText(CHARTS_CONTAINER, "Created vs Resolved Chart");
        assertThat.elementContainsText(CHARTS_CONTAINER, "Heat Map");
        assertThat.elementContainsText(CHARTS_CONTAINER, "Pie Chart");
        assertThat.elementContainsText(CHARTS_CONTAINER, "Recently Created Chart");
        assertThat.elementContainsText(CHARTS_CONTAINER, "Resolution Time");
        assertThat.elementContainsText(CHARTS_CONTAINER, "Time Since Chart");

        // assert the selected gadget
        assertThat.elementContainsText(CHARTS_CONTAINER + " ul.dialog-page-menu li.selected button.item-button:first", selectedGadget);
    }

    /**
     * @param i the button index (zero-based)
     */
    private void clickDialogTab(final int i)
    {
        client.click(String.format("jquery=#charts-dialog ul.dialog-page-menu li:nth-child(%d) button:first", i+1));
        assertThat.elementPresentByTimeout("jquery=#gadget-" + i, TIMEOUT);
    }

    private void clickSaveToDashboardButton()
    {
        client.click("jquery=#charts-dialog button.save-to-dashboard");
    }

    private void assertRowsInResultTable(final int numRows, final int totalResults)
    {
        assertThat.elementPresentByTimeout("jquery=#filter-results-content .results-count:contains(" 
                + String.format("Displaying issues 1 to %d of %d", numRows, totalResults) + ")", DROP_DOWN_WAIT);
    }

    private void assertResultTableHeadings(final String... headings)
    {
        for (String heading : headings)
        {
            assertThat.elementPresentByTimeout("jquery=#issuetable tr.rowHeader:contains(" + heading + ")", DROP_DOWN_WAIT);
        }
    }
}
