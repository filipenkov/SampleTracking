package com.sysbliss.jira.plugins.workflow.model.layout;

import java.util.List;

public interface EdgeLayout extends LayoutObject {

    public Integer getStartStepId();

    public void setStartStepId(Integer i);

    public Integer getEndStepId();

    public void setEndStepId(Integer i);

    public Integer getActionId();

    public void setActionId(Integer i);

    public NodeLayout getStartNode();

    public void setStartNode(NodeLayout n);

    public NodeLayout getEndNode();

    public void setEndNode(NodeLayout n);

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
