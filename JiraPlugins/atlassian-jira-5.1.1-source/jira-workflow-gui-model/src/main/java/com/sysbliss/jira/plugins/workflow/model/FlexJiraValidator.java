/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.util.Map;

/**
 * @author jdoklovic
 * 
 */
public interface FlexJiraValidator extends FlexWorkflowObject {
    /**
     * @param type
     */
    void setType(String type);

    String getType();

    /**
     * @param args
     */
    void setArgs(Map args);

    Map getArgs();
}
