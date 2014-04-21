package com.sysbliss.jira.plugins.workflow.manager;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflows.layout.persistence.WorkflowLayoutPropertyKeyBuilder;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.sysbliss.jira.plugins.workflow.jgraph.JGraphxJWDLayoutProcessor;
import com.sysbliss.jira.plugins.workflow.jgraph.JWDLayoutMerger;
import com.sysbliss.jira.plugins.workflow.jgraph.WorkflowToJWDLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayoutImpl;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRect;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRectImpl;
import com.sysbliss.jira.plugins.workflow.model.layout.NodeLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.serialize.JSONLayoutDeserializer;
import com.sysbliss.jira.plugins.workflow.model.layout.serialize.JSONLayoutSerializer;
import com.sysbliss.jira.plugins.workflow.util.WorkflowDesignerPropertySet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.workflows.layout.persistence.WorkflowLayoutPropertyKeyBuilder.WorkflowState.DRAFT;
import static com.atlassian.jira.workflows.layout.persistence.WorkflowLayoutPropertyKeyBuilder.WorkflowState.LIVE;

public class WorkflowLayoutManagerImpl implements WorkflowLayoutManager
{
    private final WorkflowDesignerPropertySet workflowDesignerPropertySet;

    public WorkflowLayoutManagerImpl(final WorkflowDesignerPropertySet workflowDesignerPropertySet)
    {
        this.workflowDesignerPropertySet = workflowDesignerPropertySet;
    }

    public void saveActiveLayout(String workflowName, JWDLayout layout) throws Exception
    {
        saveLayout
                (
                        WorkflowLayoutPropertyKeyBuilder.newBuilder().
                                setWorkflowName(workflowName).
                                setWorkflowState(LIVE).
                                build(),
                        layout
                );
    }

    public void removeActiveLayout(String workflowName)
    {
        removeLayout
                (
                        WorkflowLayoutPropertyKeyBuilder.newBuilder().
                                setWorkflowName(workflowName).
                                setWorkflowState(LIVE).
                                build()
                );
    }

    public JWDLayout loadSavedActiveLayout(String workflowName) throws Exception
    {
        return loadSavedLayout
                (
                        WorkflowLayoutPropertyKeyBuilder.newBuilder().
                                setWorkflowName(workflowName).
                                setWorkflowState(WorkflowLayoutPropertyKeyBuilder.WorkflowState.LIVE).
                                build()
                );
    }

    public void saveDraftLayout(String parentWorkflowName, JWDLayout layout) throws Exception
    {
        saveLayout(WorkflowLayoutPropertyKeyBuilder.newBuilder().
                setWorkflowName(parentWorkflowName).
                setWorkflowState(DRAFT).
                build(), layout);
    }

    public void removeDraftLayout(String parentWorkflowName)
    {
        removeLayout
                (
                        WorkflowLayoutPropertyKeyBuilder.newBuilder().
                                setWorkflowName(parentWorkflowName).
                                setWorkflowState(DRAFT).
                                build()
                );
    }

    public JWDLayout loadSavedDraftLayout(String parentWorkflowName) throws Exception
    {
        return loadSavedLayout
                (
                        WorkflowLayoutPropertyKeyBuilder.newBuilder().
                                setWorkflowName(parentWorkflowName).
                                setWorkflowState(DRAFT).
                                build()
                );
    }

    public JWDLayout copyLayoutForDraftWorkflow(String parentWorkflowName) throws Exception
    {
        final String originalPropKey = WorkflowLayoutPropertyKeyBuilder.newBuilder().
                setWorkflowName(parentWorkflowName).
                setWorkflowState(LIVE).
                build();

        final String originalJson = workflowDesignerPropertySet.getProperty(originalPropKey);
        JWDLayout jwdLayout = new JWDLayoutImpl();
        if (StringUtils.isNotBlank(originalJson))
        {
            final JSONLayoutDeserializer deserializer = new JSONLayoutDeserializer();
            jwdLayout = deserializer.deserialize(originalJson);
            saveLayout
                    (
                            WorkflowLayoutPropertyKeyBuilder.newBuilder().
                                    setWorkflowName(parentWorkflowName).
                                    setWorkflowState(DRAFT).
                                    build(),
                            jwdLayout
                    );
        }

        return jwdLayout;
    }

    public void copyActiveLayout(String originalWorkflowName, String newWorkflowName) throws Exception
    {
        final String originalPropKey = WorkflowLayoutPropertyKeyBuilder.newBuilder().
                setWorkflowName(originalWorkflowName).
                setWorkflowState(LIVE).
                build();

        final String newPropKey = WorkflowLayoutPropertyKeyBuilder.newBuilder().
                setWorkflowName(newWorkflowName).
                setWorkflowState(LIVE).
                build();

        JWDLayout originalLayout = loadSavedLayout(originalPropKey);

        if (originalLayout.getRoots().size() > 0)
        {
            saveLayout(newPropKey, originalLayout);
        }
        else if ("jira".equals(originalWorkflowName))
        {
            try
            {
                //load the shipped default layout
                InputStream layoutJSON = this.getClass().getClassLoader().getResourceAsStream("layouts/default-layout.json");
                StringWriter writer = new StringWriter();
                IOUtils.copy(layoutJSON, writer);

                final JSONLayoutDeserializer deserializer = new JSONLayoutDeserializer();
                JWDLayout defaultLayout = deserializer.deserialize(writer.toString());
                saveLayout(newPropKey, defaultLayout);
            }
            catch (Exception e)
            {
                //oops.
            }
        }
    }

