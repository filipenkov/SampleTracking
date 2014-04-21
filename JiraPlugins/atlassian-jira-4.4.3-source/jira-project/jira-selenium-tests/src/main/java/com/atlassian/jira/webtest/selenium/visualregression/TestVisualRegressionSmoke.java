package com.atlassian.jira.webtest.selenium.visualregression;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * UI Regression tests for the JIRA Dashboard
 *
 * @since v4.3
 */
@WebTest( { Category.SELENIUM_TEST, Category.VISUAL_REGRESSION })
public class TestVisualRegressionSmoke extends JiraVisualRegressionTest
{
    protected String getXmlDataName()
    {
        return "TestVisualRegressionSmoke.zip";
    }

    public void testEmptyDashboard()
    {
        getNavigator().dashboard("10030").view();
        assertUIMatches("empty-dashboard");
    }

    public void testMediumDashboard()
    {
        getNavigator().dashboard("10032").view();
        assertUIMatches("medium-dashboard");
    }

    public void testViewIssue()
    {
        getNavigator().issue().viewIssue("BULK-5");
        assertUIMatches("long-issue");
    }

    public void testIssueNavAdvanced()
    {
        getNavigator().gotoFindIssuesAdvanced();
        client.type("jqltext", "cf[10010] = aaa order by issuekey desc");
        client.click("jqlrunquery");
        // TODO: Hack - we can't refresh the issue nav without getting a POST warning message from the browser
        getNavigator().dashboard("10030").view();
        getNavigator().gotoFindIssuesAdvanced();
        assertUIMatches("issue-nav-advanced");
    }

    public void testIssueNavSimple()
    {
        getNavigator().gotoFindIssuesAdvanced();
        client.type("jqltext", "cf[10010] = aaa order by issuekey desc");
        client.click("jqlrunquery");
        // This is a dodgy workaround.
        getNavigator().dashboard("10030").view();
        getNavigator().gotoFindIssuesSimple();
        assertUIMatches("issue-nav-simple");
    }
}