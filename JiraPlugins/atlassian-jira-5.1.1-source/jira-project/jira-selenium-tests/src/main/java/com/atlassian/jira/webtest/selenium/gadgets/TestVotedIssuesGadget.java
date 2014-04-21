package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

/**
 * Selenium Test for the Voted Issues Gadget.
 *
 * @since v4.0
 */
@SkipInBrowser(browsers={Browser.IE}) //isTextPresent is not case sensitive in IE - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestVotedIssuesGadget extends GadgetTest
{
    private static final String VOTED_ISSUES = "Voted Issues";

    public void onSetUp()
    {
        super.onSetUp();
        addGadget(VOTED_ISSUES);
    }

    public void testView()
    {
        _testNoVotedIssues();

        _testConfigureError();

        _testTwoIssuesWithDefaults();

        _testTwoIssuesWithResolvedAndVotes();

        _testSorting();

        _testDefaultColumnsWithAdditionalColumn();

        _testAnonymous();

        _click_display_config_text();
    }

    // JRA-20638. Ensure that clicking on text toggles correct field
    private void _click_display_config_text()
    {
        selectDashboardFrame();
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        selectGadget("Voted Issues");
        clickConfigButton();
        waitForGadgetConfiguration();

        getSeleniumClient().clickElementWithXpath("//input[@name='showResolved']/following-sibling::label");
        assertThat.elementPresent("jquery=input[name=showResolved]:checked");
        // make sure we don't toggle the other one
        assertThat.elementNotPresent("jquery=input[name=showTotalVotes]:checked");

        getSeleniumClient().clickElementWithXpath("//input[@name='showTotalVotes']/following-sibling::label");
        assertThat.elementPresent("jquery=input[name=showTotalVotes]:checked");
        // make sure the other one stays the way it was
        assertThat.elementPresent("jquery=input[name=showResolved]:checked");
    }

    private void _testNoVotedIssues()
    {
        waitForGadgetConfiguration();
        submitGadgetConfig();

        waitForGadgetView("voted-content");
        assertThat.textPresent("You are not currently voting for any issues.");
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
        getWebUnitTest().getNavigation().login(ADMIN_USERNAME);
        getWebUnitTest().getNavigation().issue().createIssue("monkey", "Bug", "More monkeys!");
        getWebUnitTest().getNavigation().issue().createIssue("monkey", "Bug", "More monkeys2!");

        selectDashboardFrame();
        getNavigator().login("fred", "fred");
        getNavigator().gotoIssue("MKY-1");
        getSeleniumClient().click("jquery=#opsbar-operations_more");
        assertThat.visibleByTimeout("jquery=#toggle-vote-issue");
        getSeleniumClient().click("jquery=#toggle-vote-issue", false);
        waitFor(DROP_DOWN_WAIT);
        getNavigator().gotoIssue("MKY-2");
        getSeleniumClient().click("jquery=#opsbar-operations_more");
        assertThat.visibleByTimeout("jquery=#toggle-vote-issue");
        getSeleniumClient().click("jquery=#toggle-vote-issue", false);
        waitFor(DROP_DOWN_WAIT);
        getWebUnitTest().getNavigation().issue().resolveIssue("MKY-2", "Fixed", "With pleasure");

        getNavigator().gotoHome();
        addGadget(VOTED_ISSUES);
        waitForGadgetConfiguration();
        submitGadgetConfig();
        waitForGadgetView("voted-content");
        assertThat.textNotPresent("No matching issues found.");
        assertThat.linkPresentWithText("MKY-1");
        assertThat.linkNotPresentWithText("MKY-2");
        assertThat.textNotPresent("Votes");
    }

    private void _testSorting()
    {
        // FIXME: If the wait is removed, the test randomly(!) fails
        waitFor(5000);
        selectDashboardFrame();
        getNavigator().gotoHome();
        selectGadget(VOTED_ISSUES);

        waitFor(5000);
        // Check the ordering is by key desc
        assertThat.elementContainsText("//table[@id='issuetable']/tbody/tr[1]/td[2]", "MKY-2");
        assertThat.elementContainsText("//table[@id='issuetable']/tbody/tr[2]/td[2]", "MKY-1");

        client.clickElementWithXpath("//th[@rel='issuekey:ASC']");
        waitFor(5000);
        waitForGadgetView("voted-content");

        // Check that the sorting has changed
        assertThat.textNotPresent("No matching issues found.");
        assertThat.elementContainsText("//table[@id='issuetable']/tbody/tr[1]/td[2]", "MKY-1");
        assertThat.elementContainsText("//table[@id='issuetable']/tbody/tr[2]/td[2]", "MKY-2");
    }

    private void _testTwoIssuesWithResolvedAndVotes()
    {
        clickConfigButton();
        waitForGadgetConfiguration();
        // JRA-19139 might affect this
        client.check("showTotalVotes", "true");
        client.check("showResolved", "true");
        submitGadgetConfig();

        assertThat.elementPresentByTimeout("//a[text()='MKY-2']", 5000);
        assertThat.linkPresentWithText("MKY-1");
        assertThat.linkPresentWithText("MKY-2");
        assertThat.textPresent("Votes");
    }


    private void _testDefaultColumnsWithAdditionalColumn()
    {
        selectDashboardFrame();
        getNavigator().gotoHome();
        selectGadget(VOTED_ISSUES);

        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[1]", "T");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[2]", "Key");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[3]", "Summary");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[4]", "P");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[5]", "Status");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[6]", "Votes");

        clickConfigButton();
        waitForGadgetConfiguration();

        client.selectOption("columnNames", "Default Columns");
        client.addSelection("columnNames", "Created");
        submitGadgetConfig();

        waitForGadgetView("voted-content");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[1]", "T");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[2]", "Key");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[3]", "Summary");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[4]", "P");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[5]", "Status");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[6]", "Created");
        assertThat.elementContainsText("//table[@id='issuetable']/thead/tr[1]/th[7]", "Votes");
    }

    private void _testAnonymous()
    {
        // Voted Issues gadget is not visible to anonymous user
        selectDashboardFrame();
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();

        assertGadgetNotVisible(VOTED_ISSUES);
    }
}
