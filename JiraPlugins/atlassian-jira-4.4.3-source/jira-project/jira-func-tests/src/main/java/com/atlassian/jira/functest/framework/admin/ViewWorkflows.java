package com.atlassian.jira.functest.framework.admin;

/**
 * Represents operations on the 'Workflows' page in administration.
 *
 * @since v4.3
 */
public interface ViewWorkflows
{
    /**
     * Go to 'Workflows' page.
     *
     * @return this workflows instance
     */
    ViewWorkflows goTo();

    /**
     * Add new workflow with given <tt>name</tt> and <tt>description</tt>.
     *
     * @param name name of the new workflow
     * @param description description of the new workflow
     * @return this workflows instance
     */
    ViewWorkflows addWorkflow(String name, String description);

    /**
     * Cope workflow with given <tt>nameToCopy</tt> as a new workflow named <tt>newWorkflowName</tt>.
     *
     * @param nameToCopy name of the workflow to copy (must exist)
     * @param newWorkflowName name of the new workflow
     * @return this workflows instance
     */
    ViewWorkflows copyWorkflow(String nameToCopy, String newWorkflowName);

    /**
     * Go to 'Workflow steps' page for given workflow
     *
     * @param workflowName name of the workflow
     * @return workflow steps
     */
    WorkflowSteps workflowSteps(String workflowName);

    /**
     * Launch the Workflow Designer for the given workflow
     *
     * @param workflowName name of the workflow
     * @return
     */
    ViewWorkflows launchDesigner(String workflowName);
}
