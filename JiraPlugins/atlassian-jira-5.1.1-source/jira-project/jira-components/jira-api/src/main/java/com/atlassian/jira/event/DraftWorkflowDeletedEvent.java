package com.atlassian.jira.event;

import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * Author: jdoklovic
 */
public class DraftWorkflowDeletedEvent extends AbstractWorkflowEvent{

    public DraftWorkflowDeletedEvent(JiraWorkflow workflow) {
        super(workflow);
    }
}
