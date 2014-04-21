package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowSteps;
import com.atlassian.jira.pageobjects.pages.admin.workflow.WorkflowHeader;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.pageobjects.project.summary.workflows.WorkflowPanel;
import com.atlassian.jira.pageobjects.project.workflow.EditWorkflowDialog;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 *
 * @since v4.4
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@RestoreOnce ("xml/SummaryWorkflowPanel.xml")
public class TestSummaryWorkflowPanel extends BaseJiraWebTest
{
    private static final String PROJECT_DEFAULT_SCHEME = "HSP";
    private static final String PROJECT_XSS_SCHEME = "XSS";
    private static final String PROJECT_CUSTOM_SCHEME = "MKY";
    private static final String PROJECT_EDGE_CASE_SCHEME = "BAD";

    private String baseUrl;

    @Before
    public void setUp()
    {
        baseUrl = jira.getProductInstance().getBaseUrl();
    }

    @Test
    public void testProjectWithDefaultWorkflowScheme()
    {
        final ProjectSummaryPageTab summaryPage = jira.gotoLoginPage()
                .loginAsSysAdmin(ProjectSummaryPageTab.class, PROJECT_DEFAULT_SCHEME);
        final WorkflowPanel panel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> actualWorkflows = panel.getWorkflows();

        assertEquals(1, actualWorkflows.size());
        assertWorkflow("jira", true, true, true, actualWorkflows.get(0));

        assertEquals("Default Workflow Scheme", panel.getSchemeName());
        assertEquals(getWorkflowSchemeUrl(PROJECT_DEFAULT_SCHEME), panel.getSchemeLinkUrl());
    }

    @Test
    public void testProjectWithXSS()
    {
        final ProjectSummaryPageTab summaryPage = jira.gotoLoginPage()
                .loginAsSysAdmin(ProjectSummaryPageTab.class, PROJECT_XSS_SCHEME);

        final WorkflowPanel panel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> actualWorkflows = panel.getWorkflows();

        assertEquals(3, actualWorkflows.size());
        assertWorkflow("jira", true, true, false, actualWorkflows.get(0));
        assertWorkflow("'><script>altert('hello')</script>", true, false, false, actualWorkflows.get(1));
        assertWorkflow("abc", true, false, false, actualWorkflows.get(2));

        assertEquals("<strong>XSS Scheme</strong>", panel.getSchemeName());
        assertEquals(getWorkflowSchemeUrl(PROJECT_XSS_SCHEME), panel.getSchemeLinkUrl());
    }

    @Test
    public void testProjectWithCustomScheme()
    {
        final ProjectSummaryPageTab summaryPage = jira.gotoLoginPage()
                .loginAsSysAdmin(ProjectSummaryPageTab.class, PROJECT_CUSTOM_SCHEME);
        final WorkflowPanel panel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> actualWorkflows = panel.getWorkflows();

        assertEquals(3, actualWorkflows.size());
        assertWorkflow("def", true, true, false, actualWorkflows.get(0));
        assertWorkflow("abc", true, false, false, actualWorkflows.get(1));
        assertWorkflow("jira", true, false, false, actualWorkflows.get(2));

        assertEquals("Multiple", panel.getSchemeName());
        assertEquals(getWorkflowSchemeUrl(PROJECT_CUSTOM_SCHEME), panel.getSchemeLinkUrl());
    }

    /**
     * This is basically an edge-case test. It considers two cases:
     *
     * - Every issue type is assigned a workflow and thus the default workflow is not being used.
     * - The Wofkflow scheme maps an issue type that the project does not use.
     */
    @Test
    public void testProjectWithAllIssueTypesAssignedAndRemovedIssueType()
    {
        final ProjectSummaryPageTab summaryPage = jira.gotoLoginPage()
                .loginAsSysAdmin(ProjectSummaryPageTab.class, PROJECT_EDGE_CASE_SCHEME);
        final WorkflowPanel panel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> actualWorkflows = panel.getWorkflows();

        assertEquals(3, actualWorkflows.size());
        assertWorkflow("'><script>altert('hello')</script>", true, false, false, actualWorkflows.get(0));
        assertWorkflow("<b>Name</b>", true, false, false, actualWorkflows.get(1));
        assertWorkflow("abc", true, false, false, actualWorkflows.get(2));

        assertEquals("RemovedIssueType", panel.getSchemeName());
        assertEquals(getWorkflowSchemeUrl(PROJECT_EDGE_CASE_SCHEME), panel.getSchemeLinkUrl());
    }

    @Test
    public void testProjectWithDefaultWorkflowSchemeProjectAdmin()
    {
        final ProjectSummaryPageTab summaryPage = jira.gotoLoginPage()
                .login("fred", "fred", ProjectSummaryPageTab.class, PROJECT_DEFAULT_SCHEME);
        final WorkflowPanel panel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> actualWorkflows = panel.getWorkflows();

        assertEquals(1, actualWorkflows.size());
        assertWorkflow("jira", false, true, false, actualWorkflows.get(0));

        assertEquals("Default Workflow Scheme", panel.getSchemeName());
        assertEquals(getWorkflowSchemeUrl(PROJECT_DEFAULT_SCHEME), panel.getSchemeLinkUrl());
    }

