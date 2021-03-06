package com.atlassian.jira.webtests.ztests.plugin.reloadable;

import com.atlassian.jira.functest.framework.admin.ViewWorkflows;

import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.DEFAULT_WORKFLOW_NAME;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.TEST_WORKFLOW_NAME;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.TEST_WORKFLOW_SCHEME_DESC;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.TEST_WORKFLOW_SCHEME_NAME;

/**
 * Abstract base class for workflow component (condition, function, validator) reloadability tests.
 *
 * @since v4.4
 */
public abstract class AbstractReloadableWorkflowComponentTest extends AbstractReloadablePluginsTest
{

    protected final void setUpTestScheme()
    {
        administration.workflowSchemes().goTo().addWorkflowScheme(TEST_WORKFLOW_SCHEME_NAME, TEST_WORKFLOW_SCHEME_DESC);
        administration.workflowSchemes().goTo().assignWorkflowToAllIssueTypes(TEST_WORKFLOW_SCHEME_NAME, TEST_WORKFLOW_NAME);
    }

    protected final ViewWorkflows setUpTestWorkflow()
    {
        return administration.workflows().goTo().copyWorkflow(DEFAULT_WORKFLOW_NAME, TEST_WORKFLOW_NAME);
    }
}
