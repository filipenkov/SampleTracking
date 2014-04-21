/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.io.Serializable;

/**
 * @author jdoklovic
 * 
 */
public interface FlexJiraUserPrefs extends Serializable {

    public String getUsername();

    public void setUsername(String name);

    public boolean getConfirmDeleteSelection();

    public void setConfirmDeleteSelection(boolean b);

    public boolean getConfirmDeleteWorkflow();

    public void setConfirmDeleteWorkflow(boolean b);
}
