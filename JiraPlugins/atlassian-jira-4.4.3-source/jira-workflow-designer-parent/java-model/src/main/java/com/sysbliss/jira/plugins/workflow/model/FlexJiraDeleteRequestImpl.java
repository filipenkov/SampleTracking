/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.util.List;

/**
 * @author jdoklovic
 * 
 */
public class FlexJiraDeleteRequestImpl implements FlexJiraDeleteRequest {

    /**
     * 
     */
    private static final long serialVersionUID = 2440103112563456008L;
    private List actionRequests;
    private List steps;

    /** {@inheritDoc} */
    public List getActionRequests() {
	return actionRequests;
    }

    /** {@inheritDoc} */
    public List getSteps() {
	return steps;
    }

    /** {@inheritDoc} */
    public void setActionRequests(final List requests) {
	this.actionRequests = requests;

    }

    /** {@inheritDoc} */
    public void setSteps(final List stepList) {
	this.steps = stepList;

    }

}
