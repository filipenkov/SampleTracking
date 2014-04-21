package com.atlassian.jira.webtest.selenium.navigator;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.Flaky;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.framework.pages.IssueNavigator;
import com.atlassian.selenium.SeleniumClient;
import junit.framework.Test;


/**
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
public class TestIssueNavigatorKeyboardNavigation extends JiraSeleniumTest
{
    private IssueNavigator issueNav;

    private static final long TIMEOUT = 5000;
    private static final int SET_SELECTED_ISSUE_AJAX_DELAY = 1500;
    private static final int PAGE_SIZE = 10;

    @Override
    public  void onSetUp()
    {
        super.onSetUp();
        restoreData("TestIssueNavigatorKeyboardNavigation.xml");
        issueNav = new IssueNavigator(context());
    }

    public static Test suite()
    {
        return suiteFor(TestIssueNavigatorKeyboardNavigation.class);
    }

    @Flaky
    public void testSelectIssuesAcrossPageBoundaries()
    {
        getNavigator().findAllIssues();
        issueNav.assertNumberOfIssues(1, 10, 30);

        // Previous issue from very first result does nothing.
        assertRowSelected(1, "HSP-30");
        issueNav.results().up();
        assertRowSelected(1, "HSP-30");

        // Next issue from the last result in the page loads the next page.
        for (int i = 0; i < PAGE_SIZE - 1; i++)
        {
            issueNav.results().down();
        }
        assertRowSelected(10, "HSP-21");
        issueNav.results().down();
        client.waitForPageToLoad();
        issueNav.assertNumberOfIssues(11, 20, 30);
        assertRowSelected(1, "HSP-20");

        // Previous issue from first result in 2nd page selects last issue on previous page.
        issueNav.results().up();
        client.waitForPageToLoad();
        issueNav.assertNumberOfIssues(1, 10, 30);
        assertRowSelected(10, "HSP-21");

        // Next issue from very last result does nothing.
        client.click("page_3", true);
        issueNav.assertNumberOfIssues(21, 30, 30);
        assertRowSelected(1, "HSP-10");
        for (int i = 0; i < PAGE_SIZE - 1; i++)
        {
            issueNav.results().down();
        }
        assertRowSelected(10, "HSP-1");
        issueNav.results().down();
        assertRowSelected(10, "HSP-1");

        // Any form of paging to the (directly) previous page selects last row:
        // 1. Pressing k from the first row (tested above).
        // 2. Page number link.
        client.click("page_2", true);
        issueNav.assertNumberOfIssues(11, 20, 30);
        assertRowSelected(10, "HSP-11");
        // 3. Previous page link.
        client.click("css=.icon-previous", true);
        issueNav.assertNumberOfIssues(1, 10, 30);
        assertRowSelected(10, "HSP-21");

        // Paging to a non-adjacent previous page should select the first row.
        client.click("page_3", true);
        issueNav.assertNumberOfIssues(21, 30, 30);
        assertRowSelected(1, "HSP-10");
        client.click("page_1", true);
        issueNav.assertNumberOfIssues(1, 10, 30);
        assertRowSelected(1, "HSP-30");

        // Any form of paging to a next page selects the first row.
        // 1. Pressing j from the last row (tested above).
        // 2. Page number link (plus non-adjacent next page).
        client.click("page_2", true);
        issueNav.assertNumberOfIssues(11, 20, 30);
        assertRowSelected(1, "HSP-20");
        client.click("page_1", true);
        client.click("page_3", true);
        issueNav.assertNumberOfIssues(21, 30, 30);
        assertRowSelected(1, "HSP-10");
        // 3. Next page link.
        client.click("page_1", true);
        client.click("css=.icon-next", true);
        issueNav.assertNumberOfIssues(11, 20, 30);
        assertRowSelected(1, "HSP-20");
    }

    public void testSortingResultsRemembersSelectedIssue() throws Exception
    {
        issueNav.runJql("key < HSP-9");

        // Wait for the page make sure all the JS is run before we head to another page
        waitFor(2000);

        // Set HSP-8 as the last viewed issue session-wide.
        getNavigator().gotoIssue("HSP-8");
        returnToSearch();
        assertEquals("HSP-8", issueNav.results().selectedIssueKey());

        // Select HSP-6 in the navigator.
        issueNav.results().down().down();
        waitFor(SET_SELECTED_ISSUE_AJAX_DELAY);
        assertEquals("HSP-6", issueNav.results().selectedIssueKey());

        // New sort means the first one should be selected
        client.click("jquery=#issuetable th:nth(1)", true);
        assertEquals("HSP-1", issueNav.results().selectedIssueKey());
    }

    public void testNavigatorKeyboardNavigation() throws Exception
    {
        // First row selected on very first search.
        getNavigator().findAllIssues();
        issueNav.assertNumberOfIssues(1, 10, 30);

        // Test j, k, and enter.
        assertRowSelected(1, "HSP-30");
        issueNav.results().down().down().down().down().up();
        assertRowSelected(4, "HSP-27");
        viewSelectedIssue();
        assertViewingIssue(client, "HSP-27");

        // Test that visting an unselected issue sets that as selected.
        getNavigator().gotoIssue("HSP-25");
        assertViewingIssue(client, "HSP-25");
        returnToSearch();
        assertRowSelected(6, "HSP-25");
        viewSelectedIssue();
        assertViewingIssue(client, "HSP-25");

        // Test paging through the results in view issue page.
        navigateToNextIssue();
        navigateToPreviousIssue();
        navigateToPreviousIssue();
        returnToSearch();
        assertRowSelected(5, "HSP-26");

        // New Search so first row should be selected
        issueNav.runJql("");
        issueNav.assertNumberOfIssues(1, 10, 30);
        assertRowSelected(1, "HSP-30");

        // Deleting the selected issue should select the next issue.
        issueNav.results().down().down().down().down();
        assertRowSelected(5, "HSP-26");
        viewSelectedIssue();
        client.click("opsbar-operations_more");
        assertThat.elementPresentByTimeout("delete-issue", TIMEOUT);
        client.click("delete-issue");
        assertThat.elementPresentByTimeout("delete-issue-submit", TIMEOUT);
        client.click("delete-issue-submit", true);
        assertRowSelected(5, "HSP-25");
    }

    private void assertRowSelected(int row, String issueKey)
    {
        assertTrue("Expected row (" + row + ":" + issueKey + ") was not selected.", issueNav.results().isIssueSelectedAt(row));
        assertTrue("Expected row (" + row + ":" + issueKey + ") was not selected.", issueNav.results().isIssueSelectedWith(issueKey));
    }

    private void assertViewingIssue(SeleniumClient cl, final String issueKey)
    {
        assertEquals(issueKey, cl.getText("jquery=#key-val").trim());
    }

    private void returnToSearch()
    {
        client.click("return-to-search", true);
    }

    private void navigateToPreviousIssue()
    {
        client.click("previous-issue", true);
    }

    private void navigateToNextIssue()
    {
        client.click("next-issue", true);
    }

    private void viewSelectedIssue()
    {
        issueNav.results().selectedIssue().view();
    }
}
