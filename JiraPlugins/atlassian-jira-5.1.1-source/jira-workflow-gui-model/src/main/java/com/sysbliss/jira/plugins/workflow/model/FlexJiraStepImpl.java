/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.util.List;
import java.util.Map;

/**
 * @author jdoklovic
 * 
 */
public class FlexJiraStepImpl extends AbstractFlexWorkflowObject implements FlexJiraStep {
    /**
     * 
     */
    private static final long serialVersionUID = 5052593010415117818L;
    private Map metaAttributes;
    private List actions;
    private String linkedStatus;

    /** {@inheritDoc} */
    public List getActions() {
	return this.actions;
    }

    /** {@inheritDoc} */
    public Map getMetaAttributes() {
	return this.metaAttributes;
    }

    /** {@inheritDoc} */
    public void setActions(final List actions) {
	this.actions = actions;

    }

    /** {@inheritDoc} */
    public void setMetaAttributes(final Map metaAttributes) {
	this.metaAttributes = metaAttributes;

    }

    /** {@inheritDoc} */
    public String getLinkedStatus() {
	return linkedStatus;
    }

    /** {@inheritDoc} */
    public void setLinkedStatus(final String status) {
	this.linkedStatus = status;

    }

}
