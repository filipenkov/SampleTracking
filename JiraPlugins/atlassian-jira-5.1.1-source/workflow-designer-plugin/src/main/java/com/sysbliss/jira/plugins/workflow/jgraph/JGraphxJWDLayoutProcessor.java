package com.sysbliss.jira.plugins.workflow.jgraph;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.sysbliss.jira.plugins.workflow.model.layout.EdgeLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutObject;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutPoint;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutPointImpl;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRect;
import com.sysbliss.jira.plugins.workflow.model.layout.NodeLayout;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.normalizeSpace;

public class JGraphxJWDLayoutProcessor
{
    public static final Logger log = Logger.getLogger(JGraphxJWDLayoutProcessor.class);
    public static final String ICON_PREFIX = "/images/icons/status_";
    public static final String ROOTS = "roots";
    public static final String GRAPH = "graph";
    private final Map<String, mxCell> jgraphVertexCache = new HashMap<String, mxCell>();
    private final Map<String, mxCell> jgraphEdgeCache = new HashMap<String, mxCell>();
    private final Map<String, NodeLayout> nodeLayoutCache = new HashMap<String, NodeLayout>();
    private final Map<String, EdgeLayout> edgeLayoutCache = new HashMap<String, EdgeLayout>();
    private final List<mxCell> rootCells = new ArrayList<mxCell>();
    private final ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();
    private final boolean showLabels;

    static {
        System.setProperty("java.awt.headless", "true");
        try
        {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            UIManager.put("JTextField", javax.swing.plaf.metal.MetalTextFieldUI.class.getName());
        }
        catch (final Exception e)
        {
            log.error("Cannot load MetalLookAndFeel", e);
        }
    }

    private JGraphxJWDLayoutProcessor(boolean showLabels) {
        this.showLabels = showLabels;
    }

    public static Map<String, Object> calculateLayout(final List<NodeLayout> roots, JiraWorkflow workflow)
    {
        return new JGraphxJWDLayoutProcessor(true).actuallyCalculateLayout(roots, workflow);
    }

    private synchronized Map<String, Object> actuallyCalculateLayout(final List<NodeLayout> roots, JiraWorkflow workflow)
    {
        Map<String, Object> returnVals = new HashMap<String, Object>();

        mxCell layer = new mxCell("", new mxGeometry(0, 0, 0, 0), JGraphxWorkflowStyles.KEY_LAYER);
        layer.setVertex(true);
        layer.setConnectable(false);

        mxGraph graph = mxGraphFromJWDLayout(layer, roots, workflow, -1);
        mxCell defaultParent = (mxCell) graph.getDefaultParent();
        mxCell parent = (mxCell) defaultParent.getChildAt(0);

        JWDParallelEdgeLayout edgeLayout = new JWDParallelEdgeLayout(graph, 30);

        mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
        layout.setFineTuning(true);

        //vertical spacing
        layout.setInterRankCellSpacing(40);

        //horizontal spacing
        layout.setIntraCellSpacing(70);

        layout.setResizeParent(true);
        layout.execute(parent);

        //kill control points
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++)
        {
            mxCell cell = (mxCell) parent.getChildAt(i);
            if (cell.isEdge())
            {
                cell.getGeometry().setPoints(null);
            }
        }

        edgeLayout.execute(defaultParent);

        mxRectangle graphBounds = graph.getMaximumGraphBounds();
        mxRectangle layerBounds = graph.getBoundingBox(parent, true);

        parent.getGeometry().setX(graphBounds.getCenterX() - layerBounds.getCenterX());

        List<NodeLayout> newRoots = createReturnLayout(graph, parent);

        //fix overlapping roots
        LayoutRect oldRect = null;

        for (NodeLayout root : newRoots)
        {
            if (oldRect != null)
            {
                root.getRect().setX(oldRect.getX() + oldRect.getWidth() + 20);
            }
            oldRect = root.getRect();
        }

        returnVals.put(ROOTS, newRoots);
        returnVals.put(GRAPH, graph);

