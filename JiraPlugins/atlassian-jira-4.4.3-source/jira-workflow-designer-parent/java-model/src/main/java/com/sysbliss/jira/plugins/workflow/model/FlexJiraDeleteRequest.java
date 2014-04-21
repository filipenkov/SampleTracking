/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author jdoklovic
 * 
 */
public interface FlexJiraDeleteRequest extends Serializable {
    public List getActionRequests();

    public void setActionRequests(List requests);

    public List getSteps();

    public void setSteps(List stepList);
}
