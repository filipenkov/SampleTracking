package com.atlassian.jira.webtest.framework.page;

import com.atlassian.jira.webtest.framework.model.IssueData;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;
import com.atlassian.jira.webtest.framework.page.issue.ViewIssue;
import com.atlassian.jira.webtest.framework.page.issuenavigator.IssueNavigator;

/**
 * Access point to the default global pages in the test framework.
 *
 * @since v4.3
 */
public interface GlobalPages
{
    /**
     * Go to and return issue navigator instance.
     *
     * @return issue navigator page instance
     */
    IssueNavigator goToIssueNavigator();

    /**
     * Issue navigator instance.
     *
     * @return issue navigator page instance
     */
    IssueNavigator issueNavigator();

    /**
     * Go to view issue page for given issue data. View issue is not really a global page, but may be easily open
     * via direct URL given an issue key.
     *
     * @param issueData issue data
     * @return ViewIssue page instance for given data
     */
    ViewIssue goToViewIssueFor(IssueData issueData);

    /**
     * Go to and return the Administration page.
     *
     * @return administration page
     */
    AdministrationPage goToAdministration();

    /**
     * The Administration page.
     *
     * @return administration page
     */
    AdministrationPage administration();

    /**
     * Go to and return the Dashboard page object.
     *
     * @return dashboard
     */
    Dashboard goToDashboard();

    /**
     * The Dashboard page object.
     *
     * @return dashboard
     */
    Dashboard dashboard();
}


