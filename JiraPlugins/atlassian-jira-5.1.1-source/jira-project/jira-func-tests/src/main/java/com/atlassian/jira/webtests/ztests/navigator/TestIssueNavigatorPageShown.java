package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.WebTesterFactory;
import net.sourceforge.jwebunit.WebTester;

@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR })
public class TestIssueNavigatorPageShown extends FuncTestCase
{
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssueNavigatorPageShown.xml");
    }

    public void testOpensToCorrectPage()
    {
        navigation.issueNavigator().displayAllIssues();
        assertNumberOfIssues(1, 50, 250);

       //JRADEV-2584 - Now you always select the top row of any page on a brand new search
        assertTopRowSelected();

        // Jumping directly to a certain page after viewing an issue.
        navigation.issue().viewIssue("TEST-99");
        returnToSearch();
        assertNumberOfIssues(151, 200, 250);
        assertIssueSelected("TEST-99");

        // Navigation from the view issue page across page boundaries (backwards).
        navigation.issue().viewIssue("TEST-99");
        previousIssue();
        previousIssue();
        previousIssue();
        assertViewingIssue("TEST-102");
        returnToSearch();
        assertNumberOfIssues(101, 150, 250);
        assertIssueSelected("TEST-102");

        // Navigation from the view issue page across page boundaries (forwards).
        navigation.issue().viewIssue("TEST-201");
        nextIssue();
        nextIssue();
        nextIssue();
        assertViewingIssue("TEST-198");
        returnToSearch();
        assertNumberOfIssues(51, 100, 250);
        assertIssueSelected("TEST-198");

        // Deleting an issue falls back to selecting the next issue.
        navigation.issue().viewIssue("TEST-175");
        tester.clickLink("delete-issue");
        tester.submit("Delete");
        assertNumberOfIssues(51, 100, 249);
        assertIssueSelected("TEST-174");

        // Deleting an issue and the next issue in the pager should fall back to the page that the issue would have been on.
        navigation.issue().viewIssue("TEST-174");

        // Delete TEST-174 and TEST-173 in another window.
        WebTester otherWindow = newWebTester();
        final int TEST_174_ID = 11243;
        deleteIssues(otherWindow, TEST_174_ID, 2);

        // Now back in the main window (selects first issue on page that TEST-174 was on).
        assertViewingIssue("TEST-174");
        returnToSearch();
        assertNumberOfIssues(51, 100, 247);
        assertIssueSelected("TEST-200");
    }

    /** Test for bug JRADEV-2262 */
    public void testDeletingLoneIssueOnLastPage()
    {
        navigation.issue().createIssue("TEST", "Bug", "foo bar");
        navigation.issueNavigator().displayAllIssues();
        tester.clickLink("page_6");
        assertNumberOfIssues(251, 251, 251);

        navigation.issue().viewIssue("TEST-1");
        tester.clickLink("delete-issue");
        tester.submit("Delete");

        assertNumberOfIssues(1, 50, 250);
    }

    private void deleteIssues(WebTester webTester, long startingIssueId, long numberToDelete)
    {
        for (long i = 0, issueId = startingIssueId; i < numberToDelete; i++, issueId--)
        {
            webTester.beginAt("/secure/DeleteIssue!default.jspa?id=" + issueId);
            webTester.submit("Delete");
        }
    }

    private WebTester newWebTester()
    {
        final WebTester tester = WebTesterFactory.createNewWebTester(environmentData);

        tester.beginAt("/login.jsp");
        tester.setFormElement("os_username", ADMIN_USERNAME);
        tester.setFormElement("os_password", ADMIN_USERNAME);
        tester.setWorkingForm("login-form");
        tester.submit();

        return tester;
    }

    private void assertViewingIssue(String issueKey)
    {
        tester.assertTextInElement("key-val", issueKey);
    }

    private void nextIssue()
    {
        tester.clickLink("next-issue");
    }

    private void previousIssue()
    {
        tester.clickLink("previous-issue");
    }

    private void returnToSearch() {
        tester.clickLink("return-to-search");
    }

    private void assertNumberOfIssues(int from, int to, int total)
    {
        text.assertTextPresent(
                new XPathLocator(tester, "//div[@class='results-count aui-item']"),
                String.format("%d to %d of %d", from, to, total)
        );
    }

    private void assertIssueSelected(String issueKey)
    {
        assertTrue(new XPathLocator(tester, String.format("//tr[contains(@class, 'focused') and @data-issuekey='%s']", issueKey)).exists());
    }

    private void assertTopRowSelected()
    {
        assertTrue(new XPathLocator(tester, "//tr[contains(@class, 'focused') and position()=1]").exists());
    }

}
