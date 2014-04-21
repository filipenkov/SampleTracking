package com.sysbliss.jira.plugins.workflow.model.layout;

/**
 * Author: jdoklovic
 */
public class AnnotationLayoutImpl extends AbstractLayoutObject implements AnnotationLayout {
    protected LayoutRect rect;

    public LayoutRect getRect() {
        return rect;
    }

    public void setRect(final LayoutRect r) {
        this.rect = r;
    }
}
