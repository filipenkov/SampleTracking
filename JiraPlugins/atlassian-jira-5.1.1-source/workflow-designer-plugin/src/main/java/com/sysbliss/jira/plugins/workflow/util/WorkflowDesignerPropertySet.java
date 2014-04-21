package com.sysbliss.jira.plugins.workflow.util;

/**
 * Represents the JIRA Workflow designer's property set storage engine.
 */
public interface WorkflowDesignerPropertySet
{
    void setProperty(String key, String value);

    String getProperty(String key);

    void removeProperty(String key);

    boolean hasProperty(String key);
}
