/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model.layout.serialize;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.sysbliss.jira.plugins.workflow.model.layout.LayoutObject;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutPoint;

/**
 * @author jdoklovic
 * 
 */
@JsonDeserialize(as = SerializableEdgeImpl.class)
public interface SerializableEdge extends LayoutObject {

    public Integer getStartStepId();

    public void setStartStepId(Integer i);

    public Integer getEndStepId();

    public void setEndStepId(Integer i);

    public Integer getActionId();

    public void setActionId(Integer i);

    public String getStartNodeId();

    public void setStartNodeId(String id);

    public String getEndNodeId();

    public void setEndNodeId(String id);

    public LayoutPoint getStartPoint();

    public void setStartPoint(LayoutPoint p);

    public LayoutPoint getEndPoint();

    public void setEndPoint(LayoutPoint p);

    public List<LayoutPoint> getControlPoints();

    public void setControlPoints(List<LayoutPoint> l);

    public List<LayoutPoint> getAllPoints();

    public String getLineType();

    public void setLineType(String s);

    public LayoutPoint getLabelPoint();

    public void setLabelPoint(LayoutPoint p);
}
