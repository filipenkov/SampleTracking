package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.menu.IssueActions;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.navigator.SelectedIssue;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.viewissue.AssignIssueDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.pages.viewissue.people.PeopleSection;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * Tests for keyboard shortcuts for the issueaction context
 *
 * @since v5.1
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
@Restore ("xml/TestActionsAndOperations.xml")
public class TestKeyboardShortcuts extends BaseJiraWebTest
{
    @Inject
    private TraceContext traceContext;

    @Test
    public void testAssignToMeShortcut() throws Exception
    {
        ViewIssuePage viewIssuePage = jira.gotoLoginPage().loginAsSysAdmin(ViewIssuePage.class, "HSP-1");

        AssignIssueDialog assignIssueDialog = viewIssuePage.assignIssueViaKeyboardShortcut();
        assignIssueDialog.setAssignee("Unassigned");

        Tracer tracer = traceContext.checkpoint();
        assignIssueDialog.submit();
        viewIssuePage.waitForAjaxRefresh(tracer);

        PeopleSection peopleSection = viewIssuePage.getPeopleSection();

        assertEquals("Unassigned", peopleSection.getAssignee());
        tracer = traceContext.checkpoint();
        viewIssuePage.execKeyboardShortcut(IssueActions.ASSIGN_TO_ME.getShortcut());
        viewIssuePage.waitForAjaxRefresh(tracer);

        assertEquals("Administrator", peopleSection.getAssignee());
    }

    @Test
    public void testAssignToMeShortcutInNavigator() throws Exception
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        AdvancedSearch issueNavigator = jira.visit(AdvancedSearch.class).enterQuery("").submit();
        SelectedIssue selectedIssue = issueNavigator.getResults().selectIssue("10000");

        AssignIssueDialog assignIssueDialog = selectedIssue.assignViaShortcut();
        assignIssueDialog.setAssignee("Unassigned");
        assignIssueDialog.submit();

        assertEquals("Unassigned", selectedIssue.getAssignee());
        issueNavigator.execKeyboardShortcut(IssueActions.ASSIGN_TO_ME.getShortcut());
        assertEquals("Administrator", selectedIssue.getAssignee());
    }
}
