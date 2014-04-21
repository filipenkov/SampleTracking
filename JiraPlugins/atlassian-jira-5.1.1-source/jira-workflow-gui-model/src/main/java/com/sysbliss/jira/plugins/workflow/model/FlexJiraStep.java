/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.util.List;

/**
 * @author jdoklovic
 * 
 */
public interface FlexJiraStep extends FlexWorkflowObject, FlexJiraMetadataContainer {

    void setLinkedStatus(String status);

    String getLinkedStatus();

    void setActions(List actions);

    List getActions();

}
