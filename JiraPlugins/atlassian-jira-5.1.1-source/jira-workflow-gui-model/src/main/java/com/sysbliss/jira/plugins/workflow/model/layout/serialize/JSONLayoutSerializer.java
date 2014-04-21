/**
 *
 */
package com.sysbliss.jira.plugins.workflow.model.layout.serialize;

import com.sysbliss.jira.plugins.workflow.model.layout.AnnotationLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.EdgeLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.NodeLayout;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * @author jdoklovic
 */
public class JSONLayoutSerializer implements LayoutSerializer {
    private JWDSerializableLayout _layout;
    private Map<String, SerializableEdge> _edgeMap;
    private Map<String, SerializableNode> _nodeMap;
    private List<SerializableAnnotation> _annotations;

    /**
     * {@inheritDoc}
     *
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     */
    public String serialize(final JWDLayout layout) throws Exception {
        this._layout = new JWDSerializableLayoutImpl();
        this._edgeMap = new HashMap<String, SerializableEdge>();
        this._nodeMap = new HashMap<String, SerializableNode>();
        this._annotations = new ArrayList<SerializableAnnotation>();

        final List<String> rootIds = new ArrayList<String>(layout.getRoots().size());

        _layout.setWidth(layout.getWidth());

        final List<NodeLayout> roots = layout.getRoots();
        int i;
        final int n = roots.size();
        for (i = 0; i < n; i++) {

            final NodeLayout nodeLayout = roots.get(i);
            serializeNode(nodeLayout);
            rootIds.add(nodeLayout.getId());
        }


        for(AnnotationLayout annotationLayout : layout.getAnnotations()) {
            serializeAnnotation(annotationLayout);
        }

        _layout.setRootIds(rootIds);
        _layout.setEdgeMap(_edgeMap);
        _layout.setNodeMap(_nodeMap);
        _layout.setAnnotations(_annotations);

        final ObjectMapper mapper = new ObjectMapper();
        final StringWriter sw = new StringWriter();
        mapper.writeValue(sw, _layout);

        this._layout = null;
        this._edgeMap = null;
        this._nodeMap = null;
        this._annotations = null;

        return sw.toString();
    }

    private void serializeAnnotation(AnnotationLayout annotationLayout) {
        final SerializableAnnotation annotation = new SerializableAnnotationImpl();
        annotation.setId(annotationLayout.getId());
        annotation.setRect(annotationLayout.getRect());

        _annotations.add(annotation);
    }

    /**
     * @param nodeLayout
     */
    private void serializeNode(final NodeLayout nodeLayout) {

        if (_nodeMap.containsKey(nodeLayout.getId())) {
            return;
        }

        final SerializableNode node = new SerializableNodeImpl();
        node.setId(nodeLayout.getId());
        node.setLabel(nodeLayout.getLabel());

        _nodeMap.put(nodeLayout.getId(), node);

        node.setStepId(nodeLayout.getStepId());
        node.setIsInitialAction(nodeLayout.getIsInitialAction());

        node.setRect(nodeLayout.getRect());

        final List<String> inLinkIds = new ArrayList<String>(nodeLayout.getInLinks().size());
        final List<String> outLinkIds = new ArrayList<String>(nodeLayout.getOutLinks().size());

        EdgeLayout edgeLayout;

        final Iterator<EdgeLayout> inIt = nodeLayout.getInLinks().iterator();
        while (inIt.hasNext()) {
            edgeLayout = inIt.next();
            serializeEdge(edgeLayout);
            inLinkIds.add(edgeLayout.getId());
        }

        node.setInLinkIds(inLinkIds);

        final Iterator<EdgeLayout> outIt = nodeLayout.getOutLinks().iterator();
        while (outIt.hasNext()) {
            edgeLayout = outIt.next();
            serializeEdge(edgeLayout);
            outLinkIds.add(edgeLayout.getId());
        }

        node.setOutLinkIds(outLinkIds);

    }

    private void serializeEdge(final EdgeLayout edgeLayout) {

        if (_edgeMap.containsKey(edgeLayout.getId())) {
            return;
        }

        final SerializableEdge edge = new SerializableEdgeImpl();
        edge.setId(edgeLayout.getId());
        edge.setLabel(edgeLayout.getLabel());

        _edgeMap.put(edgeLayout.getId(), edge);

        edge.setActionId(edgeLayout.getActionId());
        edge.setStartStepId(edgeLayout.getStartStepId());
        edge.setEndStepId(edgeLayout.getEndStepId());

        edge.setStartPoint(edgeLayout.getStartPoint());
        edge.setEndPoint(edgeLayout.getEndPoint());
        edge.setControlPoints(edgeLayout.getControlPoints());
        edge.setLineType(edgeLayout.getLineType());
        edge.setLabelPoint(edgeLayout.getLabelPoint());

        serializeNode(edgeLayout.getStartNode());
        edge.setStartNodeId(edgeLayout.getStartNode().getId());

        serializeNode(edgeLayout.getEndNode());
        edge.setEndNodeId(edgeLayout.getEndNode().getId());

    }

}
