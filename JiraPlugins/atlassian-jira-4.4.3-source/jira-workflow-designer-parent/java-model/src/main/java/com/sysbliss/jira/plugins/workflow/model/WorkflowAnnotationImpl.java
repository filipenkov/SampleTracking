package com.sysbliss.jira.plugins.workflow.model;

/**
 * Author: jdoklovic
 */
public class WorkflowAnnotationImpl implements WorkflowAnnotation {

    private static final long serialVersionUID = -1777026786381904128L;
    private String id;
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
