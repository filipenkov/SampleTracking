package com.atlassian.jira.event;

import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * Author: jdoklovic
 */
public class DraftWorkflowCreatedEvent extends AbstractWorkflowEvent {

    public DraftWorkflowCreatedEvent(JiraWorkflow workflow) {
        super(workflow);
    }
}
