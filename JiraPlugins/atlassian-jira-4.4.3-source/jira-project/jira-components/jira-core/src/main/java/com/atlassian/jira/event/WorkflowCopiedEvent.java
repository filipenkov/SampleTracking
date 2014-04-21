package com.atlassian.jira.event;

import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * Author: jdoklovic
 */
public class WorkflowCopiedEvent {

    private final JiraWorkflow originalWorkflow;
    private final JiraWorkflow newWorkflow;

    public WorkflowCopiedEvent(JiraWorkflow originalWorkflow, JiraWorkflow newWorkflow) {
        this.originalWorkflow = originalWorkflow;
        this.newWorkflow = newWorkflow;
    }

    public JiraWorkflow getOriginalWorkflow() {
        return originalWorkflow;
    }

    public JiraWorkflow getNewWorkflow() {
        return newWorkflow;
    }
}
