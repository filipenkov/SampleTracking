/**
 *
 */
package com.sysbliss.jira.plugins.workflow.model.layout.serialize;

import com.sysbliss.jira.plugins.workflow.model.layout.*;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author jdoklovic
 */
public class JSONLayoutDeserializer implements LayoutDeserializer {

    private JWDSerializableLayout serializedLayout;
    private HashMap<String, NodeLayout> nodeLayoutMap;
    private HashMap<String, EdgeLayout> edgeLayoutMap;

    /**
     * {@inheritDoc}
     */
    public JWDLayout deserialize(final String packet) throws Exception {
        edgeLayoutMap = new HashMap<String, EdgeLayout>();
        nodeLayoutMap = new HashMap<String, NodeLayout>();
        final JWDLayout layout = new JWDLayoutImpl();
        final ObjectMapper mapper = new ObjectMapper();
        final JsonFactory jf = new JsonFactory();

        serializedLayout = mapper.readValue(jf.createJsonParser(new StringReader(packet)), JWDSerializableLayoutImpl.class);

        layout.setWidth(serializedLayout.getWidth());

        final List<String> rootIds = serializedLayout.getRootIds();
        int i;
        final int n = rootIds.size();
        final List<NodeLayout> roots = new ArrayList<NodeLayout>(n);
        for (i = 0; i < n; i++) {
            final String nodeId = rootIds.get(i);
            final NodeLayout node = deserializeNode(nodeId);
            roots.add(node);
        }

        layout.setRoots(roots);

        List<AnnotationLayout> annotationLayouts = new ArrayList<AnnotationLayout>();
        for(SerializableAnnotation annotation:serializedLayout.getAnnotations()) {
            AnnotationLayout annotationLayout = deserializeAnnotation(annotation);
            annotationLayouts.add(annotationLayout);
        }

        layout.setAnnotations(annotationLayouts);

        serializedLayout = null;
        edgeLayoutMap = null;
        nodeLayoutMap = null;

        return layout;
    }

    private AnnotationLayout deserializeAnnotation(SerializableAnnotation annotation) {
        AnnotationLayout annotationLayout = new AnnotationLayoutImpl();
        annotationLayout.setId(annotation.getId());
        annotationLayout.setRect(annotation.getRect());

        return annotationLayout;
    }

    /**
     * @param nodeId
     * @return
     */
    private NodeLayout deserializeNode(final String nodeId) {
        NodeLayout nodeLayout;
        if (nodeLayoutMap.containsKey(nodeId)) {
            nodeLayout = nodeLayoutMap.get(nodeId);
            return nodeLayout;
        }

        final SerializableNode node = serializedLayout.getNodeMap().get(nodeId);

        nodeLayout = new NodeLayoutImpl();
        nodeLayout.setId(node.getId());
        nodeLayout.setLabel(node.getLabel());

        nodeLayoutMap.put(nodeId, nodeLayout);

        nodeLayout.setStepId(node.getStepId());
        nodeLayout.setIsInitialAction(node.getIsInitialAction());
        nodeLayout.setRect(node.getRect());

        final List<EdgeLayout> inLinks = new ArrayList<EdgeLayout>(node.getInLinkIds().size());
        final List<EdgeLayout> outLinks = new ArrayList<EdgeLayout>(node.getOutLinkIds().size());

        String edgeId;
        final Iterator<String> inIt = node.getInLinkIds().iterator();
        while (inIt.hasNext()) {
            edgeId = inIt.next();
            final EdgeLayout edgeLayout = deserializeEdge(edgeId);
            inLinks.add(edgeLayout);
        }

        nodeLayout.setInLinks(inLinks);

        final Iterator<String> outIt = node.getOutLinkIds().iterator();
        while (outIt.hasNext()) {
            edgeId = outIt.next();
            final EdgeLayout edgeLayout = deserializeEdge(edgeId);
            outLinks.add(edgeLayout);
        }

        nodeLayout.setOutLinks(outLinks);

        return nodeLayout;
    }

    /**
     * @param edgeId
     * @return
     */
    private EdgeLayout deserializeEdge(final String edgeId) {
        EdgeLayout edgeLayout;
        if (edgeLayoutMap.containsKey(edgeId)) {
            edgeLayout = edgeLayoutMap.get(edgeId);
            return edgeLayout;
        }

        final SerializableEdge edge = serializedLayout.getEdgeMap().get(edgeId);

        edgeLayout = new EdgeLayoutImpl();
        edgeLayout.setId(edge.getId());
        edgeLayout.setLabel(edge.getLabel());

        edgeLayoutMap.put(edgeId, edgeLayout);

        edgeLayout.setActionId(edge.getActionId());
        edgeLayout.setStartStepId(edge.getStartStepId());
        edgeLayout.setEndStepId(edge.getEndStepId());
        edgeLayout.setStartPoint(edge.getStartPoint());
        edgeLayout.setEndPoint(edge.getEndPoint());
        edgeLayout.setControlPoints(edge.getControlPoints());
        edgeLayout.setLineType(edge.getLineType());
        edgeLayout.setLabelPoint(edge.getLabelPoint());

        final NodeLayout startNode = deserializeNode(edge.getStartNodeId());
        edgeLayout.setStartNode(startNode);

        final NodeLayout endNode = deserializeNode(edge.getEndNodeId());
        edgeLayout.setEndNode(endNode);

        return edgeLayout;
    }

}
