package com.sysbliss.jira.plugins.workflow.model.layout;

public class LayoutRectImpl extends LayoutPointImpl implements LayoutRect {
    private Double width;
    private Double height;

    public LayoutRectImpl() {
	super();
    }

    public Double getWidth() {
	return width;
    }

    public void setWidth(final Double n) {
	this.width = n;
    }

    public Double getHeight() {
	return height;
    }

    public void setHeight(final Double n) {
	this.height = n;
    }

}
