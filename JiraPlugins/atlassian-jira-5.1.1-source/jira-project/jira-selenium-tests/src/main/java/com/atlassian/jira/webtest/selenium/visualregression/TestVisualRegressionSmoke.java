package com.atlassian.jira.webtest.selenium.visualregression;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.selenium.Condition;
import com.thoughtworks.selenium.Selenium;

/**
 * UI Regression tests for the JIRA Dashboard
 *
 * @since v4.3
 */
@WebTest ( { Category.SELENIUM_TEST, Category.VISUAL_REGRESSION })
public class TestVisualRegressionSmoke extends JiraVisualRegressionTest
{
    protected String getXmlDataName()
    {
        return "TestVisualRegressionSmoke.zip";
    }

    public void testEmptyDashboard()
    {
        visualComparer.setWaitBeforeScreenshotCondition(new WaitingCondition(3000));
        getNavigator().dashboard("10030").view();
        assertUIMatches("empty-dashboard");
    }

    public void testMediumDashboard()
    {
        visualComparer.setWaitBeforeScreenshotCondition(new WaitingCondition(3000));
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
        client.mouseOut("issuetable");//Making this change, because if mouse hovers on the links, an underline appears on the screenshot which is taken, and that does not match with baseline image.
        assertUIMatches("issue-nav-simple");
    }

    public void testWorkflowDesigner()
    {
        getNavigator().gotoAdmin();
        client.click("workflows_section", true);
        client.click("edit_live_Copy of Copy of jira", true);
        //wait for 10 seconds to give the workflow designer flash a chance to load.
        visualComparer.setWaitBeforeScreenshotCondition(new WaitingCondition(10000));
        assertUIMatches("workflow-designer");
    }

    public void testAdminSummary()
    {
         client.click("admin_summary",true);



        assertUIMatches("admin-summary-page");
    }


     public void testProjectSummary()
    {
        client.click("admin-summary-recent-projects-BULK",true);

        assertUIMatches("project-summary");
    }

    static class WaitingCondition implements Condition
    {

        private long waitTimeMillis;

        WaitingCondition(long waitTimeMillis)
        {
            this.waitTimeMillis = waitTimeMillis;
        }

        @Override
        public boolean executeTest(Selenium selenium)
        {
            selenium.waitForCondition("selenium.browserbot.getCurrentWindow().jQuery.active == 0;", Long.toString(400));
            try
            {
                Thread.sleep(waitTimeMillis);
            }
            catch (InterruptedException e)
            {
                return false;
            }
            return true;
        }

        @Override
        public String errorMessage()
        {
            return "Holy Moly! An error occurred waiting for a thread to sleep!";
        }
    }
}