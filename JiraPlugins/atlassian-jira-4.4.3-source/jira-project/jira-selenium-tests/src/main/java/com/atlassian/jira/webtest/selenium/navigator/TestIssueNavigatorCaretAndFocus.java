package com.atlassian.jira.webtest.selenium.navigator;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.framework.pages.IssueNavigator;
import junit.framework.Test;


/**
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
public class TestIssueNavigatorCaretAndFocus extends JiraSeleniumTest
{
    private IssueNavigator issueNav;

    private static final long TIMEOUT = 5000;
    private static final int SET_SELECTED_ISSUE_AJAX_DELAY = 1500;

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestIssueNavigatorCaretAndFocus.xml");
        issueNav = new IssueNavigator(context());

        getNavigator().gotoFindIssues();

        if (client.getText("switchnavtype").equals("advanced"))
        {
            client.click("switchnavtype", true);
        }
    }

    public static Test suite()
    {
        return suiteFor(TestIssueNavigatorCaretAndFocus.class);
    }

    public void testSubmittingJqlAlwaysFocusesJqlAndRestartsPager()
    {
        assertTrue("Switching to advanced mode should focus JQL textarea", issueNav.isJqlFocusedOnPageLoad());
        issueNav.submitJql();
        issueNav.assertNumberOfIssues(1, 10, 30);
        assertTrue("After submitting JQL form, JQL textarea should have focus.", issueNav.isJqlFocusedOnPageLoad());
        issueNav.results().assertIssueSelectedAt(1);
        issueNav.submitJql();
        issueNav.assertNumberOfIssues(1, 10, 30);
        assertTrue("After submitting JQL form, JQL textarea should have focus.", issueNav.isJqlFocusedOnPageLoad());
        issueNav.results().assertIssueSelectedAt(1);

        issueNav.toNextPage();
        issueNav.assertNumberOfIssues(11, 20, 30);
        assertFalse(issueNav.isJqlFocusedOnPageLoad());
        issueNav.results().assertIssueSelectedAt(1);

        issueNav.submitJql();
        issueNav.assertNumberOfIssues(1, 10, 30);
        assertTrue("After submitting JQL form, JQL textarea should have focus.", issueNav.isJqlFocusedOnPageLoad());
        issueNav.results().assertIssueSelectedAt(1);
    }

    public void testSortColumnByClickingHeader()
    {
        issueNav.submitJql();
        issueNav.assertNumberOfIssues(1, 10, 30);
        assertTrue("After submitting JQL form, JQL textarea should have focus.", issueNav.isJqlFocusedOnPageLoad());
        issueNav.results().assertIssueSelectedAt(1);

        issueNav.sortByColumn(4);
        assertTrue("Changing a sort modifies JQL, so it should be focused.", issueNav.isJqlFocusedOnPageLoad());
        assertTrue("Changing a sort modifies JQL, so pager should be reset.", issueNav.results().isIssueSelectedAt(1));

        issueNav.toNextPage();
        issueNav.assertNumberOfIssues(11, 20, 30);
        issueNav.results().down().down();
        waitForSelectedIssueAjax();

        causePageReloadThatReusesDirtyUrl();
        issueNav.assertNumberOfIssues(11, 20, 30);
        assertFalse("URL indicates a sort request, but we haven't actually done it.", issueNav.isJqlFocusedOnPageLoad());
        issueNav.results().assertIssueSelectedAt(3);

        issueNav.sortByColumn(5);
        assertTrue("Changing a sort modifies JQL, so it should be focused.", issueNav.isJqlFocusedOnPageLoad());
        assertTrue("Changing a sort modifies JQL, so pager should be reset.", issueNav.results().isIssueSelectedAt(1));
    }

    public void testClickingEditFocusesJql()
    {
        issueNav.submitJql();
        issueNav.assertNumberOfIssues(1, 10, 30);
        assertTrue("After submitting JQL form, JQL textarea should have focus.", issueNav.isJqlFocusedOnPageLoad());
        assertTrue("Submitting a query always resets the pager", issueNav.results().isIssueSelectedAt(1));

        issueNav.toNextPage();
        issueNav.assertNumberOfIssues(11, 20, 30);
        assertFalse(issueNav.isJqlFocusedOnPageLoad());
        assertTrue("Submitting a query always resets the pager", issueNav.results().isIssueSelectedAt(1));

        issueNav.results().down().down();
        waitForSelectedIssueAjax();
        issueNav.viewSummaryTab();
        issueNav.assertNumberOfIssues(11, 20, 30);
        issueNav.results().assertIssueSelectedAt(3);
        issueNav.viewEditTab();
        issueNav.assertNumberOfIssues(11, 20, 30);
        assertTrue("Clicking the 'Edit' tab focuses the JQL textarea.", issueNav.isJqlFocusedOnPageLoad());
        assertTrue("Clicking the 'Edit' tab doesn't restart the pager.", issueNav.results().isIssueSelectedAt(3));

        causePageReloadThatReusesDirtyUrl();
        issueNav.assertNumberOfIssues(11, 20, 30);
        assertFalse("URL indicates the 'Edit' tab, but we didn't actually do it, so no focus for the JQL textarea.", issueNav.isJqlFocusedOnPageLoad());
        assertTrue("URL indicates the 'Edit' tab, but we didn't actually do it; pager shouldn't be restarted, anyway.", issueNav.results().isIssueSelectedAt(3));
    }

    public void testCaretPositionWhenLoadingSavedFilters()
    {
        issueNav.submitJql();
        issueNav.assertNumberOfIssues(1, 10, 30);
        issueNav.results().assertIssueSelectedAt(1);

        issueNav.results().down().down();
        waitForSelectedIssueAjax();
        assertRowSelected(3, "HSP-28");

        // Filter that loads all issues (same as the current query).
        getNavigator().gotoManageFilters();
        client.click("filterlink_10000", true);
        issueNav.assertNumberOfIssues(1, 10, 30);
        // We've loaded a new filter
        assertRowSelected(1, "HSP-30");
        // HSP-28 is in the results, but not selected since we've loaded a saved filter.
        assertThat.elementPresent("issuerow10027");

        issueNav.results().down().down();
        waitForSelectedIssueAjax();
        assertRowSelected(3, "HSP-28");

        causePageReloadThatReusesDirtyUrl();
        // URL still indicates loading a saved filter, but we haven't actually done that.
        assertRowSelected(3, "HSP-28");

        getNavigator().gotoManageFilters();
        client.click("filterlink_10000", true);
        // We've loaded the same filter, so it shouldn't restart the pager or clear the selected issue.
        assertRowSelected(3, "HSP-28");

        // Now load a different saved filter (with HSP-28 in the first page of results).
        getNavigator().gotoManageFilters();
        client.click("filterlink_10010", true);
        issueNav.assertNumberOfIssues(1, 10, 20);
        assertRowSelected(1, "HSP-30");
        assertTrue(
                "HSP-28 is in the results, but not selected since we've loaded a different saved filter.",
                client.isElementPresent("issuerow10027")
        );
    }

    // This mostly tests positioning the caret where we haven't gone into the view issue page.
    public void testCaretPositionAfterDialogs()
    {
        getNavigator().findAllIssues();
        issueNav.assertNumberOfIssues(1, 10, 30);
        assertRowSelected(1, "HSP-30");
        deleteSelectedIssueViaDialog();
        issueNav.assertNumberOfIssues(1, 10, 29);
        assertRowSelected(1, "HSP-29");

        issueNav.results().down().down();
        waitFor(SET_SELECTED_ISSUE_AJAX_DELAY);
        assertRowSelected(3, "HSP-27");
        deleteSelectedIssueViaDialog();
        issueNav.assertNumberOfIssues(1, 10, 28);
        assertRowSelected(3, "HSP-26");

        // Delete the last issue on this page...
        issueNav.results().down().down().down().down().down().down().down();
        waitFor(SET_SELECTED_ISSUE_AJAX_DELAY);
        assertRowSelected(10, "HSP-19");
        deleteSelectedIssueViaDialog();
        issueNav.assertNumberOfIssues(1, 10, 27);
        // ...this is an edge case currently not handled: we don't know the next issue when on the last row of the page.
        assertRowSelected(1, "HSP-29");
    }

    private void deleteSelectedIssueViaDialog()
    {
        long issueId = issueNav.results().selectedIssueId();
        client.click("jquery=#actions_" + issueId);
        assertThat.elementPresentByTimeout("jquery=#actions_" + issueId + "_drop .issueaction-delete-issue", TIMEOUT);
        client.click("jquery=#actions_" + issueId + "_drop .issueaction-delete-issue");
        assertThat.elementPresentByTimeout("jquery=#delete-issue-submit", TIMEOUT);
        client.click("jquery=#delete-issue-submit", true);
    }

    private void causePageReloadThatReusesDirtyUrl()
    {
        client.refresh();
        client.waitForPageToLoad();
    }

    private void waitForSelectedIssueAjax()
    {
        waitFor(SET_SELECTED_ISSUE_AJAX_DELAY);
    }

    private void assertRowSelected(int row, String issueKey)
    {
        assertTrue("Expected row (" + row + ":" + issueKey + ") was not selected.", issueNav.results().isIssueSelectedAt(row));
        assertTrue("Expected row (" + row + ":" + issueKey + ") was not selected.", issueNav.results().isIssueSelectedWith(issueKey));
    }
}
