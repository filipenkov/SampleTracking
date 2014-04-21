package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Selenium Test for the Watched Issues Gadget.
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestWatchedIssuesGadget extends GadgetTest
{
    public void onSetUp()
    {
        super.onSetUp();
        addGadget("Watched Issues");
    }

    public void testView()
    {
        _testNoWatchedIssues();

        _testConfigureError();

        _testTwoIssuesWithDefaults();

        _testSorting();

        _testDefaultColumnsWithAdditionalColumn();

        _testAnonymous();
    }

    private void _testNoWatchedIssues()
    {
        waitForGadgetConfiguration();
        submitGadgetConfig();
        waitForGadgetView("watched-content");
        assertThat.textPresent("You are not currently watching any issues.");
    }

    private void _testConfigureError()
    {
        clickConfigButton();
        waitForGadgetConfiguration();
        getSeleniumClient().typeInElementWithName("num", "0");
        submitGadgetConfig();
        assertThat.elementPresentByTimeout("//div[@class='error' and contains(text(), 'greater than 0')]", 5000);
        getSeleniumClient().typeInElementWithName("num", "500");
        submitGadgetConfig();
        assertThat.elementPresentByTimeout("//div[@class='error' and contains(text(), 'must not exceed 50')]", 5000);
    }

    private void _testTwoIssuesWithDefaults()
    {
        selectDashboardFrame();
        getNavigator().createIssue("monkey", "Bug", "More monkeys!");
        getNavigator().createIssue("monkey", "Bug", "More monkeys2!");
        getNavigator().gotoIssue("MKY-1");
        getSeleniumClient().click("jquery=#opsbar-operations_more");
        assertThat.visibleByTimeout("jquery=#toggle-watch-issue");
        getSeleniumClient().click("jquery=#toggle-watch-issue", false);
        waitFor(DROP_DOWN_WAIT);
        getNavigator().gotoIssue("MKY-2");
        getSeleniumClient().click("jquery=#opsbar-operations_more");
        assertThat.visibleByTimeout("jquery=#toggle-watch-issue");
        getSeleniumClient().click("jquery=#toggle-watch-issue", false);
        waitFor(DROP_DOWN_WAIT);

        getNavigator().gotoHome();
        selectGadget("Watched Issues");
        waitForGadgetView("watched-content");
        assertThat.textNotPresent("You are not currently watching any issues.");
        assertThat.linkPresentWithText("MKY-1");
        assertThat.linkPresentWithText("MKY-2");
    }

    private void _testSorting()
    {
        selectDashboardFrame();
        getNavigator().gotoHome();
        waitFor(5000);
        // Check the ordering is by key desc
        assertThat.elementContainsText("//table[@id='issuetable']/tbody/tr[1]/td[2]", "MKY-2");
        assertThat.elementContainsText("//table[@id='issuetable']/tbody/tr[2]/td[2]", "MKY-1");

        client.clickElementWithXpath("//th[@rel='issuekey:ASC']");
        waitFor(5000);
        waitForGadgetView("watched-content");

        // Check that the sorting has changed
        assertThat.textNotPresent("You are not currently watching any issues.");
        assertThat.elementContainsText("//table[@id='issuetable']/tbody/tr[1]/td[2]", "MKY-1");
        assertThat.elementContainsText("//table[@id='issuetable']/tbody/tr[2]/td[2]", "MKY-2");
    }

    private void _testDefaultColumnsWithAdditionalColumn()
    {
        getNavigator().gotoHome();
        selectGadget("Watched Issues");

        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[1]", "T");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[2]", "Key");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[3]", "Summary");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[4]", "P");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[5]", "Status");

        clickConfigButton();
        waitForGadgetConfiguration();

        client.selectOption("columnNames", "Default Columns");
        client.addSelection("columnNames", "Created");
        submitGadgetConfig();

        waitForGadgetView("watched-content");

        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[1]", "T");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[2]", "Key");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[3]", "Summary");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[4]", "P");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[5]", "Status");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[6]", "Created");
    }

    private void _testAnonymous()
    {
        // Watched Issues gadget is not visible to anonymous user
        selectDashboardFrame();
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();

        assertGadgetNotVisible("Watched Issues");
    }
}
