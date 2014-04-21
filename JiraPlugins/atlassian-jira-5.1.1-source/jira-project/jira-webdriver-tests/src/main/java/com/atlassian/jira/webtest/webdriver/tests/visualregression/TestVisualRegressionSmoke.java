package com.atlassian.jira.webtest.webdriver.tests.visualregression;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.admin.AdminSummaryPage;
import com.atlassian.jira.pageobjects.pages.admin.workflow.WorkflowsPage;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Webdriver test for visual regression.
 *
 * @since v5.0
 */
@WebTest ( { Category.WEBDRIVER_TEST, Category.VISUAL_REGRESSION })
@Restore("xml/TestVisualRegressionSmoke.zip")
@Ignore("(1) The effort to re-write from Selenium1 to WebDriver is not completed (see individual @Ignores below) and\n"
        + "(2) the baseline screenshots must be moved out of jira-selenium tests for this to work.\n"
        + "Currently there is no urgent need to complete those tasks -- dkordonski,pwyatt")
public class TestVisualRegressionSmoke extends JiraVisualRegressionTest
{

    @Before
    public void logIn()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
    }

    @Test
    public void testEmptyDashboard()
    {
        visualComparer.setWaitforJQueryTimeout(0);
        jira.gotoHomePage().gadgets().switchDashboard("Empty");
        assertUIMatches("empty-dashboard");
    }

    @Test
    public void testMediumDashboard()
    {
        visualComparer.setWaitforJQueryTimeout(0);
        jira.gotoHomePage().gadgets().switchDashboard("Medium");
        assertUIMatches("medium-dashboard");
    }

    @Test
    public void testViewIssue()
    {
        jira.goToViewIssue("BULK-5");
        assertUIMatches("long-issue");
    }

    @Test
    @Ignore("No Page Objects yet - JPO-6")
    public void testIssueNavAdvanced()
    {
        //TODO: PageObjects are currently unable to access specific views of the Issues Page. (JPO-6)
//        getNavigator().gotoFindIssuesAdvanced();
        //As the screen is not reachable, this text field is inaccessible.
//        client.type("jqltext", "cf[10010] = aaa order by issuekey desc");
//        client.click("jqlrunquery");
        // TODO: Hack - we can't refresh the issue nav without getting a POST warning message from the browser
        jira.gotoHomePage().gadgets().switchDashboard("Empty");
        //TODO: PageObjects are currently unable to access specific views of the Issues Page. (JPO-6)
//        getNavigator().gotoFindIssuesAdvanced();
        assertUIMatches("issue-nav-advanced");
    }

    @Test
    @Ignore("No Page Objects yet - JPO-6")
    public void testIssueNavSimple()
    {
        //TODO: PageObjects are currently unable to access specific views of the Issues Page. (JPO-6)
//        getNavigator().gotoFindIssuesAdvanced();
        //As the screen is not reachable, this text field is inaccessible.
        //client.type("jqltext", "cf[10010] = aaa order by issuekey desc");
        //client.click("jqlrunquery");
        // This is a dodgy workaround.
        jira.gotoHomePage().gadgets().switchDashboard("Empty");
        //TODO: PageObjects are currently unable to access specific views of the Issues Page. (JPO-6)
//        getNavigator().gotoFindIssuesSimple();
//        client.mouseOut("issuetable");//Making this change, because if mouse hovers on the links, an underline appears on the screenshot which is taken, and that does not match with baseline image.
        assertUIMatches("issue-nav-simple");
    }

    @Test
    public void testWorkflowDesigner()
    {
        jira.goTo(WorkflowsPage.class).openDesigner("Copy of Copy of jira");
        visualComparer.setWaitforJQueryTimeout(0);
        assertUIMatches("workflow-designer");
    }

    @Test
    public void testAdminSummary()
    {
        jira.goTo(AdminSummaryPage.class);
        assertUIMatches("admin-summary-page");
    }

    @Test
    public void testProjectSummary()
    {
        // TODO Currently a workaround, okay solution for this case (JPO-8)
        jira.gotoLoginPage().loginAsSysAdmin(ProjectSummaryPageTab.class, "BULK");
        assertUIMatches("project-summary");
    }


    protected void assertUIMatches(String id)
    {
        // TODO: Don't hard-code this
        // TODO: Move into jira-webdriver when all tests are ported
        visualComparer.assertUIMatches(id, "jira-selenium-tests/src/main/xml/baseline-screenshots");
    }

}
