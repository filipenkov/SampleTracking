package com.sysbliss.jira.plugins.workflow.jgraph;

import com.sysbliss.jira.plugins.workflow.model.layout.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: jdoklovic
 */
public class JWDLayoutMerger {

    private JWDLayout savedLayout;
    private JWDLayout fullLayout;
    private JWDLayout newLayout;
    private HashMap<String, NodeLayout> nodeCache;
    private HashMap<String, EdgeLayout> edgeCache;

    public JWDLayout mergeLayouts(JWDLayout savedLayout, JWDLayout fullLayout) {
        nodeCache = new HashMap();
        edgeCache = new HashMap();
        this.savedLayout = savedLayout;
        this.fullLayout = fullLayout;
        this.newLayout = new JWDLayoutImpl();
        newLayout.setIsDraftWorkflow(savedLayout.getIsDraftWorkflow());
        newLayout.setGraphBounds(savedLayout.getGraphBounds());
        newLayout.setWidth(savedLayout.getWidth());
        newLayout.setWorkflowName(savedLayout.getWorkflowName());
        newLayout.setRoots(new ArrayList<NodeLayout>());
        newLayout.setAnnotations(savedLayout.getAnnotations());

        List<EdgeLayout> missingEdges = new ArrayList<EdgeLayout>();

        LayoutObjects savedObjects = getAllNodesAndEdges(savedLayout);
        LayoutObjects fullObjects = getAllNodesAndEdges(fullLayout);

        for (NodeLayout nodeLayout : fullObjects.nodes) {
            if (CollectionUtils.find(savedObjects.nodes, new NodePredicate(nodeLayout)) == null) {
                savedObjects.nodes.add(nodeLayout);
            }
        }

        for (EdgeLayout edgeLayout : fullObjects.edges) {
            if (CollectionUtils.find(savedObjects.edges, new EdgePredicate(edgeLayout)) == null) {
                missingEdges.add(edgeLayout);
            }
        }

        List<NodeLayout> newRoots = (List<NodeLayout>) CollectionUtils.select(savedObjects.nodes, new RootPredicate());
        List<NodeLayout> autoRoots = (List<NodeLayout>) CollectionUtils.select(fullObjects.nodes, new RootPredicate());


        for (NodeLayout rootNode : newRoots) {
            NodeLayout newRoot = createNodeLayout(rootNode);
            newLayout.getRoots().add(newRoot);
        }

        //now add any missing edges
        for (EdgeLayout missingEdge : missingEdges) {
            List<NodeLayout> startNode = (List<NodeLayout>) CollectionUtils.select(nodeCache.values(), new NodePredicate(missingEdge.getStartNode()));
            if (startNode != null && startNode.size() > 0) {
                EdgeLayout newEdge = createEdgeLayout(missingEdge, startNode.get(0));
            }

        }

        return newLayout;
    }

    private NodeLayout createNodeLayout(NodeLayout node) {
        NodeLayout newNode = new NodeLayoutImpl();
        List<NodeLayout> endNodeAsList = (List<NodeLayout>) CollectionUtils.select(nodeCache.values(), new NodePredicate(node));
        if (endNodeAsList == null || endNodeAsList.size() < 1) {
            nodeCache.put(node.getId(), newNode);

            newNode.setStepId(node.getStepId());
            newNode.setIsInitialAction(node.getIsInitialAction());
            newNode.setRect(node.getRect());
            newNode.setId(node.getId());
            newNode.setLabel(node.getLabel());

        } else {
            newNode = endNodeAsList.get(0);
        }

        createEdgesForStartNode(node, newNode);

        return newNode;
    }

    private void createEdgesForStartNode(NodeLayout oldNode, NodeLayout newNode) {
        List<EdgeLayout> outEdges = oldNode.getOutLinks();
        for (EdgeLayout edge : outEdges) {
            List<EdgeLayout> edgeAsList = (List<EdgeLayout>) CollectionUtils.select(edgeCache.values(), new EdgePredicate(edge));
            if (edgeAsList == null || edgeAsList.size() < 1) {
                createEdgeLayout(edge, newNode);
            }
        }
    }

