package com.sysbliss.jira.plugins.workflow.model;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.io.Serializable;

/**
 * Author: jdoklovic
 */
@JsonDeserialize(as = WorkflowAnnotationImpl.class)
public interface WorkflowAnnotation extends Serializable {

    void setId(String id);

    String getId();

    String getDescription();

    void setDescription(String desc);
}
