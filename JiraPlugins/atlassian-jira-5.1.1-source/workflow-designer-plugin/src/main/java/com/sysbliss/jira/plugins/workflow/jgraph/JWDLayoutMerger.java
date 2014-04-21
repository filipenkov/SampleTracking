package com.sysbliss.jira.plugins.workflow.jgraph;

import com.sysbliss.jira.plugins.workflow.model.layout.EdgeLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.EdgeLayoutImpl;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayoutImpl;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutPoint;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutPointImpl;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRect;
import com.sysbliss.jira.plugins.workflow.model.layout.NodeLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.NodeLayoutImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: jdoklovic
 */
public class JWDLayoutMerger
{
    private final Map<String, NodeLayout> nodeCache = new HashMap<String, NodeLayout>();
    private final Map<String, EdgeLayout> edgeCache = new HashMap<String, EdgeLayout>();

    public static JWDLayout mergeLayouts(final JWDLayout savedLayout, final JWDLayout fullLayout)
    {
        return new JWDLayoutMerger().actuallyMergeLayouts(savedLayout, fullLayout);
    }

    private JWDLayoutMerger()
    {
    }

    private JWDLayout actuallyMergeLayouts(final JWDLayout savedLayout, final JWDLayout fullLayout)
    {
        JWDLayout newLayout = new JWDLayoutImpl();
        newLayout.setIsDraftWorkflow(savedLayout.getIsDraftWorkflow());
        newLayout.setGraphBounds(savedLayout.getGraphBounds());
        newLayout.setWidth(savedLayout.getWidth());
        newLayout.setWorkflowName(savedLayout.getWorkflowName());
        newLayout.setRoots(new ArrayList<NodeLayout>());
        newLayout.setAnnotations(savedLayout.getAnnotations());

        List<EdgeLayout> missingEdges = new ArrayList<EdgeLayout>();

        LayoutObjects savedObjects = getAllNodesAndEdges(savedLayout);
        LayoutObjects fullObjects = getAllNodesAndEdges(fullLayout);

        for (NodeLayout nodeLayout : fullObjects.nodes)
        {
            if (CollectionUtils.find(savedObjects.nodes, new NodePredicate(nodeLayout)) == null)
            {
                savedObjects.nodes.add(nodeLayout);
            }
        }

        for (EdgeLayout edgeLayout : fullObjects.edges)
        {
            if (CollectionUtils.find(savedObjects.edges, new EdgePredicate(edgeLayout)) == null)
            {
                missingEdges.add(edgeLayout);
            }
        }

        List<NodeLayout> newRoots = (List<NodeLayout>) CollectionUtils.select(savedObjects.nodes, new RootPredicate());

        for (NodeLayout rootNode : newRoots)
        {
            NodeLayout newRoot = createNodeLayout(rootNode);
            newLayout.getRoots().add(newRoot);
        }

        //now add any missing edges
        for (EdgeLayout missingEdge : missingEdges)
        {
            List<NodeLayout> startNode = (List<NodeLayout>) CollectionUtils.select(nodeCache.values(), new NodePredicate(missingEdge.getStartNode()));
            if (startNode != null && startNode.size() > 0)
            {
                createEdgeLayout(missingEdge, startNode.get(0));
            }

        }

        return newLayout;
    }

    private NodeLayout createNodeLayout(final NodeLayout node)
    {
        NodeLayout newNode = new NodeLayoutImpl();
        List<NodeLayout> endNodeAsList = (List<NodeLayout>) CollectionUtils.select(nodeCache.values(), new NodePredicate(node));
        if (endNodeAsList == null || endNodeAsList.size() < 1)
        {
            nodeCache.put(node.getId(), newNode);

            newNode.setStepId(node.getStepId());
            newNode.setIsInitialAction(node.getIsInitialAction());
            newNode.setRect(node.getRect());
            newNode.setId(node.getId());
            newNode.setLabel(node.getLabel());

        }
        else
        {
            newNode = endNodeAsList.get(0);
        }

        createEdgesForStartNode(node, newNode);

        return newNode;
    }

    private void createEdgesForStartNode(final NodeLayout oldNode, final NodeLayout newNode)
    {
        List<EdgeLayout> outEdges = oldNode.getOutLinks();
        for (EdgeLayout edge : outEdges)
        {
            List<EdgeLayout> edgeAsList = (List<EdgeLayout>) CollectionUtils.select(edgeCache.values(), new EdgePredicate(edge));
            if (edgeAsList == null || edgeAsList.size() < 1)
            {
                createEdgeLayout(edge, newNode);
            }
        }
    }

