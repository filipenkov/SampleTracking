/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model.layout.serialize;

import com.sysbliss.jira.plugins.workflow.model.layout.AnnotationLayout;

import java.util.List;
import java.util.Map;

/**
 * @author jdoklovic
 * 
 */
public interface JWDSerializableLayout {

    public List<String> getRootIds();

    public void setRootIds(List<String> ids);

    public Integer getWidth();

    public void setWidth(Integer width);

    public Map<String, SerializableNode> getNodeMap();

    public void setNodeMap(Map<String, SerializableNode> map);

    public Map<String, SerializableEdge> getEdgeMap();

    public void setEdgeMap(Map<String, SerializableEdge> map);

    public void setAnnotations(List<SerializableAnnotation> annotations);

    public List<SerializableAnnotation> getAnnotations();
}
