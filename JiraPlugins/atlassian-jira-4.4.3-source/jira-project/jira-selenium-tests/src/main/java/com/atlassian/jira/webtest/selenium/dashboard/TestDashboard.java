package com.atlassian.jira.webtest.selenium.dashboard;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.harness.util.Dashboard;

/**
 * Tests the new dashboard for JIRA 4.0
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestDashboard extends JiraSeleniumTest
{
    private static final String DASHBOARD_NAME = "19 Gadget Dashboard";
    private static final String TOO_MANY_GADGETS_TAB_NAME = "Too many gadgets";

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestDashboard.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testExceedMaxGadgetsPerDashboard()
    {
        //view a dashboard with 19 gadgets
        final Dashboard dashboard = getNavigator().dashboard("10010");
        dashboard.view();

        assertTrue(client.getTitle().contains(DASHBOARD_NAME));

        final String tabName = client.getText("//ul[contains(@class, 'tabs')]/li[contains(@class, 'active')]");
        assert (DASHBOARD_NAME.startsWith(tabName.replace("...", "")));
        assertEquals(19, dashboard.getGadgetCount());

        //the add up to the limit
        addGadget();
        assertEquals(20, dashboard.getGadgetCount());

        //finally add one more.  Should get an error
        client.click("add-gadget");
        assertThat.visibleByTimeout("id=macro-browser-dialog", PAGE_LOAD_WAIT_TIME);

        //now add the gadget
        client.click("//li[@id='macro-Admin']/div/input[1]");
        //TODO: There should be a better way to do this rather than just wait 5secs
        waitFor(5000);
        final String error = client.getAlert();

        assertEquals("Sorry, you have exceeded the maximum number of gadgets supported by your dashboard.  You must remove some gadgets before proceeding.", error);
        assertEquals(20, dashboard.getGadgetCount());
    }

    public void testViewDashboardWithTooManyGadgets()
    {
        //this dashboard has more than the limit. Should still be able to view it.
        final Dashboard dashboard = getNavigator().dashboard("10020");
        dashboard.view();

        assertTrue(client.getTitle().contains(TOO_MANY_GADGETS_TAB_NAME));

        final String tabName = client.getText("//ul[contains(@class, 'tabs')]/li[contains(@class, 'active')]");
        assert (TOO_MANY_GADGETS_TAB_NAME.startsWith(tabName.replace("...", "")));

        assertEquals(21, dashboard.getGadgetCount());
    }

    private void addGadget()
    {
        client.click("add-gadget");
        assertThat.visibleByTimeout("id=macro-browser-dialog", PAGE_LOAD_WAIT_TIME);

        //now add the gadget
        client.click("//li[@id='macro-Admin']/div/input[1]");
        //TODO: There should be a better way to do this rather than just wait 5secs
        waitFor(5000);

        //close the dialog
        client.click("//div[@id='macro-browser-dialog']/div[1]/div[2]/button[3]");
        assertThat.notVisibleByTimeout("id=macro-browser-dialog", PAGE_LOAD_WAIT_TIME);
    }
}
