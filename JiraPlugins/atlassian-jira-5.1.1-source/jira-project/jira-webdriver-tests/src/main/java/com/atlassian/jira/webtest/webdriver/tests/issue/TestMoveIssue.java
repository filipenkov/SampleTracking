package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.model.DefaultIssueActions;
import com.atlassian.jira.pageobjects.model.IssueOperation;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.viewissue.IssueMenu;
import com.atlassian.jira.pageobjects.pages.viewissue.MoveIssueConfirmation;
import com.atlassian.jira.pageobjects.pages.viewissue.MoveIssuePage;
import com.atlassian.jira.pageobjects.pages.viewissue.MoveIssueUpdateFields;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.PageBinder;
import org.junit.Test;

import javax.inject.Inject;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;


@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
@Restore ("xml/TestProjectSelectForCreate.xml")
public class TestMoveIssue extends BaseJiraWebTest
{

    private static final String HSP_1 = "HSP-1";
    @Inject
    private PageBinder binder;

    @Test
    public void testProjectSelect()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        ViewIssuePage viewIssuePage = jira.goToViewIssue(HSP_1);
        IssueMenu issueMenu = viewIssuePage.getIssueMenu();
        issueMenu.invoke(DefaultIssueActions.MOVE);
        final MoveIssuePage moveIssuePage = binder.bind(MoveIssuePage.class, HSP_1);
        assertEquals(moveIssuePage.getIssueTypes(), asList("Bug", "New Feature", "Task", "Improvement"));
        moveIssuePage.setNewProject("gorilla");
        assertEquals(moveIssuePage.getIssueTypes(), asList("Task"));
        moveIssuePage.setNewProject("monkey");
        assertEquals(moveIssuePage.getIssueTypes(), asList("Bug"));
        final ViewIssuePage issuePage = moveIssuePage.next().next().move();
        assertEquals("Bug", issuePage.getDetailsSection().getIssueType());
        assertEquals("monkey", issuePage.getProject());
    }
}

