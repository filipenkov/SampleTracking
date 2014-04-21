package com.sysbliss.jira.plugins.workflow.jgraph;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.sysbliss.jira.plugins.workflow.model.layout.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class WorkflowToJWDLayout {

    private HashMap<String, NodeLayout> nodeCache;
    private HashMap<String, EdgeLayout> edgeCache;

    public WorkflowToJWDLayout() {
        nodeCache = new HashMap();
        edgeCache = new HashMap();
    }

    public JWDLayout transform(JiraWorkflow workflow) {

        List<NodeLayout> roots = new ArrayList<NodeLayout>();

        List<ActionDescriptor> initialActions = workflow.getDescriptor().getInitialActions();
        for (ActionDescriptor action : initialActions) {

            LayoutRect rect = new LayoutRectImpl();
            rect.setX(0.0);
            rect.setY(0.0);
            rect.setWidth(0.0);
            rect.setHeight(0.0);

            NodeLayout rootLayout = new NodeLayoutImpl();
            rootLayout.setStepId(action.getId());
            rootLayout.setIsInitialAction(true);
            rootLayout.setRect(rect);
            rootLayout.setId(createUIDForAction(action, action.getId()));
            rootLayout.setLabel(action.getName());

            createEdgeLayout(action, rootLayout, workflow);

            roots.add(rootLayout);


        }

        //process "loner nodes"
        List<StepDescriptor> lonerNodes = (List<StepDescriptor>) CollectionUtils.select(workflow.getDescriptor().getSteps(), new LonerPredicate());
        for(StepDescriptor step : lonerNodes) {
            LayoutRect rect = new LayoutRectImpl();
            rect.setX(0.0);
            rect.setY(0.0);
            rect.setWidth(0.0);
            rect.setHeight(0.0);

            NodeLayout rootLayout = new NodeLayoutImpl();
            rootLayout.setStepId(step.getId());
            rootLayout.setIsInitialAction(false);
            rootLayout.setRect(rect);
            rootLayout.setId(createUIDForStep(step));
            rootLayout.setLabel(step.getName());

            roots.add(rootLayout);
        }

        final JWDLayout jwdLayout = new JWDLayoutImpl();
        jwdLayout.setWidth(3072);
        jwdLayout.setRoots(roots);

        return jwdLayout;
    }

    private void cacheNodeUID(NodeLayout root) {
        if (!nodeCache.containsKey(root.getId())) {
            nodeCache.put(root.getId(), root);

            List<EdgeLayout> edges = root.getOutLinks();
            for (EdgeLayout edge : edges) {
                cacheEdgeUID(edge);
            }
        }

    }

    private void cacheEdgeUID(EdgeLayout edge) {
        if (!edgeCache.containsKey(edge.getId())) {
            edgeCache.put(edge.getId(), edge);
        }

        cacheNodeUID(edge.getEndNode());
    }

    private NodeLayout createNodeLayout(StepDescriptor step, JiraWorkflow workflow) {
        String uid = createUIDForStep(step);
        NodeLayout nodeLayout = new NodeLayoutImpl();

        if (!nodeCache.containsKey(uid)) {
            nodeLayout = new NodeLayoutImpl();

            nodeCache.put(uid, nodeLayout);

            LayoutRect rect = new LayoutRectImpl();
            rect.setX(0.0);
            rect.setY(0.0);
            rect.setWidth(0.0);
            rect.setHeight(0.0);

            nodeLayout.setStepId(step.getId());
            nodeLayout.setIsInitialAction(false);
            nodeLayout.setRect(rect);
            nodeLayout.setId(uid);
            nodeLayout.setLabel(step.getName());

        } else {
            nodeLayout = nodeCache.get(uid);
        }

        createEdgesForStartNode(nodeLayout, step, workflow);

        return nodeLayout;
    }

    private void createEdgesForStartNode(NodeLayout startNode, StepDescriptor startStep, JiraWorkflow workflow) {
        List<ActionDescriptor> outActions = startStep.getActions();
        for (ActionDescriptor action : outActions) {
            String uid = createUIDForAction(action, startStep.getId());
            if (!edgeCache.containsKey(uid)) {
                createEdgeLayout(action, startNode, workflow);
            }
        }
    }

    private EdgeLayout createEdgeLayout(ActionDescriptor action, NodeLayout startNode, JiraWorkflow workflow) {
        EdgeLayout edgeLayout = new EdgeLayoutImpl();
        String uid = createUIDForAction(action, startNode.getStepId());
        if (!edgeCache.containsKey(uid)) {
            edgeLayout = new EdgeLayoutImpl();
            edgeLayout.setId(uid);
            edgeCache.put(uid, edgeLayout);

            edgeLayout.setLabel(action.getName());
            edgeLayout.setActionId(action.getId());
            edgeLayout.setStartStepId(startNode.getStepId());
            edgeLayout.setEndStepId(action.getUnconditionalResult().getStep());
            edgeLayout.setStartNode(startNode);

            startNode.getOutLinks().add(edgeLayout);
            edgeLayout.setLineType("straight");

            LayoutPoint startPoint = new LayoutPointImpl();
            startPoint.setX(0.0);
            startPoint.setY(0.0);

            LayoutPoint endPoint = new LayoutPointImpl();
            endPoint.setX(0.0);
            endPoint.setY(0.0);

            LayoutPoint labelPoint = new LayoutPointImpl();
            labelPoint.setX(-200.0);
            labelPoint.setY(-200.0);

            edgeLayout.setStartPoint(startPoint);
            edgeLayout.setEndPoint(endPoint);
            edgeLayout.setLabelPoint(labelPoint);

            NodeLayout endNode = createNodeLayout(workflow.getDescriptor().getStep(action.getUnconditionalResult().getStep()), workflow);
            edgeLayout.setEndNode(endNode);
            endNode.getInLinks().add(edgeLayout);

        } else {
            edgeLayout = edgeCache.get(uid);

        }

        return edgeLayout;
    }


    private String createUIDForAction(ActionDescriptor action, int startStepId) {
        StringBuffer uid = new StringBuffer(action.getName());
        uid.append(action.getId());
        uid.append(Integer.toString(startStepId));
        uid.append(Integer.toString(action.getUnconditionalResult().getStep()));

        return uid.toString();
    }

    private String createUIDForStep(StepDescriptor step) {
        StringBuffer uid = new StringBuffer(step.getName());
        uid.append(step.getId());

        return uid.toString();
    }

    private class LonerPredicate implements Predicate {


        public boolean evaluate(final Object object) {
            StepDescriptor testNode = (StepDescriptor) object;
            return (testNode.getActions() == null || testNode.getActions().size() < 1);
        }

    }
}
