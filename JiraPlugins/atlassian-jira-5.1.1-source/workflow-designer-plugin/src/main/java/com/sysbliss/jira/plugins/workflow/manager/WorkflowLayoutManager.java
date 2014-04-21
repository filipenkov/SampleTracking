package com.sysbliss.jira.plugins.workflow.manager;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;

public interface WorkflowLayoutManager
{
    void saveActiveLayout(String workflowName, JWDLayout layout) throws Exception;

    void removeActiveLayout(String workflowName);

    JWDLayout loadSavedActiveLayout(String workflowName) throws Exception;

    JWDLayout getLayoutForWorkflow(JiraWorkflow jiraWorkflow) throws Exception;

    JWDLayout calculateLayout(JiraWorkflow jiraWorkflow, JWDLayout jwdLayout);

    void saveDraftLayout(String parentWorkflowName, JWDLayout layout) throws Exception;

    void removeDraftLayout(String parentWorkflowName);

    JWDLayout loadSavedDraftLayout(String parentWorkflowName) throws Exception;

    JWDLayout copyLayoutForDraftWorkflow(String parentWorkflowName) throws Exception;

    void copyActiveLayout(String originalWorkflowName, String newWorkflowName) throws Exception;

    void publishDraftLayout(String parentWorkflowName) throws Exception;
}
