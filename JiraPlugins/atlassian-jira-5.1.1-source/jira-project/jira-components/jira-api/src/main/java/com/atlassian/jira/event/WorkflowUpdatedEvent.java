package com.atlassian.jira.event;

import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * Event indicating a workflow has been updated.
 *
 * @since v5.0
 */
public class WorkflowUpdatedEvent extends AbstractWorkflowEvent
{
    public WorkflowUpdatedEvent(JiraWorkflow workflow)
    {
        super(workflow);
    }
}