        return returnVals;
    }

    public static mxGraph mxGraphFromJWDLayout(List<NodeLayout> roots, JiraWorkflow workflow, int stepId, boolean showLabels)
    {
        return new JGraphxJWDLayoutProcessor(showLabels).mxGraphFromJWDLayout(null, roots, workflow, stepId);
    }

    private mxGraph mxGraphFromJWDLayout(Object parent, List<NodeLayout> roots, JiraWorkflow workflow, int stepId)
    {
        mxGraph graph = new mxGraph()
        {
            // Overrides method to provide a cell label in the display
            public String convertValueToString(Object cell)
            {
                if (cell instanceof mxCell)
                {
                    Object value = ((mxCell) cell).getValue();
                    if (value instanceof LayoutObject)
                    {
                        String label = ((LayoutObject) value).getLabel();
                        //normalize whitespace since jgraphx does render some whitespace characters (e.g. tabs)
                        return normalizeSpace(label);
                    }
                }
                return super.convertValueToString(cell);
            }
        };

        graph.setKeepEdgesInBackground(true);
        mxRectangle bounds = new mxRectangle(0, 0, 3072, 2304);
        graph.setMinimumGraphSize(bounds);
        graph.setMaximumGraphBounds(bounds);
        graph.setAllowLoops(true);
        graph.setCellsBendable(true);

        mxStylesheet stylesheet = graph.getStylesheet();
        stylesheet.putCellStyle(JGraphxWorkflowStyles.KEY_LAYER, JGraphxWorkflowStyles.STYLE_LAYER);
        stylesheet.putCellStyle(JGraphxWorkflowStyles.KEY_JIRA_STEP, JGraphxWorkflowStyles.STYLE_JIRA_STEP);
        stylesheet.putCellStyle(JGraphxWorkflowStyles.KEY_JIRA_STEP_SELECTED, JGraphxWorkflowStyles.STYLE_JIRA_STEP_SELECTED);
        stylesheet.putCellStyle(JGraphxWorkflowStyles.KEY_JIRA_ACTION, JGraphxWorkflowStyles.STYLE_JIRA_ACTION);

        graph.setDefaultLoopStyle(JGraphxWorkflowStyles.JIRA_EDGE_LOOP);

        if (parent == null)
        {
            parent = graph.getDefaultParent();
        }
        else
        {
            graph.addCell(parent);
        }

        //convert the JWDLayout to JGraphx
        createGraphTree(graph, parent, roots, workflow, stepId);

        return graph;
    }

    private void createGraphTree(mxGraph graph, Object parent, List<NodeLayout> roots, JiraWorkflow workflow, int stepId)
    {
        graph.getModel().beginUpdate();

        try
        {
            NodeLayout rootLayout;
            final Iterator it = roots.iterator();
            while (it.hasNext())
            {
                rootLayout = (NodeLayout) it.next();
                mxCell node = createNode(graph, parent, rootLayout, workflow, stepId);
                rootCells.add(node);
            }
        }
        finally
        {
            graph.getModel().endUpdate();
        }
    }

    private mxCell createNode(mxGraph graph, Object parent, NodeLayout nodeLayout, JiraWorkflow workflow, int stepId)
    {
        mxCell node;

        if (!jgraphVertexCache.containsKey(nodeLayout.getId()))
        {
            String iconUrl = getIconUrlForStep(nodeLayout.getStepId(), workflow);
            String nodeStyle = JGraphxWorkflowStyles.KEY_JIRA_STEP + ";" + mxConstants.STYLE_IMAGE + "=" + iconUrl;

            if (stepId > -1 && nodeLayout.getStepId().equals(stepId) && !nodeLayout.getIsInitialAction())
            {
                nodeStyle = JGraphxWorkflowStyles.KEY_JIRA_STEP_SELECTED + ";" + mxConstants.STYLE_IMAGE + "=" + iconUrl;
            }

            node = (mxCell) graph.insertVertex(parent, null, nodeLayout, nodeLayout.getRect().getX(), nodeLayout.getRect().getY(), nodeLayout.getRect().getWidth(), nodeLayout.getRect().getHeight(), nodeStyle);

            if (node.getGeometry().getWidth() < 1 || node.getGeometry().getHeight() < 1)
            {
                graph.updateCellSize(node);
            }

            jgraphVertexCache.put(nodeLayout.getId(), node);
        }
        else
        {
            node = jgraphVertexCache.get(nodeLayout.getId());
        }

        //process edges
        final List<EdgeLayout> outLinks = nodeLayout.getOutLinks();
        for (int i = 0; i < outLinks.size(); i++)
        {
            EdgeLayout edgeLayout = outLinks.get(i);

            if (!jgraphEdgeCache.containsKey(edgeLayout.getId()))
            {
                createEdge(graph, parent, node, edgeLayout, workflow, stepId);
            }
        }

        return node;
    }

    private String getIconUrlForStep(Integer stepId, JiraWorkflow workflow)
    {
        URL genericUrl = this.getClass().getClassLoader().getResource("status-icons/status_generic_med.png");
        String iconUrl = genericUrl.toString();
        String origUrl = "";
        StepDescriptor step = workflow.getDescriptor().getStep(stepId);
        if (step != null)
        {
            Map metaAttributes = step.getMetaAttributes();
            if (metaAttributes != null)
            {
                String statusId = (String) metaAttributes.get("jira.status.id");
                Status status = constantsManager.getStatusObject(statusId);
                if (status != null)
                {
                    origUrl = status.getIconUrl();
                }
            }
        }

        //if it's an external image just default to generic
        if (origUrl.startsWith(ICON_PREFIX))
        {
            String statusName = StringUtils.substringBetween(origUrl, ICON_PREFIX, ".gif");
            String newName = "status-icons/status_" + statusName + "_med.png";
            URL url = this.getClass().getClassLoader().getResource(newName);
            if (url != null)
            {
                iconUrl = url.toString();
            }
        }
        return iconUrl;
    }

    private void createEdge(mxGraph graph, Object parent, mxCell startNode, EdgeLayout edgeLayout, JiraWorkflow workflow, int stepId)
    {
        if (!jgraphEdgeCache.containsKey(edgeLayout.getId()))
        {
            mxCell edge = new mxCell(edgeLayout, new mxGeometry(), "");
            edge.setEdge(true);
            edge.getGeometry().setRelative(false);

            //need to add it to cache before creating the end node
            jgraphEdgeCache.put(edgeLayout.getId(), edge);

            //create end node if needed
            mxCell endNode = createNode(graph, parent, edgeLayout.getEndNode(), workflow, stepId);

            mxPoint lineStart = new mxPoint(edgeLayout.getStartPoint().getX(), edgeLayout.getStartPoint().getY());
            mxPoint lineEnd = new mxPoint(edgeLayout.getEndPoint().getX(), edgeLayout.getEndPoint().getY());

            mxPoint startTerminate = new mxPoint(edgeLayout.getEndPoint().getX(), edgeLayout.getEndPoint().getY());
            mxPoint endBegin = new mxPoint(edgeLayout.getStartPoint().getX(), edgeLayout.getStartPoint().getY());


            if (edgeLayout.getControlPoints().size() > 0)
            {
                startTerminate = new mxPoint(edgeLayout.getControlPoints().get(0).getX(), edgeLayout.getControlPoints().get(0).getY());
                endBegin = new mxPoint(edgeLayout.getControlPoints().get(edgeLayout.getControlPoints().size() - 1).getX(), edgeLayout.getControlPoints().get(edgeLayout.getControlPoints().size() - 1).getY());
            }

            mxPoint startIntersect = getIntersection(lineStart, startTerminate, startNode);
            mxPoint endIntersect = getIntersection(endBegin, lineEnd, endNode);

            //mxPoint startIntersect = lineStart;
            //mxPoint endIntersect = lineEnd;

            //exit is startNode, entry is endNode
            //mxPoint exitPoint = new mxPoint(((startIntersect.getX() - startNode.getGeometry().getX()) / startNode.getGeometry().getWidth()), ((startIntersect.getY() - startNode.getGeometry().getY()) / startNode.getGeometry().getHeight()));
            //mxPoint entryPoint = new mxPoint(((endIntersect.getX() - endNode.getGeometry().getX()) / endNode.getGeometry().getWidth()), ((endIntersect.getY() - endNode.getGeometry().getY()) / endNode.getGeometry().getHeight()));

            double startPercentX = (startIntersect.getX() - startNode.getGeometry().getX()) / startNode.getGeometry().getWidth();
            double startPercentY = (startIntersect.getY() - startNode.getGeometry().getY()) / startNode.getGeometry().getHeight();

            double endPercentX = (endIntersect.getX() - endNode.getGeometry().getX()) / endNode.getGeometry().getWidth();
            double endPercentY = (endIntersect.getY() - endNode.getGeometry().getY()) / endNode.getGeometry().getHeight();

            mxPoint exitPoint = new mxPoint(startPercentX, startPercentY);
            mxPoint entryPoint = new mxPoint(endPercentX, endPercentY);


            StringBuffer edgeStyle = new StringBuffer(JGraphxWorkflowStyles.KEY_JIRA_ACTION).append(";").append(mxConstants.STYLE_NOLABEL).append("=").append(!showLabels);


            if (!Double.isNaN(entryPoint.getX()) && !Double.isNaN(entryPoint.getY()))
            {
                edgeStyle.append(";entryX=");
                edgeStyle.append(entryPoint.getX());
                edgeStyle.append(";entryY=");
                edgeStyle.append(entryPoint.getY());
                edgeStyle.append(";entryPerimeter=0");
            }

            if (!Double.isNaN(exitPoint.getX()) && !Double.isNaN(exitPoint.getY()))
            {
                edgeStyle.append(";exitX=");
                edgeStyle.append(exitPoint.getX());
                edgeStyle.append(";exitY=");
                edgeStyle.append(exitPoint.getY());
                edgeStyle.append(";exitPerimeter=0");
            }


            edge = (mxCell) graph.addEdge(edge, parent, startNode, endNode, null);

            edgeStyle.append(";");

            edge.setStyle(edgeStyle.toString());


            if (edgeLayout.getControlPoints().size() > 0)
            {
                if (edge.getGeometry().getPoints() == null)
                {
                    edge.getGeometry().setPoints(new ArrayList<mxPoint>());
                }

                for (LayoutPoint cpoint : edgeLayout.getControlPoints())
                {
                    edge.getGeometry().getPoints().add(new mxPoint(cpoint.getX(), cpoint.getY()));
                }
            }

            if (edgeLayout.getLabelPoint() != null && edgeLayout.getLabelPoint().getX() > 0 && edgeLayout.getLabelPoint().getY() > 0)
            {
                graph.getView().revalidate();
                mxCellState edgeState = graph.getView().getState(edge);
                double offsetX = edgeLayout.getLabelPoint().getX() - edgeState.getLabelBounds().getX();
                double offsetY = edgeLayout.getLabelPoint().getY() - edgeState.getLabelBounds().getY();
                edge.getGeometry().setOffset(new mxPoint(offsetX, offsetY));
            }
        }
    }

    private List<NodeLayout> createReturnLayout(mxGraph graph, Object parent)
    {
        final List<NodeLayout> returnList = new ArrayList<NodeLayout>();

        for (mxCell topLevelNode : rootCells)
        {
            NodeLayout rootNode = createReturnNode(graph, parent, topLevelNode);
            returnList.add(rootNode);
        }

        return returnList;
    }

    private NodeLayout createReturnNode(mxGraph graph, Object parent, mxCell node)
    {
        NodeLayout nodeLayout = (NodeLayout) node.getValue();

        if (!nodeLayoutCache.containsKey(nodeLayout.getId()))
        {
            final LayoutRect nodeRect = nodeLayout.getRect();
            final mxGeometry geom = node.getGeometry();
            double offsetX = ((mxCell) parent).getGeometry().getX();
            nodeRect.setX(geom.getX() + offsetX);
            nodeRect.setY(geom.getY());
            nodeRect.setWidth(geom.getWidth());
            nodeRect.setHeight(geom.getHeight());

            nodeLayout.setRect(nodeRect);

            nodeLayoutCache.put(nodeLayout.getId(), nodeLayout);
        }

        createReturnEdges(graph, parent, node);

        return nodeLayout;
    }

    private void createReturnEdges(mxGraph graph, Object parent, final mxCell startNode)
    {
        int edgeCount = startNode.getEdgeCount();
        mxCell edgeCell;
        EdgeLayout edgeLayout;
        double offsetX = ((mxCell) parent).getGeometry().getX();
        for (int e = 0; e < edgeCount; e++)
        {
            edgeCell = (mxCell) startNode.getEdgeAt(e);
            edgeLayout = (EdgeLayout) edgeCell.getValue();

            LayoutPoint labelPoint = new LayoutPointImpl();
            mxCellState edgeState = graph.getView().getState(edgeCell);

            labelPoint.setX(edgeState.getLabelBounds().getX() + offsetX);
            labelPoint.setY(edgeState.getLabelBounds().getY());

            edgeLayout.setLabelPoint(labelPoint);

            List<mxPoint> absPoints = edgeState.getAbsolutePoints();
            List<mxPoint> edgePoints = edgeCell.getGeometry().getPoints();
            if (edgePoints == null)
            {
                edgePoints = new ArrayList<mxPoint>();
            }

            LayoutPoint startPoint = new LayoutPointImpl();
            startPoint.setX(absPoints.get(0).getX() + offsetX);
            startPoint.setY(absPoints.get(0).getY());

            LayoutPoint endPoint = new LayoutPointImpl();
            endPoint.setX(absPoints.get(absPoints.size() - 1).getX() + offsetX);
            endPoint.setY(absPoints.get(absPoints.size() - 1).getY());

            edgeLayout.setStartPoint(startPoint);
            edgeLayout.setEndPoint(endPoint);

            if (!edgeLayoutCache.containsKey(edgeLayout.getId()))
            {

                edgeLayout.setLineType("straight");
                edgeLayout.getControlPoints().clear();

                if (edgePoints.size() > 0)
                {
                    // set points
                    for (int i = 0; i < edgePoints.size(); i++)
                    {
                        final mxPoint point = edgePoints.get(i);
                        final LayoutPoint layoutPoint = new LayoutPointImpl();
                        layoutPoint.setX(point.getX() + offsetX);
                        layoutPoint.setY(point.getY());

                        edgeLayout.getControlPoints().add(layoutPoint);
                        edgeLayout.setLineType("poly");

                    }
                }

                edgeLayoutCache.put(edgeLayout.getId(), edgeLayout);

                // create and set end node
                createReturnNode(graph, parent, (mxCell) edgeCell.getTarget());
            }
        }
    }

    private mxPoint getIntersection(mxPoint lineStart, mxPoint lineEnd, mxCell node)
    {
        mxPoint intersection;

        mxGeometry nodeRect = node.getGeometry();

        mxPoint bottomBoxStart = new mxPoint(node.getGeometry().getX(), node.getGeometry().getY() + node.getGeometry().getHeight());
        mxPoint bottomBoxEnd = new mxPoint(node.getGeometry().getX() + node.getGeometry().getWidth(), node.getGeometry().getY() + node.getGeometry().getHeight());

        intersection = getXIntersection(lineStart, lineEnd, bottomBoxStart, bottomBoxEnd);

        if (!Double.isNaN(intersection.getX()) && !Double.isNaN(intersection.getY()) && node.getGeometry().contains(intersection.getX(), intersection.getY()))
        {
            if (
                    (
                            nodeRect.contains(lineStart.getX(), lineStart.getY())
                                    && lineStart.getY() < lineEnd.getY()
                                    && intersection.getY() > lineStart.getY()
                    )
                            ||
                            (
                                    nodeRect.contains(lineEnd.getX(), lineEnd.getY())
                                            && lineStart.getY() > lineEnd.getY()
                                            && intersection.getY() > lineEnd.getY()
                            )
                    )
            {

                return intersection;
            }
        }

        mxPoint topBoxStart = new mxPoint(node.getGeometry().getX(), node.getGeometry().getY());
        mxPoint topBoxEnd = new mxPoint(node.getGeometry().getX() + node.getGeometry().getWidth(), node.getGeometry().getY());

        intersection = getXIntersection(lineStart, lineEnd, topBoxStart, topBoxEnd);

        if (!Double.isNaN(intersection.getX()) && !Double.isNaN(intersection.getY()) && node.getGeometry().contains(intersection.getX(), intersection.getY()))
        {
            if (
                    (
                            nodeRect.contains(lineStart.getX(), lineStart.getY())
                                    && lineStart.getY() > lineEnd.getY()
                                    && intersection.getY() < lineStart.getY()
                    )
                            ||
                            (
                                    nodeRect.contains(lineEnd.getX(), lineEnd.getY())
                                            && lineStart.getY() < lineEnd.getY()
                                            && intersection.getY() < lineEnd.getY()
                            )
                    )
            {

                return intersection;
            }
        }

        mxPoint rightBoxStart = new mxPoint(node.getGeometry().getX() + node.getGeometry().getWidth(), node.getGeometry().getY());
        mxPoint rightBoxEnd = new mxPoint(node.getGeometry().getX() + node.getGeometry().getWidth(), node.getGeometry().getY() + node.getGeometry().getHeight());

        intersection = getYIntersection(lineStart, lineEnd, rightBoxStart, rightBoxEnd);

        if (!Double.isNaN(intersection.getX()) && !Double.isNaN(intersection.getY()) && node.getGeometry().contains(intersection.getX(), intersection.getY()))
        {
            if (
                    (
                            nodeRect.contains(lineStart.getX(), lineStart.getY())
                                    && lineStart.getX() > lineEnd.getX()
                                    && intersection.getX() < lineStart.getX()
                    )
                            ||
                            (
                                    nodeRect.contains(lineEnd.getX(), lineEnd.getY())
                                            && lineStart.getX() < lineEnd.getX()
                                            && intersection.getX() < lineEnd.getX()
                            )
                    )
            {

                return intersection;
            }
        }

        mxPoint leftBoxStart = new mxPoint(node.getGeometry().getX(), node.getGeometry().getY());
        mxPoint leftBoxEnd = new mxPoint(node.getGeometry().getX(), node.getGeometry().getY() + node.getGeometry().getHeight());

        intersection = getYIntersection(lineStart, lineEnd, leftBoxStart, leftBoxEnd);

        if (!Double.isNaN(intersection.getX()) && !Double.isNaN(intersection.getY()) && node.getGeometry().contains(intersection.getX(), intersection.getY()))
        {
            if (
                    (
                            nodeRect.contains(lineStart.getX(), lineStart.getY())
                                    && lineStart.getX() < lineEnd.getX()
                                    && intersection.getX() > lineStart.getX()
                    )
                            ||
                            (
                                    nodeRect.contains(lineEnd.getX(), lineEnd.getY())
                                            && lineStart.getX() > lineEnd.getX()
                                            && intersection.getX() > lineEnd.getX()
                            )
                    )
            {

                return intersection;
            }
        }

        return new mxPoint(Double.NaN, Double.NaN);
    }

    private mxPoint getYIntersection(mxPoint lineStart, mxPoint lineEnd, mxPoint boxStart, mxPoint boxEnd)
    {
        double lineSlope = (lineEnd.getX() - lineStart.getX()) / (lineEnd.getY() - lineStart.getY());
        double boxSlope = (boxEnd.getX() - boxStart.getX()) / (boxEnd.getY() - boxStart.getY());

        double lineX = lineStart.getX() - lineSlope * lineStart.getY();
        double boxX = boxStart.getX() - boxSlope * boxStart.getY();

        double collisionY = (boxX - lineX) / (boxSlope - lineSlope);

        double newY;
        double newX = Math.floor(boxX);

        if (Double.isInfinite(lineSlope))
        {
            //line is vertical, we just need the Y and return the given X
            newY = lineStart.getY();
        }
        else
        {
            newY = Math.floor(boxStart.getY() - (boxStart.getY() + collisionY));
        }

        return new mxPoint(newX, newY);
    }

    private mxPoint getXIntersection(mxPoint lineStart, mxPoint lineEnd, mxPoint boxStart, mxPoint boxEnd)
    {
        double lineSlope = (lineEnd.getY() - lineStart.getY()) / (lineEnd.getX() - lineStart.getX());
        double boxSlope = (boxEnd.getY() - boxStart.getY()) / (boxEnd.getX() - boxStart.getX());

        double lineX = lineStart.getY() - lineSlope * lineStart.getX();
        double boxY = boxStart.getY() - boxSlope * boxStart.getX();

        double collisionX = (boxY - lineX) / (boxSlope - lineSlope);

        double newX;
        double newY = Math.floor(boxY);

        if (Double.isInfinite(lineSlope))
        {
            //line is vertical, we just need the Y and return the given X
            newX = lineStart.getX();
        }
        else
        {
            newX = Math.floor(boxStart.getX() - (boxStart.getX() + collisionX));
        }

        return new mxPoint(newX, newY);
    }
}
