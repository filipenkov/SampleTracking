package com.atlassian.jira.webtest.selenium.filters;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.gadgets.GadgetTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

import java.util.Arrays;

@SkipInBrowser(browsers={Browser.IE}) //JS Errors - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestFilterPopUp extends GadgetTest
{
    private static final String FILTER_PICKER_WINDOW = "filter_filterid_window";

    @Override
    protected void restoreGadgetData()
    {
        restoreData("TestFilterPicker.xml");
    }

    public static Test suite()
    {
        return suiteFor(TestFilterPopUp.class);
    }

    public void testPopUpCreatePortlet() throws Exception
    {
        getNavigator().gotoHome();

        client.click("add-gadget");
        assertThat.visibleByTimeout("id=macro-browser-dialog", PAGE_LOAD_WAIT_TIME);

        //now add the gadget
        client.click("//li[@id='macro-FilterResults']/div/input[1]");
        //TODO: There should be a better way to do this rather than just wait 5secs
        waitFor(5000);

        //close the dialog
        client.click("//div[@id='macro-browser-dialog']/div[1]/div[2]/button[3]");
        assertThat.notVisibleByTimeout("id=macro-browser-dialog", PAGE_LOAD_WAIT_TIME);

        assertThat.visibleByTimeout("filter_filterId_advance", TIMEOUT);
        client.click("filter_filterId_advance");
        // switch to popup window
        client.waitForPopUp("filter_filterId_window", PAGE_LOAD_WAIT);
        client.selectWindow("filter_filterId_window");

        assertThat.textPresent("Filter Picker");
        assertThat.textPresent("Select a filter from the filter directory.");

        // there are no favourites so we should be on the search tab
        assertThat.elementNotPresent("link=Search");
        client.click("name=Search", true); // search for all

        assertThat.elementPresent("id=filterlink_10001"); // should be homofilter
        client.click("filterlink_10001");

        // assert the window closed
        assertFalse(Arrays.asList(client.getAllWindowNames()).contains(FILTER_PICKER_WINDOW));

        // now go back to the original window
        selectOriginalWindow();

        // assert the homofilter filter is set
        assertEquals("homofilter", client.getText("filter_filterId_name"));
    }

    public void testPopUpEditPortlet()
    {
        getNavigator().gotoHome();

        waitFor(5000);
        assertThat.elementHasText("id=stats-gadget-project-or-filter-name", "allfilter");

        client.selectFrame("gadget-10030");
        clickConfigButton();

        assertThat.elementPresentByTimeout("id=filter_projectOrFilterId_advance", TIMEOUT);
        assertThat.visibleByTimeout("id=filter_projectOrFilterId_advance", TIMEOUT);
        client.click("id=filter_projectOrFilterId_advance");
        // switch to popup window
        client.waitForPopUp("filter_projectOrFilterId_window", PAGE_LOAD_WAIT);
        client.selectWindow("filter_projectOrFilterId_window");

        assertThat.textPresent("Filter or Project Picker");
        assertThat.textPresent("Select a filter or project from the directory.");

        // there are no favourites so we should be on the search tab
        assertThat.elementNotPresent("link=Search");
        client.click("link=Popular", true); // search for all

        client.click("filterlink_10001");

        // now go back to the original window
        selectOriginalWindow();
        // assert the window closed
        assertFalse(Arrays.asList(client.getAllWindowNames()).contains(FILTER_PICKER_WINDOW));

        client.selectFrame("gadget-10030");
        client.click("//input[@value='Save']");
        waitFor(2000);
        assertThat.elementHasText("id=stats-gadget-project-or-filter-name", "homofilter");
    }

    private void selectOriginalWindow()
    {
        // http://release.openqa.org/selenium-remote-control/0.9.2/doc/java/com/thoughtworks/selenium/Selenium.html#selectWindow(java.lang.String)
        client.selectWindow(null);
    }
}