    public void publishDraftLayout(String parentWorkflowName) throws Exception
    {
        JWDLayout draftLayout = loadSavedDraftLayout(parentWorkflowName);

        if (draftLayout.getRoots().size() > 0)
        {
            saveActiveLayout(parentWorkflowName, draftLayout);
        }
    }

    public JWDLayout getLayoutForWorkflow(JiraWorkflow workflow) throws Exception
    {
        WorkflowToJWDLayout layoutMaker = new WorkflowToJWDLayout();
        JWDLayout jwdLayout = layoutMaker.transform(workflow);

        final List roots = jwdLayout.getRoots();

        Map<String, Object> processedLayout = JGraphxJWDLayoutProcessor.calculateLayout(roots, workflow);
        List<NodeLayout> autoRoots = (List<NodeLayout>) processedLayout.get(JGraphxJWDLayoutProcessor.ROOTS);

        mxGraph graph = (mxGraph) processedLayout.get(JGraphxJWDLayoutProcessor.GRAPH);

        //if we have a saved layout, we need to apply it on top of the previous auto-layout
        //this will ensure new nodes not in the saved layout will be put *somewhere*
        JWDLayout savedLayout;
        JWDLayout finalLayout;

        if (workflow.isDraftWorkflow())
        {
            savedLayout = loadSavedDraftLayout(workflow.getName());
        }
        else
        {
            savedLayout = loadSavedActiveLayout(workflow.getName());
        }

        if (savedLayout.getRoots().size() < 1 && workflow.isDefault() && "jira".equals(workflow.getName()))
        {
            //we now ship a default layout
            InputStream layoutJSON = this.getClass().getClassLoader().getResourceAsStream("layouts/default-layout.json");
            StringWriter writer = new StringWriter();
            IOUtils.copy(layoutJSON, writer);

            final JSONLayoutDeserializer deserializer = new JSONLayoutDeserializer();
            savedLayout = deserializer.deserialize(writer.toString());
        }

        if (savedLayout.getRoots().size() > 0)
        {
            jwdLayout.setRoots(autoRoots);
            finalLayout = JWDLayoutMerger.mergeLayouts(savedLayout, jwdLayout);
        }
        else
        {
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

    public JWDLayout calculateLayout(JiraWorkflow jiraWorkflow, JWDLayout jwdLayout)
    {
        final JWDLayout newJWDLayout = new JWDLayoutImpl();

        Map<String, Object> processedLayout = JGraphxJWDLayoutProcessor.calculateLayout(jwdLayout.getRoots(), jiraWorkflow);
        List<NodeLayout> autoRoots = (List<NodeLayout>) processedLayout.get(JGraphxJWDLayoutProcessor.ROOTS);

        newJWDLayout.setRoots(autoRoots);

        mxGraph graph = (mxGraph) processedLayout.get(JGraphxJWDLayoutProcessor.GRAPH);

        mxRectangle gb = graph.getGraphBounds();
        LayoutRect gBounds = new LayoutRectImpl();
        gBounds.setX(gb.getX());
        gBounds.setY(gb.getY());
        gBounds.setWidth(gb.getWidth());
        gBounds.setHeight(gb.getHeight());
        newJWDLayout.setGraphBounds(gBounds);

        return newJWDLayout;
    }

    protected void saveLayout(String propKey, JWDLayout layout) throws Exception
    {
        final JSONLayoutSerializer serializer = new JSONLayoutSerializer();
        final String json = serializer.serialize(layout);
        workflowDesignerPropertySet.setProperty(propKey, json);
    }

    protected void removeLayout(String propKey)
    {
        if (workflowDesignerPropertySet.hasProperty(propKey))
        {
            workflowDesignerPropertySet.removeProperty(propKey);
        }
    }

    protected JWDLayout loadSavedLayout(String propKey) throws Exception
    {
        JWDLayout jwdLayout;
        final String json = workflowDesignerPropertySet.getProperty(propKey);
        if (StringUtils.isBlank(json))
        {
            jwdLayout = new JWDLayoutImpl();
            jwdLayout.setWidth(0);
            jwdLayout.setRoots(Collections.<NodeLayout>emptyList());
            return jwdLayout;
        }

        final JSONLayoutDeserializer deserializer = new JSONLayoutDeserializer();
        jwdLayout = deserializer.deserialize(json);

        return jwdLayout;
    }
}
