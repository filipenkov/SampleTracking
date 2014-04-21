/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.io.Serializable;

/**
 * @author jdoklovic
 * 
 */
public interface FlexJiraDeleteActionRequest extends Serializable {
    public FlexJiraStep getStep();

    public void setStep(FlexJiraStep step);

    public FlexJiraAction getAction();

    public void setAction(FlexJiraAction action);
}
