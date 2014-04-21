package com.sysbliss.jira.plugins.workflow.manager;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.sysbliss.jira.plugins.workflow.WorkflowDesignerConstants;
import com.sysbliss.jira.plugins.workflow.jgraph.JGraphxJWDLayoutProcessor;
import com.sysbliss.jira.plugins.workflow.jgraph.JWDLayoutMerger;
import com.sysbliss.jira.plugins.workflow.jgraph.WorkflowToJWDLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.*;
import com.sysbliss.jira.plugins.workflow.model.layout.serialize.JSONLayoutDeserializer;
import com.sysbliss.jira.plugins.workflow.model.layout.serialize.JSONLayoutSerializer;
import com.sysbliss.jira.plugins.workflow.util.WorkflowDesignerPropertySet;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Author: jdoklovic
 */
public class WorkflowLayoutManagerImpl implements WorkflowLayoutManager {

    private final ConstantsManager constantsManager;
    private final WorkflowDesignerPropertySet workflowDesignerPropertySet;

    public WorkflowLayoutManagerImpl(ConstantsManager constantsManager, WorkflowDesignerPropertySet workflowDesignerPropertySet) {
        this.constantsManager = constantsManager;
        this.workflowDesignerPropertySet = workflowDesignerPropertySet;
    }

    public void saveActiveLayout(String workflowName, JWDLayout layout) throws Exception {
        final String propKey = WorkflowDesignerConstants.LAYOUT_PREFIX.concat(workflowName);
        saveLayout(propKey, layout);
    }

    public void removeActiveLayout(String workflowName) {
        final String propKey = WorkflowDesignerConstants.LAYOUT_PREFIX.concat(workflowName);
        removeLayout(propKey);
    }

    public JWDLayout loadSavedActiveLayout(String workflowName) throws Exception {
        final String propKey = WorkflowDesignerConstants.LAYOUT_PREFIX.concat(workflowName);
        return loadSavedLayout(propKey);
    }

    public void saveDraftLayout(String parentWorkflowName, JWDLayout layout) throws Exception {
        final String propKey = WorkflowDesignerConstants.LAYOUT_DRAFT_PREFIX.concat(parentWorkflowName);
        saveLayout(propKey, layout);
    }

    public void removeDraftLayout(String parentWorkflowName) {
        final String propKey = WorkflowDesignerConstants.LAYOUT_DRAFT_PREFIX.concat(parentWorkflowName);
        removeLayout(propKey);
    }

    public JWDLayout loadSavedDraftLayout(String parentWorkflowName) throws Exception {
        final String propKey = WorkflowDesignerConstants.LAYOUT_DRAFT_PREFIX.concat(parentWorkflowName);
        return loadSavedLayout(propKey);
    }

    public JWDLayout copyLayoutForDraftWorkflow(String parentWorkflowName) throws Exception {
        final String originalPropKey = WorkflowDesignerConstants.LAYOUT_PREFIX.concat(parentWorkflowName);
        final String originalJson = workflowDesignerPropertySet.getProperty(originalPropKey);
        JWDLayout jwdLayout = new JWDLayoutImpl();
        if (StringUtils.isNotBlank(originalJson)) {
            final JSONLayoutDeserializer deserializer = new JSONLayoutDeserializer();
            jwdLayout = deserializer.deserialize(originalJson);
            saveLayout(WorkflowDesignerConstants.LAYOUT_DRAFT_PREFIX.concat(parentWorkflowName), jwdLayout);
        }

        return jwdLayout;
    }

    public void copyActiveLayout(String originalWorkflowName, String newWorkflowName) throws Exception {
        final String originalPropKey = WorkflowDesignerConstants.LAYOUT_PREFIX.concat(originalWorkflowName);
        final String newPropKey = WorkflowDesignerConstants.LAYOUT_PREFIX.concat(newWorkflowName);
        JWDLayout originalLayout = loadSavedLayout(originalPropKey);

        if(originalLayout.getRoots().size() > 0) {

            saveLayout(newPropKey, originalLayout);
        } else if ("jira".equals(originalWorkflowName)) {
                try {
                    //load the shipped default layout
                    InputStream layoutJSON = this.getClass().getClassLoader().getResourceAsStream("layouts/default-layout.json");
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(layoutJSON, writer);

                    final JSONLayoutDeserializer deserializer = new JSONLayoutDeserializer();
                    JWDLayout defaultLayout = deserializer.deserialize(writer.toString());
                    saveLayout(newPropKey, defaultLayout);
                } catch (Exception e) {
                    //oops.
                }
        }
    }

    public void publishDraftLayout(String parentWorkflowName) throws Exception {
        JWDLayout draftLayout = loadSavedDraftLayout(parentWorkflowName);

        if(draftLayout.getRoots().size() > 0) {
            saveActiveLayout(parentWorkflowName, draftLayout);
        }
    }

