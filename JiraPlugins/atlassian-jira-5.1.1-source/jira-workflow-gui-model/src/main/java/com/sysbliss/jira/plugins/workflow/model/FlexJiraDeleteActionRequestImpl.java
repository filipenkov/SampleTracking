/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

/**
 * @author jdoklovic
 * 
 */
public class FlexJiraDeleteActionRequestImpl implements FlexJiraDeleteActionRequest {

    /**
     * 
     */
    private static final long serialVersionUID = -6470003725974044174L;
    private FlexJiraStep step;
    private FlexJiraAction action;

    /** {@inheritDoc} */
    public FlexJiraAction getAction() {
	return action;
    }

    /** {@inheritDoc} */
    public FlexJiraStep getStep() {
	return step;
    }

    /** {@inheritDoc} */
    public void setAction(final FlexJiraAction action) {
	this.action = action;

    }

    /** {@inheritDoc} */
    public void setStep(final FlexJiraStep step) {
	this.step = step;

    }

}
