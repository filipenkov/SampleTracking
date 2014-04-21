package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.Quarantine;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

/**
 * Selenium test for the Assigned to Me Gadget.
 */
@SkipInBrowser(browsers={Browser.IE}) //textPresent is not case sensitive in IE - Responsibility: JIRA Team
@Quarantine
@WebTest({Category.SELENIUM_TEST })
public class TestAssignedToMeGadget extends GadgetTest
{

    public void onSetUp()
    {
        super.onSetUp();
        addGadget("AssignedtoMe", "Assigned to Me");
        client.deleteCookie("jira.issue.navigator.type", getEnvironmentData().getContext() + "/secure/");
    }

    public void testConfigureAndView()
    {
        _testWithNoIssues();

        createMonkeyIssue();

        _testFilterFitsInSimple();
        _testFilterWithOneIssueAndDefaultColumns();
        _testFilterWithoutDefaultColumns();
        _testFilterWithDefaultAndExtraColumn();
        _testFilterWithDefaultAndExtraColumnThatIsInDefault();

        createAnotherMonkeyIssue();

        _testSorting();
    }

    private void _testFilterFitsInSimple()
    {                                    
        getNavigator().gotoHome();

        assertThat.elementNotPresentByTimeout("css=#dashboard.initializing", 8000);

        client.click("//a[@title='Assigned to Me']", true);
        assertThat.elementPresentByTimeout("issue-filter", 50000);
        assertThat.elementNotPresent("jqltext");
    }

    private void _testSorting()
    {
        getNavigator().gotoHome();

        assertThat.elementNotPresentByTimeout("css=#dashboard.initializing", 8000);

        // Check the ordering is by key desc
        client.click("//th[@rel='issuekey:ASC']");
        visibleByTimeoutWithDelay("//th[@rel='issuekey:DESC']", TIMEOUT);
        assertThat.elementContainsText("//table[@id='issuetable']/tbody/tr[1]/td[2]", "MKY-1");
        assertThat.elementContainsText("//table[@id='issuetable']/tbody/tr[2]/td[2]", "MKY-2");

        client.click("//th[@rel='issuekey:DESC']");
        visibleByTimeoutWithDelay("//th[@rel='issuekey:ASC']", TIMEOUT);
        waitForGadgetView("assigned-to-me-content");

        // Check that the sorting has changed
        assertThat.elementContainsText("//table[@id='issuetable']/tbody/tr[1]/td[2]", "MKY-2");
        assertThat.elementContainsText("//table[@id='issuetable']/tbody/tr[2]/td[2]", "MKY-1");
    }

    private void _testFilterWithOneIssueAndDefaultColumns()
    {
        getNavigator().gotoHome();

        assertThat.elementNotPresentByTimeout("css=#dashboard.initializing", 8000);

        selectGadget("Assigned to Me");

        assertThat.elementPresentByTimeout("//a[text()='MKY-1']", 10000);

        //These really shouldn't be necessary, but despite the assertion above they still fail. WTF?
        waitFor(2500);

        assertThat.linkPresentWithText("More monkeys!");
        assertThat.linkPresentWithText("MKY-1");
        assertThat.textPresent("Key");
        assertThat.textPresent("Summary");
        assertThat.textPresent("More monkeys!");
        assertThat.textPresent("P");
        assertThat.textPresent("T");
    }

    private void waitEvenLongerForConfiguration()
    {
        waitForGadgetConfiguration();
        assertThat.visibleByTimeout("//div[@class='buttons']/input[@class='button']", 10000);
    }

    private void _testFilterWithoutDefaultColumns()
    {
        clickConfigButton();
        waitForGadgetConfiguration();
        getSeleniumClient().select("columnNames", "Project");
        submitGadgetConfig();
        waitForGadgetView("assigned-to-me-content");
        waitFor(2500);

        assertThat.elementPresentByTimeout("//a[text()='monkey']", 10000);

        //These really shouldn't be necessary, but despite the assertion above they still fail. WTF?
        waitFor(2500);

        assertThat.textPresent("Project");
        assertThat.textPresent("monkey");
        assertThat.textNotPresent("Key");
        assertThat.textNotPresent("Summary");
        assertThat.textNotPresent("More monkeys!");
        assertThat.textNotPresent("T");
    }

    private void _testFilterWithDefaultAndExtraColumn()
    {
        clickConfigButton();
        waitForGadgetConfiguration();
        getSeleniumClient().addSelection("columnNames", "Project");
        getSeleniumClient().addSelection("columnNames", "Default Columns");
        submitGadgetConfig();
        waitForGadgetView("assigned-to-me-content");
        waitFor(2500);
        assertThat.elementPresentByTimeout("//a[text()='monkey']", 10000);

        //These really shouldn't be necessary, but despite the assertion above they still fail. WTF?
        waitFor(2500);
        assertThat.textPresent("Project");
        assertThat.textPresent("monkey");
        assertThat.textPresent("Key");
        assertThat.textPresent("Summary");
        assertThat.textPresent("More monkeys!");
        assertThat.textPresent("P");
        assertThat.textPresent("T");
    }

    private void _testFilterWithDefaultAndExtraColumnThatIsInDefault()
    {
        clickConfigButton();
        waitForGadgetConfiguration();
        getSeleniumClient().addSelection("columnNames", "Key");
        submitGadgetConfig();
        waitForGadgetView("assigned-to-me-content");
        waitFor(2500);
        assertThat.elementPresentByTimeout("//a[text()='monkey']", 10000);

        //These really shouldn't be necessary, but despite the assertion above they still fail. WTF?
        waitFor(2500);

        assertThat.textPresent("Key");
        assertThat.textPresent("Summary");
//        assertThat.textPresent("More monkeys!");
        assertThat.textPresent("P");
        assertThat.textPresent("T");
        //Should be 5 columns if there are no duplicates
        assertEquals(6, client.getXpathCount("//table[@id='issuetable']/tbody/tr[1]/td"));

//        getSeleniumClient().getTable()

        int i = 0;
    }

    private void createMonkeyIssue()
    {
        getSeleniumClient().selectFrame("relative=top");
        getWebUnitTest().getNavigation().issue().createIssue("monkey", "Bug", "More monkeys!");
        getSeleniumClient().selectFrame("relative=top");
        getNavigator().gotoHome();
    }

    private void createAnotherMonkeyIssue()
    {
        getWebUnitTest().getNavigation().issue().createIssue("monkey", "New Feature", "Even More monkeys!");
        getSeleniumClient().selectFrame("relative=top");
        getNavigator().gotoHome();
    }

    private void _testWithNoIssues()
    {
        waitForGadgetConfiguration();
        submitGadgetConfig();
        waitForGadgetView("assigned-to-me-content");
        assertThat.textPresent("No matching issues");
    }
}