    public JWDLayout getLayoutForWorkflow(JiraWorkflow workflow) throws Exception {
        WorkflowToJWDLayout layoutMaker = new WorkflowToJWDLayout();
        JWDLayout jwdLayout = layoutMaker.transform(workflow);

        final JGraphxJWDLayoutProcessor processor = new JGraphxJWDLayoutProcessor(constantsManager);
        final List roots = jwdLayout.getRoots();

        Map<String, Object> processedLayout = processor.calculateLayout(roots, workflow);
        List<NodeLayout> autoRoots = (List<NodeLayout>)processedLayout.get(JGraphxJWDLayoutProcessor.ROOTS);

        mxGraph graph = (mxGraph)processedLayout.get(JGraphxJWDLayoutProcessor.GRAPH);

        //if we have a saved layout, we need to apply it on top of the previous auto-layout
        //this will ensure new nodes not in the saved layout will be put *somewhere*
        JWDLayout savedLayout;
        JWDLayout finalLayout;

        if (workflow.isDraftWorkflow()) {
            savedLayout = loadSavedDraftLayout(workflow.getName());
        } else {
            savedLayout = loadSavedActiveLayout(workflow.getName());
        }

        if (savedLayout.getRoots().size() < 1 && workflow.isDefault() && "jira".equals(workflow.getName())) {
            //we now ship a default layout
            InputStream layoutJSON = this.getClass().getClassLoader().getResourceAsStream("layouts/default-layout.json");
            StringWriter writer = new StringWriter();
            IOUtils.copy(layoutJSON, writer);

            final JSONLayoutDeserializer deserializer = new JSONLayoutDeserializer();
            savedLayout = deserializer.deserialize(writer.toString());
        }

        if (savedLayout.getRoots().size() > 0) {
            JWDLayoutMerger merger = new JWDLayoutMerger();
            jwdLayout.setRoots(autoRoots);

            finalLayout = merger.mergeLayouts(savedLayout,jwdLayout);
        } else {
            finalLayout = new JWDLayoutImpl();
            finalLayout.setRoots(autoRoots);
        }

        mxRectangle gb = graph.getGraphBounds();
        LayoutRect gBounds = new LayoutRectImpl();
        gBounds.setX(gb.getX());
        gBounds.setY(gb.getY());
        gBounds.setWidth(gb.getWidth());
        gBounds.setHeight(gb.getHeight());
        finalLayout.setGraphBounds(gBounds);

        return finalLayout;
    }

    public JWDLayout calculateLayout(JiraWorkflow jiraWorkflow, JWDLayout jwdLayout) {

        final JGraphxJWDLayoutProcessor processor = new JGraphxJWDLayoutProcessor(constantsManager);

        final JWDLayout newJWDLayout = new JWDLayoutImpl();

        Map<String, Object> processedLayout = processor.calculateLayout(jwdLayout.getRoots(),jiraWorkflow);
        List<NodeLayout> autoRoots = (List<NodeLayout>)processedLayout.get(JGraphxJWDLayoutProcessor.ROOTS);

        newJWDLayout.setRoots(autoRoots);

        mxGraph graph = (mxGraph)processedLayout.get(JGraphxJWDLayoutProcessor.GRAPH);

        mxRectangle gb = graph.getGraphBounds();
        LayoutRect gBounds = new LayoutRectImpl();
        gBounds.setX(gb.getX());
        gBounds.setY(gb.getY());
        gBounds.setWidth(gb.getWidth());
        gBounds.setHeight(gb.getHeight());
        newJWDLayout.setGraphBounds(gBounds);

        return newJWDLayout;
    }

    protected void saveLayout(String propKey, JWDLayout layout) throws Exception {
        final JSONLayoutSerializer serializer = new JSONLayoutSerializer();
        final String json = serializer.serialize(layout);
        workflowDesignerPropertySet.setProperty(propKey, json);
    }

    protected void removeLayout(String propKey) {
        if(workflowDesignerPropertySet.hasProperty(propKey)) {
            workflowDesignerPropertySet.removeProperty(propKey);
        }
    }

    protected JWDLayout loadSavedLayout(String propKey) throws Exception {
        JWDLayout jwdLayout;
        final String json = workflowDesignerPropertySet.getProperty(propKey);
        if (StringUtils.isBlank(json)) {
            jwdLayout = new JWDLayoutImpl();
            jwdLayout.setWidth(0);
            jwdLayout.setRoots(ListUtils.EMPTY_LIST);
            return jwdLayout;
        }

        final JSONLayoutDeserializer deserializer = new JSONLayoutDeserializer();
        jwdLayout = deserializer.deserialize(json);

        return jwdLayout;
    }

}
