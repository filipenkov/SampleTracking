package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import com.thoughtworks.selenium.SeleniumException;

/**
 * Selenium Test for the Favourite Filters Gadget.
 *
 * @since v4.0
 */
@SkipInBrowser(browsers={Browser.IE}) //JS Error - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestFavouriteFiltersGadget extends GadgetTest
{
    @Override
    protected void restoreGadgetData()
    {
        //do nothing.
    }

    public void testView() throws InterruptedException
    {
        restoreData("TestFavouriteFiltersGadgetNoFilters.xml");
        addGadget("Favourite Filters");
        waitForGadgetConfiguration();
        submitGadgetConfig();
        visibleByTimeoutWithDelay("//div[@class='filter-list-content']", TIMEOUT);
        _testNoFilters();
        client.selectWindow(null);
        createFilter();
        selectGadget("Favourite Filters");
        _testOneFilterWithIssueCount();
        _testNewIssueCountedAfterManualRefresh();
        _testAnonymousUser();
    }

    public void testViewWithFilterPermissionChange() throws InterruptedException
    {
        restoreData("TestFavouriteFiltersGadget.xml");
        getNavigator().login("nondev", "nondev");

        // favourite filters gadget should be here
        addGadget("Favourite Filters");
        waitForGadgetConfiguration();
        submitGadgetConfig();
        visibleByTimeoutWithDelay("//div[@class='filter-list-content']", TIMEOUT);

        assertFilterLinkPresent("public_filter");
        assertFilterLinkPresent("dev_filter");
        getSeleniumClient().selectFrame("relative=top");

        // remove from devs group, shouldn't see dev filter

        getWebUnitTest().removeUserFromGroup("nondev", "jira-developers");
        getNavigator().currentDashboard().view(); // refresh
        waitFor(5000);

        selectGadget("Favourite Filters");
        assertFilterLinkPresent("public_filter");
        assertFilterLinkNotPresent("dev_filter");
    }

    public void testViewWithNoIssueCounts() throws InterruptedException
    {
        restoreData("BaseGadgetData.xml");
        addGadget("Favourite Filters");
        waitForGadgetConfiguration();
        getSeleniumClient().select("showCounts", "label=No");
        getSeleniumClient().click("//input[@value='Save']");
        _testNoFilters();
        client.selectWindow(null);
        createFilter();
        selectGadget("Favourite Filters");
        _testOneFilterWithNoIssueCount();
    }

    private void createFilter()
    {
        getNavigator().findAllIssues();
        client.click("//a[@id='filtersavenew']", true);
        client.type("filterName", "testFilter");
        client.type("filterDescription", "some desc");
        client.click("submit", true);

        getNavigator().currentDashboard().view();
    }

    private void _testNewIssueCountedAfterManualRefresh()
            throws InterruptedException
    {
        getWebUnitTest().getNavigation().login(ADMIN_USERNAME);
        getWebUnitTest().getNavigation().issue().createIssue("monkey", "Bug", "More monkeys!");
        getSeleniumClient().selectFrame("relative=top");
        getNavigator().gotoHome();
        selectGadget("Favourite Filters");

        waitForIssueCount(2);
    }

    private void _testOneFilter()
    {
        clickRefreshButton();
        assertThat.elementPresentByTimeout("//span[@class='filter-name' and text()='testFilter']", 5000);
        assertThat.linkVisibleWithText("Create Filter");
        assertThat.linkVisibleWithText("Manage Filters");
    }

    private void assertFilterLinkPresent(String name)
    {
        assertThat.elementPresentByTimeout("//div[@class='filter-list-content']", 5000);
        // linkVisibleWithText does not work here as the anchor element contains a div, which contains the asserted text
        // assertThat.linkVisibleWithText("testFilter");
        assertThat.elementPresent("//span[@class='filter-name' and text()='" + name + "']");
    }

    private void assertFilterLinkNotPresent(String name)
    {
        assertThat.elementPresentByTimeout("//div[@class='filter-list-content']", 5000);
        // linkVisibleWithText does not work here as the anchor element contains a div, which contains the asserted text
        // assertThat.linkVisibleWithText("testFilter");
        assertThat.elementDoesntHaveText("css=div.filter-list-content ul li a", name);
    }

    private void _testOneFilterWithIssueCount()
    {
        _testOneFilter();
        assertThat.textPresent("1"); // TODO assert this number is text in the right element, by class
    }

    private void _testOneFilterWithNoIssueCount()
    {
        _testOneFilter();
        assertThat.elementDoesntHaveText("//span[@class='filter-count']", "1");
    }

    private void _testNoFilters()
    {
        assertThat.elementPresentByTimeout("//div[@class='filter-list-content']", 10000);
        assertThat.linkVisibleWithText("Create Filter");
        assertThat.linkVisibleWithText("Manage Filters");
        assertThat.elementContainsText("//div[@class='filter-list-content']", "You have no favourite filters at the moment.");
    }

    private void waitForIssueCount(int count)
            throws InterruptedException
    {
        clickRefreshButton();
        for (int x = 0; x < 10; x++)
        {
            assertThat.elementPresentByTimeout("//div[@class='filter-list-content']", 5000);
            if (getSeleniumClient().isElementPresent("//span[@class='filter-count' and .='" + count + "']"))
            {
                break;
            }
            else
            {
                Thread.sleep(1000);
                assertThat.elementPresentByTimeout("css=button.reload", 20000);
                getSeleniumClient().click("css=button.reload");
            }
        }
        assertThat.elementContainsText("//span[@class='filter-count']", "2");
    }

    private void _testAnonymousUser()
    {
        getSeleniumClient().selectFrame("relative=top");
        loginAsAdmin();
        getNavigator().gotoPage("secure/admin/jira/EditDefaultDashboard!default.jspa", true);
        addGadget("Favourite Filters");
        waitForGadgetConfiguration();
        submitGadgetConfig();
        visibleByTimeoutWithDelay("//div[@class='filter-list-content']", TIMEOUT);
        getSeleniumClient().selectFrame("relative=top");
        getNavigator().logout(getXsrfToken());
        int attempts = 0;
        while (attempts < 10)
        {
            try
            {
                getNavigator().gotoHome();
                break;
            }
            catch (SeleniumException ex)
            {
                // if at first you don't succeed....
                attempts++;
            }
        }
        assertTrue("too many selenium exceptions", attempts < 10);
        assertThat.textNotPresent("Favourite Filters");
    }
}
