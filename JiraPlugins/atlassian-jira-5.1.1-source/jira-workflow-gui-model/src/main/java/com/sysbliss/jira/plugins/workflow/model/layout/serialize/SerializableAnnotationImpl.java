package com.sysbliss.jira.plugins.workflow.model.layout.serialize;

import com.sysbliss.jira.plugins.workflow.model.layout.AbstractLayoutObject;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRect;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRectImpl;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * Author: jdoklovic
 */
public class SerializableAnnotationImpl extends AbstractLayoutObject implements SerializableAnnotation {

    private LayoutRect rect;

    @JsonDeserialize(as = LayoutRectImpl.class)
    public LayoutRect getRect() {
        return rect;
    }

    @JsonDeserialize(as = LayoutRectImpl.class)
    public void setRect(final LayoutRect r) {
        this.rect = r;
    }
}
