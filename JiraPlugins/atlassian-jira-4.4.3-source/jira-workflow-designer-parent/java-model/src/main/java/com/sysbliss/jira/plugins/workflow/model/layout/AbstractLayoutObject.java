package com.sysbliss.jira.plugins.workflow.model.layout;

public class AbstractLayoutObject implements LayoutObject {
    private String id;
    private String label;

    public AbstractLayoutObject() {
    }

    public String getId() {
        return id;
    }

    public void setId(final String s) {
        this.id = s;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String s) {
        this.label = s;
    }

}
