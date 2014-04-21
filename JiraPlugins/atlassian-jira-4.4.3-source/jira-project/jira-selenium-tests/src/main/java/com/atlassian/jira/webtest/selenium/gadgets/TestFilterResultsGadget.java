package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import junit.framework.Test;

/**
 * Selenium Test for the Filter Results Gadget.
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestFilterResultsGadget extends GadgetTest
{
    public void onSetUp()
    {
        super.onSetUp();
        addGadget("Filter Results");
    }

    public static Test suite()
    {
        return suiteFor(TestFilterResultsGadget.class);
    }

    public void testConfigureAndView()
    {
        _testBadConfiguration();

        _testFilterWithNoIssues();

        _testFilterWithOneIssue();

        _testFilterWithTwoIssues();

        _testFilterWithColumnsConfigured();

        _testFilterWithTwoIssuesAndPaging();
    }

    private void _testBadConfiguration()
    {
        waitForGadgetConfiguration();
        getSeleniumClient().typeInElementWithName("num", "0");
        getSeleniumClient().click("//input[@value='Save']");
        assertThat.elementPresentByTimeout("//div[@class='error' and contains(text(), 'greater than 0')]", 5000);
        getSeleniumClient().typeInElementWithName("num", "500");
        getSeleniumClient().click("//input[@value='Save']");
        assertThat.elementPresentByTimeout("//div[@class='error' and contains(text(), 'Number must not exceed 50')]", 5000);
        getSeleniumClient().typeInElementWithName("num", "1");
        getSeleniumClient().click("//input[@value='Save']");
    }

    private void _testFilterWithNoIssues()
    {
        getSeleniumClient().selectFrame("relative=top");
        getWebUnitTest().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getWebUnitTest().gotoIssue("");
        getWebUnitTest().saveFilter("testFilter", "some desc");
        getNavigator().gotoHome();
        assertThat.textNotPresent("No Filter selected");
        selectGadget("Filter Results");
        selectProjectOrFilterFromAutoComplete("quickfind", "test", "filter-10000");
        clickSaveAndWaitForView();
        assertGadgetTitle("Filter Results: testFilter");
        assertThat.textPresent("No matching issues found");
    }

    private void _testFilterWithOneIssue()
    {
        getWebUnitTest().getNavigation().issue().createIssue("monkey", "Bug", "More monkeys!");
        getSeleniumClient().selectFrame("relative=top");
        getNavigator().gotoHome();
        selectGadget("Filter Results");
        waitForGadgetView("filter-results-content");
        assertGadgetTitle("Filter Results: testFilter");
        assertThat.linkPresentWithText("More monkeys!");
        assertThat.linkPresentWithText("MKY-1");
    }

    private void _testFilterWithTwoIssues()
    {
        getWebUnitTest().getNavigation().issue().createIssue("monkey", "Bug", "More monkeys2!");
        getSeleniumClient().selectFrame("relative=top");
        getNavigator().gotoHome();
        selectGadget("Filter Results");
        waitForGadgetView("filter-results-content");
        assertThat.linkPresentWithText("More monkeys2!");
        assertThat.linkPresentWithText("MKY-2");
        assertThat.linkPresentWithText("More monkeys!");
        assertThat.linkPresentWithText("MKY-1");
    }

    private void _testFilterWithColumnsConfigured()
    {
        // only choose one column - project
        clickConfigAndWait();
        getSeleniumClient().select("columnNames", "Project");
        clickSaveAndWaitForView();

        assertThat.elementPresentByTimeout("//a[text()='monkey']", 5000);
        assertThat.textPresent("Project");
        assertThat.elementVisible("css=.headerrow-project");
        assertThat.elementNotPresent("css=.nav.issuekey");

        // choose project column + defaults
        clickConfigAndWait();
        getSeleniumClient().select("columnNames", "Project");
        getSeleniumClient().addSelection("columnNames", "Default Columns");
        clickSaveAndWaitForView();

        assertThat.elementPresentByTimeout("//a[text()='monkey']", 5000);
        assertThat.linkPresentWithText("MKY-2");
        assertThat.linkPresentWithText("MKY-1");
        assertThat.textPresent("T");
        assertThat.textPresent("Key");
        assertThat.textPresent("Summary");
        assertThat.textPresent("P");
        assertThat.textPresent("Project");

        // restore default
        clickConfigAndWait();
        getSeleniumClient().select("columnNames", "Default Columns");
        clickSaveAndWaitForView();
    }

    private void _testFilterWithTwoIssuesAndPaging()
    {
        clickConfigAndWait();
        getSeleniumClient().typeInElementWithName("num", "1");
        clickSaveAndWaitForView();

        assertThat.elementPresentByTimeout("//a[text()='2']", 5000);
        assertThat.linkPresentWithText("MKY-2");
        assertThat.linkPresentWithText("2");
        assertThat.linkPresentWithText("Next >>");
        assertThat.linkNotPresentWithText("MKY-1");
        getSeleniumClient().clickLinkWithText("2", false);
        assertThat.elementPresentByTimeout("//a[text()='1']", 5000);
        assertThat.linkPresentWithText("MKY-1");
        assertThat.linkPresentWithText("1");
        assertThat.linkPresentWithText("<< Previous");
        assertThat.linkNotPresentWithText("MKY-2");
    }

    private void clickConfigAndWait()
    {
        clickConfigButton();
        waitForGadgetConfiguration();
    }

    private void clickSaveAndWaitForView()
    {
        getSeleniumClient().click("//input[@value='Save']");
        waitForGadgetView("filter-results-content");
    }
}
