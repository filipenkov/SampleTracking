package com.atlassian.jira.event;

import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * Author: jdoklovic
 */
public class WorkflowDeletedEvent extends AbstractWorkflowEvent {

    public WorkflowDeletedEvent(JiraWorkflow workflow) {
        super(workflow);
    }
}

