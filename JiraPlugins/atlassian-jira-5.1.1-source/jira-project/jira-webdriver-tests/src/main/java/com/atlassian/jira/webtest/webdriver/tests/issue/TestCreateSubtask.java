package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.menu.IssueActions;
import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.jira.pageobjects.elements.AuiMessage;
import com.atlassian.jira.pageobjects.elements.GlobalMessage;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.navigator.IssueNavigatorResults;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.viewissue.MoreActionsMenu;
import com.atlassian.jira.pageobjects.pages.viewissue.Subtask;
import com.atlassian.jira.pageobjects.pages.viewissue.SubtaskModule;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.PageBinder;
import org.junit.Test;

import javax.inject.Inject;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v5.0
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
@Restore("xml/TestCreateSubtasks.xml")
public class TestCreateSubtask extends BaseJiraWebTest
{
    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected TraceContext traceContext;

    @Test
    public void testCreateSingleSubtaskOnViewIssue()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        final MoreActionsMenu moreActionsMenu = viewIssuePage.getMoreActionsMenu();
        moreActionsMenu.open().clickItem(IssueActions.CREATE_SUBTASK);
        CreateIssueDialog createIssueDialog = pageBinder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.SUBTASK);
        createIssueDialog.fill("summary", "test subtask creation").submit(ViewIssuePage.class, "HSP-1");

        viewIssuePage = jira.goToViewIssue("HSP-1");

        SubtaskModule subTasksModule = viewIssuePage.getSubTasksModule();
        assertEquals("test subtask creation", subTasksModule.getSubtasks().get(0).getSummary());

        createIssueDialog = subTasksModule.openCreateSubtaskDialog()
                .checkCreateMultiple()
                .fill("summary", "Two")
                .submit(CreateIssueDialog.class, CreateIssueDialog.Type.SUBTASK);

        AuiMessage auiMessage = createIssueDialog.getAuiMessage();

        assertEquals(AuiMessage.Type.SUCCESS, auiMessage.getType());
        auiMessage.dismiss();

        createIssueDialog = createIssueDialog.fill("summary", "Three")
                .submit(CreateIssueDialog.class, CreateIssueDialog.Type.SUBTASK);

        auiMessage = createIssueDialog.getAuiMessage();

        assertEquals(AuiMessage.Type.SUCCESS, auiMessage.getType());
        auiMessage.dismiss();


        Tracer tracer = traceContext.checkpoint();
        createIssueDialog.close();

        subTasksModule = viewIssuePage.waitForAjaxRefresh(tracer).getSubTasksModule();

        final List<Subtask> subtasks = subTasksModule.getSubtasks();
        assertEquals("Two", subtasks.get(1).getSummary());
        assertEquals("Three", subtasks.get(2).getSummary());
    }

    @Test
    public void testCreateSingleSubtaskOnIssueNavigator()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.visit(AdvancedSearch.class).enterQuery("").submit();
        IssueNavigatorResults issueNavigatorResults = pageBinder.bind(IssueNavigatorResults.class);
        issueNavigatorResults.getSelectedIssue().getActionsMenu().open().clickItem(IssueActions.CREATE_SUBTASK);
        CreateIssueDialog createSubtaskDialog = pageBinder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.SUBTASK);
        assertTrue("Expected Edit Issue Dialog for [MKY-1]", createSubtaskDialog.getTitle().contains("MKY-1"));
        createSubtaskDialog.close();
        issueNavigatorResults.nextIssue();
        issueNavigatorResults.getSelectedIssue().getActionsMenu().open().clickItem(IssueActions.CREATE_SUBTASK);
        createSubtaskDialog = pageBinder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.SUBTASK);
        assertTrue("Expected Edit Issue Dialog for [HSP-5]", createSubtaskDialog.getTitle().contains("HSP-5"));
        final GlobalMessage message = createSubtaskDialog.fill("summary", "My new subtask").submit(GlobalMessage.class);
        assertEquals(GlobalMessage.Type.SUCCESS, message.getType());
    }

}
