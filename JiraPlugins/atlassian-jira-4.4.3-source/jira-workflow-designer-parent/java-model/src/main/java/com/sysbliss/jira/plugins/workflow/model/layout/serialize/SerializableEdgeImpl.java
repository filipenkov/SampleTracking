/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model.layout.serialize;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.sysbliss.jira.plugins.workflow.model.layout.AbstractLayoutObject;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutPoint;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutPointImpl;

/**
 * @author jdoklovic
 * 
 */
public class SerializableEdgeImpl extends AbstractLayoutObject implements SerializableEdge {

    private Integer startStepId;
    private Integer endStepId;
    private Integer actionId;
    private String startNodeId;
    private String endNodeId;
    private LayoutPoint startPoint;
    private LayoutPoint endPoint;
    private LayoutPoint labelPoint;
    private List<LayoutPoint> controlPoints;
    private String lineType;

    public SerializableEdgeImpl() {
	super();
	this.controlPoints = new ArrayList<LayoutPoint>();
    }

    @JsonDeserialize(as = LayoutPointImpl.class)
    public LayoutPoint getStartPoint() {
	return startPoint;
    }

    @JsonDeserialize(as = LayoutPointImpl.class)
    public void setStartPoint(final LayoutPoint p) {
	this.startPoint = p;
    }

    @JsonDeserialize(as = LayoutPointImpl.class)
    public LayoutPoint getEndPoint() {
	return endPoint;
    }

    @JsonDeserialize(as = LayoutPointImpl.class)
    public void setEndPoint(final LayoutPoint p) {
	this.endPoint = p;
    }

    @JsonDeserialize(as = LayoutPointImpl.class)
    public LayoutPoint getLabelPoint()
    {
        return labelPoint;
    }

    @JsonDeserialize(as = LayoutPointImpl.class)
    public void setLabelPoint(LayoutPoint p)
    {
        this.labelPoint = p;
    }

    @JsonDeserialize(contentAs = LayoutPointImpl.class)
    public List<LayoutPoint> getControlPoints() {
	return controlPoints;
    }

    @JsonDeserialize(contentAs = LayoutPointImpl.class)
    public void setControlPoints(final List<LayoutPoint> l) {
	this.controlPoints = l;
    }

    @JsonDeserialize(contentAs = LayoutPointImpl.class)
    public List<LayoutPoint> getAllPoints() {
	final List<LayoutPoint> tmp = new ArrayList<LayoutPoint>();
	tmp.add(startPoint);
	tmp.addAll(controlPoints);
	tmp.add(endPoint);

	return tmp;
    }

    public String getLineType() {
	return lineType;
    }

    public void setLineType(final String s) {
	this.lineType = s;
    }

    /** {@inheritDoc} */
    public String getEndNodeId() {
	return endNodeId;
    }

    /** {@inheritDoc} */
    public String getStartNodeId() {
	return startNodeId;
    }

    /** {@inheritDoc} */
    public void setEndNodeId(final String id) {
	this.endNodeId = id;
    }

    /** {@inheritDoc} */
    public void setStartNodeId(final String id) {
	this.startNodeId = id;
    }

    /** {@inheritDoc} */
    public Integer getActionId() {
	return actionId;
    }

    /** {@inheritDoc} */
    public Integer getEndStepId() {
	return endStepId;
    }

    /** {@inheritDoc} */
    public Integer getStartStepId() {
	return startStepId;
    }

    /** {@inheritDoc} */
    public void setActionId(final Integer i) {
	this.actionId = i;

    }

    /** {@inheritDoc} */
    public void setEndStepId(final Integer i) {
	this.endStepId = i;

    }

    /** {@inheritDoc} */
    public void setStartStepId(final Integer i) {
	this.startStepId = i;

    }

}
