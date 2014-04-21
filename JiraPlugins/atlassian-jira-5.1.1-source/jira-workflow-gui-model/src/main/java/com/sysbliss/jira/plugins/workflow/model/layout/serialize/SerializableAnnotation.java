package com.sysbliss.jira.plugins.workflow.model.layout.serialize;

import com.sysbliss.jira.plugins.workflow.model.layout.LayoutObject;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRect;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

@JsonDeserialize(as = SerializableAnnotationImpl.class)
public interface SerializableAnnotation extends LayoutObject {

    public LayoutRect getRect();

    public void setRect(LayoutRect r);
}
