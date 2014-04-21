package com.atlassian.jira.webtest.webdriver.tests.admin.workflow;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.menu.AdminMenu;
import com.atlassian.jira.pageobjects.pages.JiraAdminHomePage;
import com.atlassian.jira.pageobjects.pages.admin.WorkflowDesignerPage;
import com.atlassian.jira.pageobjects.pages.admin.WorkflowsPage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the breadcrumbs section of the JIRA workflow designer.
 *
 * @since v4.4
 */
@WebTest ( { Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.WORKFLOW })
@Restore ("xml/TestWorkflowDesigner.xml")
public class TestWorkflowDesigner extends BaseJiraWebTest
{
    @Test
    public void testEditWorkflowNameAndDescription()
    {
        WorkflowsPage workflows = jira.gotoLoginPage().loginAsSysAdmin(JiraAdminHomePage.class)
                .adminMenu()
                .goToAdminPage(WorkflowsPage.class);

        final WorkflowDesignerPage defaultWorkflow = workflows.openDesigner("jira");
        assertTrue(defaultWorkflow.getWorkflowHeader().contains("jira"));
        assertEquals("The default JIRA workflow.", defaultWorkflow.getWorkflowDescription());
        assertFalse(defaultWorkflow.isEditable());
        assertFalse(defaultWorkflow.isPublishable());

        final WorkflowDesignerPage workflow1 = workflows.openDesigner("Workflow1");
        assertTrue(defaultWorkflow.getWorkflowHeader().contains("Workflow1"));
        assertEquals("", workflow1.getWorkflowDescription());
        assertFalse(workflow1.isEditable());
        assertFalse(workflow1.isPublishable());

        final WorkflowDesignerPage workflowasdf = workflows.openDesigner("Workflowasdf");
        assertTrue(defaultWorkflow.getWorkflowHeader().contains("Workflowasdf"));
        assertEquals("Yeah", workflowasdf.getWorkflowDescription());
        assertTrue(workflowasdf.isEditable());
        assertFalse(workflowasdf.isPublishable());
        assertTrue(workflowasdf.isNameEditable());

        WorkflowDesignerPage.EditDialog editDialog = workflowasdf.editDialog();
        editDialog.open().edit("Workflow with a proper name", "This is a decent description").submit("Workflow with a proper name");
        WorkflowDesignerPage editedWorkflow = workflows.openDesigner("Workflow with a proper name");
        assertTrue(defaultWorkflow.getWorkflowHeader().contains("Workflow with a proper name"));
        assertEquals("This is a decent description", editedWorkflow.getWorkflowDescription());

        WorkflowDesignerPage properWorkflow = workflows.openDesigner("Workflow with a proper name");
        String editError = properWorkflow.editDialog().open().edit("Workflow1", "Some description").submitWithError();
        assertEquals("A workflow with this name already exists.", editError);


        final WorkflowDesignerPage draftWorkflow = workflows.openDesigner("Workflow1", true);
        assertTrue(defaultWorkflow.getWorkflowHeader().contains("Workflow1"));
        assertTrue(defaultWorkflow.getWorkflowHeader().contains("Draft"));
        assertEquals("test", draftWorkflow.getWorkflowDescription());
        assertTrue(draftWorkflow.isEditable());
        assertTrue(draftWorkflow.isPublishable());
        assertFalse(draftWorkflow.isNameEditable());
    }

    @Test
    public void testPublishWorkflow()
    {
        AdminMenu adminSideMenu = jira.gotoLoginPage().loginAsSysAdmin(JiraAdminHomePage.class).adminMenu();
        final WorkflowsPage workflows = adminSideMenu.goToAdminPage(WorkflowsPage.class);

        List<WorkflowsPage.Workflow> workflowList = workflows.getWorkflows();
        assertEquals(5, workflowList.size());

        assertEquals("jira", workflowList.get(0).getName());
        assertEquals("workflow 1", workflowList.get(1).getName());
        assertEquals("Workflow1", workflowList.get(2).getName());
        assertEquals("Workflow1", workflowList.get(3).getName());
        assertEquals("Workflowasdf", workflowList.get(4).getName());

        WorkflowDesignerPage draftWorkflow = workflows.openDesigner("Workflow1", true);
        draftWorkflow.publish(false);

        adminSideMenu = jira.gotoAdminHomePage().adminMenu();
        workflowList = adminSideMenu.goToAdminPage(WorkflowsPage.class).getWorkflows();
        assertEquals(4, workflowList.size());

        assertEquals("jira", workflowList.get(0).getName());
        assertEquals("workflow 1", workflowList.get(1).getName());
        assertEquals("Workflow1", workflowList.get(2).getName());
        assertEquals("Workflowasdf", workflowList.get(3).getName());
    }

    @Test
    public void testPublishWorkflowWithBackup()
    {
        AdminMenu adminSideMenu = jira.gotoLoginPage().loginAsSysAdmin(JiraAdminHomePage.class).adminMenu();
        final WorkflowsPage workflows = adminSideMenu.goToAdminPage(WorkflowsPage.class);

        List<WorkflowsPage.Workflow> workflowList = workflows.getWorkflows();
        assertEquals(5, workflowList.size());

        assertEquals("jira", workflowList.get(0).getName());
        assertEquals("workflow 1", workflowList.get(1).getName());
        assertEquals("Workflow1", workflowList.get(2).getName());
        assertEquals("Workflow1", workflowList.get(3).getName());
        assertEquals("Workflowasdf", workflowList.get(4).getName());

        WorkflowDesignerPage draftWorkflow = workflows.openDesigner("Workflow1", true);
        draftWorkflow.publish(true);

        adminSideMenu = jira.gotoAdminHomePage().adminMenu();
        workflowList = adminSideMenu.goToAdminPage(WorkflowsPage.class).getWorkflows();
        assertEquals(5, workflowList.size());

        assertEquals("jira", workflowList.get(0).getName());
        assertEquals("Copy of Workflow1", workflowList.get(1).getName());
        assertEquals("workflow 1", workflowList.get(2).getName());
        assertEquals("Workflow1", workflowList.get(3).getName());
        assertEquals("Workflowasdf", workflowList.get(4).getName());
    }


}