    @Test
    public void testProjectWithXSSProjectAdmin()
    {
        final ProjectSummaryPageTab summaryPage = jira.gotoLoginPage()
                .login("fred", "fred", ProjectSummaryPageTab.class, PROJECT_XSS_SCHEME);
        final WorkflowPanel panel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> actualWorkflows = panel.getWorkflows();

        assertEquals(3, actualWorkflows.size());
        assertWorkflow("jira", false, true, false, actualWorkflows.get(0));
        assertWorkflow("'><script>altert('hello')</script>", false, false, false, actualWorkflows.get(1));
        assertWorkflow("abc", false, false, false, actualWorkflows.get(2));

        assertEquals("<strong>XSS Scheme</strong>", panel.getSchemeName());
        assertEquals(getWorkflowSchemeUrl(PROJECT_XSS_SCHEME), panel.getSchemeLinkUrl());
    }

    @Test
    public void testProjectWithCustomSchemeProjectAdmin()
    {
        final ProjectSummaryPageTab summaryPage = jira.gotoLoginPage()
                .login("fred", "fred", ProjectSummaryPageTab.class, PROJECT_CUSTOM_SCHEME);
        final WorkflowPanel panel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> actualWorkflows = panel.getWorkflows();

        assertEquals(3, actualWorkflows.size());
        assertWorkflow("def", false, true, false, actualWorkflows.get(0));
        assertWorkflow("abc", false, false, false, actualWorkflows.get(1));
        assertWorkflow("jira", false, false, false, actualWorkflows.get(2));

        assertEquals("Multiple", panel.getSchemeName());
        assertEquals(getWorkflowSchemeUrl(PROJECT_CUSTOM_SCHEME), panel.getSchemeLinkUrl());
    }

    /**
     * This is basically an edge-case test. It considers two cases:
     *
     * - Every issue type is assigned a workflow and thus the default workflow is not being used.
     * - The Wofkflow scheme maps an issue type that the project does not use.
     */
    @Test
    public void testProjectWithAllIssueTypesAssignedAndRemovedIssueTypeProjectAdmin()
    {
        final ProjectSummaryPageTab summaryPage = jira.gotoLoginPage()
                .login("fred", "fred", ProjectSummaryPageTab.class, PROJECT_EDGE_CASE_SCHEME);
        final WorkflowPanel panel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> actualWorkflows = panel.getWorkflows();

        assertEquals(3, actualWorkflows.size());
        assertWorkflow("'><script>altert('hello')</script>", false, false, false, actualWorkflows.get(0));
        assertWorkflow("<b>Name</b>", false, false, false, actualWorkflows.get(1));
        assertWorkflow("abc", false, false, false, actualWorkflows.get(2));

        assertEquals("RemovedIssueType", panel.getSchemeName());
        assertEquals(getWorkflowSchemeUrl(PROJECT_EDGE_CASE_SCHEME), panel.getSchemeLinkUrl());
    }

    @Test
    public void testEditWorkflowOnDefaultScheme()
    {
        //Make sure we are in text mode before this test runs.
        final ViewWorkflowSteps steps = jira.gotoLoginPage().loginAsSysAdmin(ViewWorkflowSteps.class, "jira", false);
        steps.setCurrentViewMode(WorkflowHeader.WorkflowMode.TEXT);

        final String newKey = "TEWODS";
        final String newProjectName = "testEditWorkflowOnDefaultScheme";
        final String newWorkflow = newProjectName + " Workflow";
        backdoor.project().addProject("testEditWorkflowOnDefaultScheme", newKey, "admin");
        backdoor.issues().createIssue(newKey, "Issue To Migrate");

        final ProjectSummaryPageTab summaryPage = jira.gotoLoginPage()
                .loginAsSysAdmin(ProjectSummaryPageTab.class, newKey);
        final WorkflowPanel workflowPanel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> workflows = workflowPanel.getWorkflows();

        assertEquals(1, workflows.size());
        final WorkflowPanel.Workflow workflow = workflows.get(0);
        assertWorkflow("jira", true, true, true, workflow);

        final EditWorkflowDialog editWorkflowDialog = workflow.gotoEditWorkflowDialog();
        final WorkflowHeader header = editWorkflowDialog.gotoEditWorkflowText(newWorkflow);
        assertTrue(header.isDraft());
        assertTrue(header.canDiscard());
        assertTrue(header.canPublish());
        assertTrue(header.canEditNameOrDescription());
    }

    private void assertWorkflow(String name, boolean hasUrl, boolean isDefault, boolean canEdit, WorkflowPanel.Workflow workflow)
    {
        assertEquals(name, workflow.getName());
        assertEquals(isDefault, workflow.isDefault());
        assertEquals(canEdit, workflow.hasEditLink());
        if (hasUrl)
        {
            try
            {
                assertNotNull(workflow.getUrl());
                String url = jira.getProductInstance().getBaseUrl() +
                        format("/secure/admin/workflows/ViewWorkflowSteps.jspa?workflowMode=live&workflowName=%s", URLEncoder.encode(name, "UTF-8"));
                assertEquals(workflow.getUrl(), url);
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            assertNull(workflow.getUrl());
        }
    }

    private String getWorkflowSchemeUrl(final String projectKey)
    {
        return baseUrl + "/plugins/servlet/project-config/" + projectKey + "/workflows";
    }
}