    private void createEdgeLayout(final EdgeLayout edge, final NodeLayout newNode)
    {
        List<EdgeLayout> edgeAsList = (List<EdgeLayout>) CollectionUtils.select(edgeCache.values(), new EdgePredicate(edge));
        if (edgeAsList == null || edgeAsList.size() < 1)
        {
            EdgeLayout newEdge = new EdgeLayoutImpl();
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
            if (!pointInsideRect(edge.getStartPoint(), newNode.getRect()))
            {
                startPoint = new LayoutPointImpl();
                startPoint.setX(newNode.getRect().getX() + (newNode.getRect().getWidth() / 2));
                startPoint.setY(newNode.getRect().getY() + (newNode.getRect().getHeight() / 2));
            }

            LayoutPoint endPoint = edge.getEndPoint();
            if (!pointInsideRect(edge.getEndPoint(), endNode.getRect()))
            {
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
        }
    }

    private LayoutObjects getAllNodesAndEdges(final JWDLayout layout)
    {
        List<EdgeLayout> allEdges = new ArrayList<EdgeLayout>();
        List<NodeLayout> allNodes = new ArrayList<NodeLayout>();
        for (NodeLayout node : layout.getRoots())
        {
            processNodeForAllNodes(node, allNodes, allEdges);
        }

        return new LayoutObjects(allNodes, allEdges);
    }

    private void processNodeForAllNodes(final NodeLayout node, final List<NodeLayout> allNodes, final List<EdgeLayout> allEdges)
    {
        if (!allNodes.contains(node))
        {
            allNodes.add(node);
            for (EdgeLayout edge : node.getOutLinks())
            {
                processEdgesForAllEdges(edge, allNodes, allEdges);
            }
        }
    }

    private void processEdgesForAllEdges(final EdgeLayout edge, final List<NodeLayout> allNodes, final List<EdgeLayout> allEdges)
    {
        if (!allEdges.contains(edge))
        {
            allEdges.add(edge);
            processNodeForAllNodes(edge.getEndNode(), allNodes, allEdges);
        }
    }

    private static class LayoutObjects
    {
        private final List<EdgeLayout> edges;
        private final List<NodeLayout> nodes;

        public LayoutObjects(final List<NodeLayout> nodes, final List<EdgeLayout> edges)
        {
            this.nodes = nodes;
            this.edges = edges;
        }
    }

    private static class RootPredicate implements Predicate
    {
        public boolean evaluate(final Object object)
        {
            NodeLayout testNode = (NodeLayout) object;
            return (
                    testNode.getInLinks() != null
                            && testNode.getInLinks().size() < 1
            );
        }
    }

    private static class NodePredicate implements Predicate
    {
        private final NodeLayout nodeLayout;

        public NodePredicate(final NodeLayout nodeLayout)
        {
            this.nodeLayout = nodeLayout;
        }

        public boolean evaluate(final Object object)
        {
            NodeLayout testNode = (NodeLayout) object;
            return (
                    testNode.getStepId().equals(nodeLayout.getStepId())
                            && testNode.getIsInitialAction().equals(nodeLayout.getIsInitialAction())
            );
        }
    }

    private static class EdgePredicate implements Predicate
    {
        private final EdgeLayout edgeLayout;

        public EdgePredicate(final EdgeLayout edgeLayout)
        {
            this.edgeLayout = edgeLayout;
        }

        public boolean evaluate(final Object object)
        {
            EdgeLayout testEdge = (EdgeLayout) object;
            return (
                    testEdge.getActionId().equals(edgeLayout.getActionId())
                            && testEdge.getStartStepId().equals(edgeLayout.getStartStepId())
                            && testEdge.getEndStepId().equals(edgeLayout.getEndStepId())
            );
        }
    }

    private boolean pointInsideRect(final LayoutPoint point, final LayoutRect rect)
    {
        double w = rect.getWidth();
        double h = rect.getHeight();
        if (w < 0 || h < 0)
        {
            // At least one of the dimensions is negative...
            return false;
        }
        // Note: if either dimension is zero, tests below must return false...
        double x = rect.getX();
        double y = rect.getY();
        if (point.getX() < x || point.getY() < y)
        {
            return false;
        }
        w += x;
        h += y;
        //    overflow || intersect
        return ((w < x || w > point.getX()) && (h < y || h > point.getY()));
    }
}
