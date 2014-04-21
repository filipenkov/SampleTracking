package com.sysbliss.jira.plugins.workflow.model.layout;

import java.util.ArrayList;
import java.util.List;

public class EdgeLayoutImpl extends AbstractLayoutObject implements EdgeLayout {
    protected Integer startStepId;
    protected Integer endStepId;
    protected Integer actionId;
    protected NodeLayout startNode;
    protected NodeLayout endNode;
    protected LayoutPoint startPoint;
    protected LayoutPoint endPoint;
    protected LayoutPoint labelPoint;
    protected List<LayoutPoint> controlPoints;
    protected String lineType;

    public EdgeLayoutImpl() {
	super();
	this.controlPoints = new ArrayList<LayoutPoint>();
        this.labelPoint = new LayoutPointImpl();
        this.labelPoint.setX(new Double(-200));
        this.labelPoint.setY(new Double(-200));
    }

    public NodeLayout getStartNode() {
	return startNode;
    }

    public void setStartNode(final NodeLayout n) {
	this.startNode = n;
    }

    public NodeLayout getEndNode() {
	return endNode;
    }

    public void setEndNode(final NodeLayout n) {
	this.endNode = n;
    }

    public LayoutPoint getStartPoint() {
	return startPoint;
    }

    public void setStartPoint(final LayoutPoint p) {
	this.startPoint = p;
    }

    public LayoutPoint getEndPoint() {
	return endPoint;
    }

    public void setEndPoint(final LayoutPoint p) {
	this.endPoint = p;
    }

    public List<LayoutPoint> getControlPoints() {
	return controlPoints;
    }

    public void setControlPoints(final List<LayoutPoint> l) {
	this.controlPoints = l;
    }

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

    public LayoutPoint getLabelPoint()
    {
        return labelPoint;
    }

    public void setLabelPoint(LayoutPoint p)
    {
        this.labelPoint = p;
    }
}
