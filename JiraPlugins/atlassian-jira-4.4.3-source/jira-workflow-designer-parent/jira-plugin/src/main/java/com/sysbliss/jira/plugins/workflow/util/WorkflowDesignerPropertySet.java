package com.sysbliss.jira.plugins.workflow.util;

/**
 * Author: jdoklovic
 */
public interface WorkflowDesignerPropertySet {

    void setProperty(String key, String value);
    String getProperty(String key);
    void removeProperty(String key);

    boolean hasProperty(String key);
}
