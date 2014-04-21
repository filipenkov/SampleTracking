package com.sysbliss.jira.plugins.workflow.jgraph;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.mxgraph.view.mxGraph;
import com.sysbliss.jira.plugins.workflow.manager.WorkflowLayoutManager;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;

public class WorkflowToJGraphxTransformer
{
    private WorkflowLayoutManager workflowLayoutManager;

    public WorkflowToJGraphxTransformer(WorkflowLayoutManager workflowLayoutManager)
    {
        this.workflowLayoutManager = workflowLayoutManager;
    }

    public mxGraph transform(JiraWorkflow workflow, int stepId, boolean showLabels) throws Exception
    {
        JWDLayout mergedLayout = workflowLayoutManager.getLayoutForWorkflow(workflow);
        mxGraph graph = JGraphxJWDLayoutProcessor.mxGraphFromJWDLayout(mergedLayout.getRoots(), workflow, stepId, showLabels);
        return graph;
    }
}
