package com.atlassian.jira.event;

import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * Author: jdoklovic
 */
public class DraftWorkflowPublishedEvent extends AbstractWorkflowEvent {

    public DraftWorkflowPublishedEvent(JiraWorkflow workflow) {
        super(workflow);
    }
}
