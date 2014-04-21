/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.util.List;

/**
 * @author jdoklovic
 * 
 */
public interface FlexJiraResult extends FlexWorkflowObject {

    /**
     * @param oldStatus
     */
    void setOldStatus(String oldStatus);

    String getOldStatus();

    /**
     * @param status
     */
    void setStatus(String status);

    String getStatus();

    /**
     * @param step
     */
    void setStepId(int step);

    int getStepId();

    /**
     * @param convertFunctions
     */
    void setPostFunctions(List postFunctions);

    List getPostFunctions();

}
