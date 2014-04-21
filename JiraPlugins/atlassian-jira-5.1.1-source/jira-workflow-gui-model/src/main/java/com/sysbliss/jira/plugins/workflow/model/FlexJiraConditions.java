/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.util.List;

/**
 * @author jdoklovic
 * 
 */
public interface FlexJiraConditions extends FlexWorkflowObject {

    void setType(String type);

    String getType();

    /**
     * @param nestedConditions
     */
    void setConditions(List conditions);

    List getConditions();
}
