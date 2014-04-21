/**
 *
 */
package com.sysbliss.jira.plugins.workflow.model.layout.serialize;

import com.sysbliss.jira.plugins.workflow.model.layout.AnnotationLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.AnnotationLayoutImpl;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jdoklovic
 */
public class JWDSerializableLayoutImpl implements JWDSerializableLayout {

    private Map<String, SerializableEdge> edgeMap;
    private Map<String, SerializableNode> nodeMap;
    private List<SerializableAnnotation> annotations;
    private List<String> rootIds;
    private Integer width;

    public JWDSerializableLayoutImpl() {
        this.annotations = new ArrayList<SerializableAnnotation>();
        this.rootIds = new ArrayList<String>();
    }

    @JsonDeserialize(keyAs = String.class, contentAs = SerializableEdgeImpl.class)
    public Map<String, SerializableEdge> getEdgeMap() {
        return edgeMap;
    }

    @JsonDeserialize(keyAs = String.class, contentAs = SerializableNodeImpl.class)
    public Map<String, SerializableNode> getNodeMap() {
        return nodeMap;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getRootIds() {
        return rootIds;
    }

    /**
     * {@inheritDoc}
     */
    public Integer getWidth() {
        return width;
    }

    @JsonDeserialize(keyAs = String.class, contentAs = SerializableEdgeImpl.class)
    public void setEdgeMap(final Map<String, SerializableEdge> map) {
        this.edgeMap = map;
    }

    @JsonDeserialize(keyAs = String.class, contentAs = SerializableNodeImpl.class)
    public void setNodeMap(final Map<String, SerializableNode> map) {
        this.nodeMap = map;
    }

    /**
     * {@inheritDoc}
     */
    public void setRootIds(final List<String> ids) {
        this.rootIds = ids;
    }

    /**
     * {@inheritDoc}
     */
    public void setWidth(final Integer width) {
        this.width = width;
    }

    @JsonDeserialize(contentAs = SerializableAnnotationImpl.class)
    public List<SerializableAnnotation> getAnnotations() {
        return annotations;
    }

    @JsonDeserialize(contentAs = SerializableAnnotationImpl.class)
    public void setAnnotations(List<SerializableAnnotation> annotations) {
        this.annotations = annotations;
    }
}
