package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.backdoor.WorkflowSchemesControl;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowSteps;
import com.atlassian.jira.pageobjects.pages.admin.workflow.WorkflowHeader;
import com.atlassian.jira.pageobjects.project.workflow.EditWorkflowDialog;
import com.atlassian.jira.pageobjects.project.workflow.WorkflowsPageTab;
import com.atlassian.jira.pageobjects.websudo.JiraSudoFormDialog;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.ChangeLog;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v5.1
 */

@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@RestoreOnce ("xml/TestWorkflowTab.xml")
public class TestTabWorkflowEdit extends BaseJiraWebTest
{
    private static final String PROJECT_DEFAULT_KEY = "HSP";
    private static final String PROJECT_DEFAULT_NAME = "homosapien";

    private static final String PROJECT_PRIVATE_KEY = "MKY";
    private static final String PROJECT_PRIVATE_NAME = "monkey";

    private static final String PROJECT_XSS_NAME = "<strong>Cross Site Scripting</strong>";
    private static final String PROJECT_XSS_KEY = "XSS";

    @Before
    public void before()
    {
        //Make sure we are in text mode before this test runs.
        final ViewWorkflowSteps steps = jira.gotoLoginPage().loginAsSysAdmin(ViewWorkflowSteps.class, "jira", false);
        steps.setCurrentViewMode(WorkflowHeader.WorkflowMode.TEXT);
    }

    @Test
    public void testEditWorkflowCopiesWorkflowAndSchemeOnEditWhenUsingDefaultSchemeNoIssues()
    {
        checkWorkflowAndSchemeCopied(PROJECT_DEFAULT_NAME, PROJECT_DEFAULT_KEY, false);
    }

    @Test
    public void testEditWorkflowCopiesWorkflowAndSchemeOnEditWhenUsingDefaultSchemeWithIssueToMigrate()
    {
        String name = "Project With Issues";
        String key = "PWIADWFS";

        checkWorkflowChangedAndIssuesMigrated(name, key, false);
    }

    @Test
    public void testWebsudoOnTheMigrationDialog()
    {
        String name = "WebSudo Project";
        String key = "WEBSUDO";

        backdoor.websudo().enable();
        try
        {
            checkWorkflowChangedAndIssuesMigrated(name, key, true);
        }
        finally
        {
            backdoor.websudo().disable();
        }
    }

    private void checkWorkflowChangedAndIssuesMigrated(String name, String key, boolean webSudo)
    {
        backdoor.project().addProject(name, key, "admin");
        final IssueCreateResponse createResponse = backdoor.issues().createIssue(key, "This is a simple issue for migration");
        checkWorkflowAndSchemeCopied(name, key, webSudo);

        //Check that the issue has been migrated correctly.
        final Issue issue = backdoor.issues().getIssue(createResponse.key, Issue.Expand.changelog);
        assertChangeLogContainsChange("Workflow", "jira", format("%s Workflow", name), issue.changelog);
    }

    @Test
    public void testEditWorkflowCopiesWorkflowAndSchemeOnEditWhenUsingDefaultSchemeXss()
    {
        String name = "\"'<strong>XSS</strong>\"'";
        String key = "SXSS";

        backdoor.project().addProject(name, key, "admin");
        checkWorkflowAndSchemeCopied(name, key, false);
    }

    @Test
    public void testEditWorkflowDoesNotCopyWorkflowWhenUsingNonDefaultWorkflowScheme()
    {
        final ViewWorkflowSteps designerPage
                = checkWorkflowSchemeNotCopied(PROJECT_PRIVATE_NAME, PROJECT_PRIVATE_KEY);

        //We should have copied the workflow as a draft.
        assertTrue(designerPage.canPublish());
    }

    @Test
    public void testEditWorkflowDoesNotCopyWorkflowWhenUsingNonDefaultWorkflowSchemeXss()
    {
        final ViewWorkflowSteps designerPage
                = checkWorkflowSchemeNotCopied(PROJECT_XSS_NAME, PROJECT_XSS_KEY);

        //The workflow is a system workflow and as such it should be publishable.
        assertFalse(designerPage.canPublish());
    }

    private void checkWorkflowAndSchemeCopied(String projectName, String key, boolean webSudo)
    {
        String newWorkflowName = format("%s Workflow", projectName);
        String newWorkflowSchemeName = format("%s Workflow Scheme", projectName);

        WorkflowsPageTab workflows = jira.goTo(WorkflowsPageTab.class, key);
        List<WorkflowsPageTab.WorkflowPanel> panels = workflows.getWorkflowPanels();
        WorkflowsPageTab.WorkflowPanel workflowPanel = panels.get(0);

        final EditWorkflowDialog dialog = workflowPanel.gotoEditWorkflowDialog();
        assertFalse(dialog.isProgressBarPresent());
        assertTrue(dialog.isButtonsVisible());

        ViewWorkflowSteps workflowDesignerPage;
        if (webSudo)
        {
            final JiraSudoFormDialog sudoFormDialog = dialog.clickContinueAndBind(JiraSudoFormDialog.class, JiraSudoFormDialog.ID_SMART_WEBSUDO);
            sudoFormDialog.authenticateFail("fail");
            workflowDesignerPage = sudoFormDialog.authenticate("admin", ViewWorkflowSteps.class, newWorkflowName, true);
        }
        else
        {
            workflowDesignerPage = dialog.gotoEditWorkflowText(newWorkflowName);
        }
        assertTrue(workflowDesignerPage.canPublish());

        assertTrue(format("Unable to find workflow '%s'.", newWorkflowName), backdoor.workflow().getWorkflows().contains(newWorkflowName));
        boolean found = false;
        for (WorkflowSchemesControl.WorkflowScheme scheme : backdoor.workflowSchemes().getWorkflowSchemes())
        {
            if (newWorkflowSchemeName.equals(scheme.getName()))
            {
                found = true;
                assertEquals(newWorkflowName, scheme.getDefaultWorkflow());
                assertTrue(scheme.getMapping().isEmpty());
            }
        }

        if (!found)
        {
            fail(format("Workflow Scheme '%s' not found.", newWorkflowSchemeName));
        }
    }

    private ViewWorkflowSteps checkWorkflowSchemeNotCopied(String projectName, String projectKey)
    {
        String newWorkflowName = format("%s Workflow", projectName);

        WorkflowsPageTab workflows = jira.goTo(WorkflowsPageTab.class, projectKey);
        List<WorkflowsPageTab.WorkflowPanel> panels = workflows.getWorkflowPanels();
        WorkflowsPageTab.WorkflowPanel workflowPanel = panels.get(0);
        ViewWorkflowSteps viewWorkflowSteps = workflowPanel.clickEditWorkflowAndBind(ViewWorkflowSteps.class, workflowPanel.getWorkflowName());
        assertFalse(backdoor.workflow().getWorkflows().contains(newWorkflowName));

        return viewWorkflowSteps;
    }

    private void assertChangeLogContainsChange(String field, String from, String to, ChangeLog changeLog)
    {
        final Collection<? extends ChangeLog.HistoryItem> historyItems = changeLog.mergeHistoryItems();
        for (ChangeLog.HistoryItem historyItem : historyItems)
        {
            if (StringUtils.equals(historyItem.field, field)
                && StringUtils.equals(historyItem.fromString, from)
                && StringUtils.equals(historyItem.toString, to))
            {
                return;
            }
        }
        fail(format("Unable to find item with {field: %s, fromString: %s, toString: %s} in [%s]",
                field, from, to, historyItems));
    }
}
