package com.sysbliss.jira.plugins.workflow.model.layout;

/**
 * Author: jdoklovic
 */
public interface AnnotationLayout extends LayoutObject {
    public LayoutRect getRect();

    public void setRect(LayoutRect r);
}