    private EdgeLayout createEdgeLayout(EdgeLayout edge, NodeLayout newNode) {
        EdgeLayout newEdge = new EdgeLayoutImpl();

        List<EdgeLayout> edgeAsList = (List<EdgeLayout>) CollectionUtils.select(edgeCache.values(), new EdgePredicate(edge));
        if (edgeAsList == null || edgeAsList.size() < 1) {
            newEdge = new EdgeLayoutImpl();
            newEdge.setId(edge.getId());
            edgeCache.put(edge.getId(), newEdge);

            newEdge.setLabel(edge.getLabel());
            newEdge.setActionId(edge.getActionId());
            newEdge.setStartStepId(edge.getStartStepId());
            newEdge.setEndStepId(edge.getEndStepId());
            newEdge.setStartNode(newNode);

            newNode.getOutLinks().add(newEdge);
            newEdge.setLineType(edge.getLineType());

            NodeLayout endNode = createNodeLayout(edge.getEndNode());

            LayoutPoint startPoint = edge.getStartPoint();
            if(!pointInsideRect(edge.getStartPoint(),newNode.getRect())) {
                startPoint = new LayoutPointImpl();
                startPoint.setX(newNode.getRect().getX() + (newNode.getRect().getWidth() / 2));
                startPoint.setY(newNode.getRect().getY() + (newNode.getRect().getHeight() / 2));
            }

            LayoutPoint endPoint = edge.getEndPoint();
            if(!pointInsideRect(edge.getEndPoint(),endNode.getRect())) {
                endPoint = new LayoutPointImpl();
                endPoint.setX(endNode.getRect().getX() + (endNode.getRect().getWidth() / 2));
                endPoint.setY(endNode.getRect().getY() + (endNode.getRect().getHeight() / 2));
            }

            newEdge.setStartPoint(startPoint);
            newEdge.setEndPoint(endPoint);
            newEdge.setLabelPoint(edge.getLabelPoint());
            newEdge.setControlPoints(edge.getControlPoints());


            newEdge.setEndNode(endNode);
            endNode.getInLinks().add(newEdge);

        } else {
            newEdge = edgeAsList.get(0);

        }

        return newEdge;
    }


    private LayoutObjects getAllNodesAndEdges(JWDLayout layout) {
        List<EdgeLayout> allEdges = new ArrayList<EdgeLayout>();
        List<NodeLayout> allNodes = new ArrayList<NodeLayout>();
        for (NodeLayout node : layout.getRoots()) {
            processNodeForAllNodes(node, allNodes, allEdges);
        }

        return new LayoutObjects(allNodes, allEdges);
    }

    private void processNodeForAllNodes(NodeLayout node, List<NodeLayout> allNodes, List<EdgeLayout> allEdges) {
        if (!allNodes.contains(node)) {
            allNodes.add(node);
            for (EdgeLayout edge : node.getOutLinks()) {
                processEdgesForAllEdges(edge, allNodes, allEdges);
            }
        }
    }

    private void processEdgesForAllEdges(EdgeLayout edge, List<NodeLayout> allNodes, List<EdgeLayout> allEdges) {
        if (!allEdges.contains(edge)) {
            allEdges.add(edge);
            processNodeForAllNodes(edge.getEndNode(), allNodes, allEdges);
        }
    }

    public class LayoutObjects {
        public final List<EdgeLayout> edges;
        public final List<NodeLayout> nodes;

        public LayoutObjects(List<NodeLayout> nodes, List<EdgeLayout> edges) {
            this.nodes = nodes;
            this.edges = edges;
        }
    }

    private class RootPredicate implements Predicate {

        public boolean evaluate(final Object object) {
            NodeLayout testNode = (NodeLayout) object;
            return (
                    testNode.getInLinks() != null
                            && testNode.getInLinks().size() < 1
            );
        }

    }


    private class NodePredicate implements Predicate {

        private final NodeLayout nodeLayout;

        public NodePredicate(final NodeLayout nodeLayout) {
            this.nodeLayout = nodeLayout;
        }

        public boolean evaluate(final Object object) {
            NodeLayout testNode = (NodeLayout) object;
            return (
                    testNode.getStepId().equals(nodeLayout.getStepId())
                            && testNode.getIsInitialAction().equals(nodeLayout.getIsInitialAction())
                            && testNode.getLabel().equals(nodeLayout.getLabel())
            );
        }

    }

    private class EdgePredicate implements Predicate {

        private final EdgeLayout edgeLayout;

        public EdgePredicate(final EdgeLayout edgeLayout) {
            this.edgeLayout = edgeLayout;
        }

        public boolean evaluate(final Object object) {
            EdgeLayout testEdge = (EdgeLayout) object;
            return (
                    testEdge.getActionId().equals(edgeLayout.getActionId())
                            && testEdge.getStartStepId().equals(edgeLayout.getStartStepId())
                            && testEdge.getEndStepId().equals(edgeLayout.getEndStepId())
                            && testEdge.getLabel().equals(edgeLayout.getLabel())
            );
        }

    }

    private boolean pointInsideRect(LayoutPoint point, LayoutRect rect) {
        double w = rect.getWidth();
        double h = rect.getHeight();
        if (w < 0 || h < 0) {
            // At least one of the dimensions is negative...
            return false;
        }
        // Note: if either dimension is zero, tests below must return false...
        double x = rect.getX();
        double y = rect.getY();
        if (point.getX() < x || point.getY() < y) {
            return false;
        }
        w += x;
        h += y;
        //    overflow || intersect
        return ((w < x || w > point.getX()) && (h < y || h > point.getY()));
    }
}
