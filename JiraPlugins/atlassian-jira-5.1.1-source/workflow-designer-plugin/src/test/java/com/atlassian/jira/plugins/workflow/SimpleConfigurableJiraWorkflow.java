package com.atlassian.jira.plugins.workflow;

import com.atlassian.jira.workflow.AbstractJiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.loader.WorkflowDescriptor;


public class SimpleConfigurableJiraWorkflow extends AbstractJiraWorkflow {

    private String name;
    private boolean draft;
    private boolean isDefault;

    public SimpleConfigurableJiraWorkflow(String name, boolean draft, WorkflowDescriptor workflowDescriptor, WorkflowManager workflowManager, boolean isDefault) {
        super(workflowManager, workflowDescriptor);
        this.name = name;
        this.draft = draft;
        this.isDefault = isDefault;
    }

    public String getName() {
        return name;
    }

    public boolean isDraftWorkflow() {
        return draft;
    }

    public void reset() {
        //do nothing!
    }

    public boolean isDefault() {
        return isDefault;
    }


}
