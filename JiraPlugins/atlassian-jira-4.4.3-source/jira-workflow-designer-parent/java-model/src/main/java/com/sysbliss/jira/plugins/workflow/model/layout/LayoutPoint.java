package com.sysbliss.jira.plugins.workflow.model.layout;

public interface LayoutPoint {

    public LayoutPoint getPositiveController();

    public void setPositiveController(final LayoutPoint p);

    public Double getX();

    public void setX(Double n);

    public Double getY();

    public void setY(Double n);
}
