package com.sysbliss.jira.plugins.workflow.jgraph;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.mxgraph.view.mxGraph;
import com.sysbliss.jira.plugins.workflow.manager.WorkflowLayoutManager;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.NodeLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.serialize.JSONLayoutDeserializer;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class WorkflowToJGraphxTransformer {

    private ConstantsManager constantsManager;
    private WorkflowLayoutManager workflowLayoutManager;

    public WorkflowToJGraphxTransformer(ConstantsManager constantsManager, WorkflowLayoutManager workflowLayoutManager) {

        this.constantsManager = constantsManager;
        this.workflowLayoutManager = workflowLayoutManager;
    }

    public mxGraph transform(JiraWorkflow workflow, int stepId, boolean showLabels) throws Exception {

        final JGraphxJWDLayoutProcessor processor = new JGraphxJWDLayoutProcessor(constantsManager);
        JWDLayout mergedLayout = workflowLayoutManager.getLayoutForWorkflow(workflow);

        mxGraph graph = processor.mxGraphFromJWDLayout(mergedLayout.getRoots(), workflow, stepId, showLabels);

        return graph;
    }

}
