package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.pageobjects.project.summary.workflows.WorkflowPanel;
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

    private static String contextPath;

    @Before
    public void setUp()
    {
        contextPath = jira.getProductInstance().getContextPath();
    }

    @Test
    public void testProjectWithDefaultWorkflowScheme()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ProjectSummaryPageTab summaryPage = navigateToSummaryPageFor(PROJECT_DEFAULT_SCHEME);
        final WorkflowPanel panel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> actualWorkflows = panel.getWorkflows();

        assertEquals(1, actualWorkflows.size());
        assertWorkflow("jira", true, true, actualWorkflows.get(0));

        assertEquals("Default Workflow Scheme", panel.getSchemeName());
        assertEquals(getWorkflowSchemeUrl(PROJECT_DEFAULT_SCHEME), panel.getSchemeLinkUrl());
    }

    @Test
    public void testProjectWithXSS()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ProjectSummaryPageTab summaryPage = navigateToSummaryPageFor(PROJECT_XSS_SCHEME);
        final WorkflowPanel panel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> actualWorkflows = panel.getWorkflows();

        assertEquals(3, actualWorkflows.size());
        assertWorkflow("jira", true, true, actualWorkflows.get(0));
        assertWorkflow("'><script>altert('hello')</script>", true, false, actualWorkflows.get(1));
        assertWorkflow("abc", true, false, actualWorkflows.get(2));

        assertEquals("<strong>XSS Scheme</strong>", panel.getSchemeName());
        assertEquals(getWorkflowSchemeUrl(PROJECT_XSS_SCHEME), panel.getSchemeLinkUrl());
    }

    @Test
    public void testProjectWithCustomScheme()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ProjectSummaryPageTab summaryPage = navigateToSummaryPageFor(PROJECT_CUSTOM_SCHEME);
        final WorkflowPanel panel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> actualWorkflows = panel.getWorkflows();

        assertEquals(3, actualWorkflows.size());
        assertWorkflow("def", true, true, actualWorkflows.get(0));
        assertWorkflow("abc", true, false, actualWorkflows.get(1));
        assertWorkflow("jira", true, false, actualWorkflows.get(2));

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
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final ProjectSummaryPageTab summaryPage = navigateToSummaryPageFor(PROJECT_EDGE_CASE_SCHEME);
        final WorkflowPanel panel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> actualWorkflows = panel.getWorkflows();

        assertEquals(3, actualWorkflows.size());
        assertWorkflow("'><script>altert('hello')</script>", true, false, actualWorkflows.get(0));
        assertWorkflow("<b>Name</b>", true, false, actualWorkflows.get(1));
        assertWorkflow("abc", true, false, actualWorkflows.get(2));

        assertEquals("RemovedIssueType", panel.getSchemeName());
        assertEquals(getWorkflowSchemeUrl(PROJECT_EDGE_CASE_SCHEME), panel.getSchemeLinkUrl());
    }

    @Test
    public void testProjectWithDefaultWorkflowSchemeProjectAdmin()
    {
        jira.gotoLoginPage().login("fred", "fred", DashboardPage.class);

        final ProjectSummaryPageTab summaryPage = navigateToSummaryPageFor(PROJECT_DEFAULT_SCHEME);
        final WorkflowPanel panel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> actualWorkflows = panel.getWorkflows();

        assertEquals(1, actualWorkflows.size());
        assertWorkflow("jira", false, true, actualWorkflows.get(0));

        assertEquals("Default Workflow Scheme", panel.getSchemeName());
        assertEquals(getWorkflowSchemeUrl(PROJECT_DEFAULT_SCHEME), panel.getSchemeLinkUrl());
    }

    @Test
    public void testProjectWithXSSProjectAdmin()
    {
        jira.gotoLoginPage().login("fred", "fred", DashboardPage.class);

        final ProjectSummaryPageTab summaryPage = navigateToSummaryPageFor(PROJECT_XSS_SCHEME);
        final WorkflowPanel panel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> actualWorkflows = panel.getWorkflows();

        assertEquals(3, actualWorkflows.size());
        assertWorkflow("jira", false, true, actualWorkflows.get(0));
        assertWorkflow("'><script>altert('hello')</script>", false, false, actualWorkflows.get(1));
        assertWorkflow("abc", false, false, actualWorkflows.get(2));

        assertEquals("<strong>XSS Scheme</strong>", panel.getSchemeName());
        assertEquals(getWorkflowSchemeUrl(PROJECT_XSS_SCHEME), panel.getSchemeLinkUrl());
    }

    @Test
    public void testProjectWithCustomSchemeProjectAdmin()
    {
        jira.gotoLoginPage().login("fred", "fred", DashboardPage.class);

        final ProjectSummaryPageTab summaryPage = navigateToSummaryPageFor(PROJECT_CUSTOM_SCHEME);
        final WorkflowPanel panel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> actualWorkflows = panel.getWorkflows();

        assertEquals(3, actualWorkflows.size());
        assertWorkflow("def", false, true, actualWorkflows.get(0));
        assertWorkflow("abc", false, false, actualWorkflows.get(1));
        assertWorkflow("jira", false, false, actualWorkflows.get(2));

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
        jira.gotoLoginPage().login("fred", "fred", DashboardPage.class);

        final ProjectSummaryPageTab summaryPage = navigateToSummaryPageFor(PROJECT_EDGE_CASE_SCHEME);
        final WorkflowPanel panel = summaryPage.openPanel(WorkflowPanel.class);
        final List<WorkflowPanel.Workflow> actualWorkflows = panel.getWorkflows();

        assertEquals(3, actualWorkflows.size());
        assertWorkflow("'><script>altert('hello')</script>", false, false, actualWorkflows.get(0));
        assertWorkflow("<b>Name</b>", false, false, actualWorkflows.get(1));
        assertWorkflow("abc", false, false, actualWorkflows.get(2));

        assertEquals("RemovedIssueType", panel.getSchemeName());
        assertEquals(getWorkflowSchemeUrl(PROJECT_EDGE_CASE_SCHEME), panel.getSchemeLinkUrl());
    }

    private void assertWorkflow(String name, boolean hasUrl, boolean isDefault, WorkflowPanel.Workflow workflow)
    {
        assertEquals(name, workflow.getName());
        assertEquals(isDefault, workflow.isDefault());
        if (hasUrl)
        {
            try
            {
                assertNotNull(workflow.getUrl());
                String url = jira.getProductInstance().getContextPath() +
                        format("/secure/admin/workflows/WorkflowDesigner.jspa?workflowMode=live&wfName=%s", URLEncoder.encode(name, "UTF-8"));
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
        return contextPath + "/plugins/servlet/project-config/" + projectKey + "/workflows";
    }

    private ProjectSummaryPageTab navigateToSummaryPageFor(final String projectKey)
    {
        return pageBinder.navigateToAndBind(ProjectSummaryPageTab.class, projectKey);
    }
}
