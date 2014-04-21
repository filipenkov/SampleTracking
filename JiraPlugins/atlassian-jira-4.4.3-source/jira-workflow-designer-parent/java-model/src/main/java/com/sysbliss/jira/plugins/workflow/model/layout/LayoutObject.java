package com.sysbliss.jira.plugins.workflow.model.layout;

import java.io.Serializable;

public interface LayoutObject extends Serializable
{
    String getId();

    void setId(String s);

    String getLabel();

    void setLabel(String s);
}
