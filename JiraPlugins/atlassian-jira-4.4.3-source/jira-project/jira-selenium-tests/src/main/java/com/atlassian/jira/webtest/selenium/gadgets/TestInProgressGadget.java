package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

/**
 * Blatant copy and paste job of TestAssignedToMeGadget
 */
@SkipInBrowser(browsers={Browser.IE}) //textPresent is not case sensitive in IE - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestInProgressGadget extends GadgetTest
{
    private static final String ISSUES_IN_PROGRESS = "Issues in progress";

    public void onSetUp()
    {
        super.onSetUp();
        addGadget(ISSUES_IN_PROGRESS);
    }

    public void testConfigureAndView()
    {
        createMonkeyIssue();
        selectGadget(ISSUES_IN_PROGRESS);

        _testWithNoInProgressIssues();
        selectDashboardFrame();
        beginMonkeyIssue();

        _testFilterWithOneIssueAndDefaultColumns();
        _testFilterWithoutDefaultColumns();
        _testFilterWithDefaultAndExtraColumn();
        _testFilterWithDefaultAndExtraColumnThatIsInDefault();
    }

    private void beginMonkeyIssue()
    {
        getNavigator().gotoIssue("MKY-1");
        client.clickLinkWithText("Start Progress", true);
        getNavigator().gotoHome();
    }

    private void _testFilterWithOneIssueAndDefaultColumns()
    {
        selectDashboardFrame();
        getNavigator().gotoHome();
        selectGadget(ISSUES_IN_PROGRESS);

        clickConfigAndWait();

        submitGadgetConfig();
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

    private void _testFilterWithoutDefaultColumns()
    {
        clickConfigAndWait();

        //This will unselect Default
        getSeleniumClient().select("columnNames", "Project");
        submitGadgetConfig();

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
        clickConfigAndWait();

        getSeleniumClient().select("columnNames", "Project");
        getSeleniumClient().addSelection("columnNames", "Default Columns");
        submitGadgetConfig();
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
        clickConfigAndWait();

        getSeleniumClient().addSelection("columnNames", "Key");
        submitGadgetConfig();
        assertThat.elementPresentByTimeout("//a[text()='monkey']", 10000);

        //These really shouldn't be necessary, but despite the assertion above they still fail. WTF?
        waitFor(2500);

        assertThat.textPresent("Key");
        assertThat.textPresent("Summary");
        assertThat.textPresent("More monkeys!");
        assertThat.textPresent("P");
        assertThat.textPresent("T");
        //Should be 5 columns if there are no duplicates
        assertEquals(6, client.getXpathCount("//table[@id='issuetable']/tbody/tr[1]/td"));
    }

    private void createMonkeyIssue()
    {
        getWebUnitTest().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getWebUnitTest().getNavigation().issue().createIssue("monkey", "Bug", "More monkeys!");
        selectDashboardFrame();
        getNavigator().gotoHome();
    }

    private void _testWithNoInProgressIssues()
    {
        clickConfigAndWait();
        submitGadgetConfig();
        waitFor(2500);
        assertThat.textPresent("No matching issues");
    }

    private void clickConfigAndWait()
    {
        clickConfigButton();
        waitForGadgetConfiguration();
        assertThat.visibleByTimeout("//div[@class='buttons']/input[1]", 10000);
    }
}
