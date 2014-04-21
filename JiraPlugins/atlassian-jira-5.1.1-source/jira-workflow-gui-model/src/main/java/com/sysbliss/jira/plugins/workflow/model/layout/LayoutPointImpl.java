package com.sysbliss.jira.plugins.workflow.model.layout;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

public class LayoutPointImpl implements LayoutPoint {

    private LayoutPoint positiveController;
    private Double x;
    private Double y;

    public LayoutPointImpl() {
    }

    @JsonDeserialize(as = LayoutPointImpl.class)
    public LayoutPoint getPositiveController() {
	return positiveController;
    }

    @JsonDeserialize(as = LayoutPointImpl.class)
    public void setPositiveController(final LayoutPoint p) {
	this.positiveController = p;
    }

    public Double getX() {
	return x;
    }

    public void setX(final Double n) {
	this.x = n;
    }

    public Double getY() {
	return y;
    }

    public void setY(final Double n) {
	this.y = n;
    }

}
