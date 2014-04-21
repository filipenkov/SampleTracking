/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.util.List;

/**
 * @author jdoklovic
 * 
 */
public class FlexJiraResultImpl extends AbstractFlexWorkflowObject implements FlexJiraResult {

    /**
     * 
     */
    private static final long serialVersionUID = -1868641660471313801L;
    private String oldStatus;
    private String status;
    private int stepId;
    private List postFunctions;

    /** {@inheritDoc} */
    public String getOldStatus() {
	return oldStatus;
    }

    /** {@inheritDoc} */
    public List getPostFunctions() {
	return postFunctions;
    }

    /** {@inheritDoc} */
    public String getStatus() {
	return status;
    }

    /** {@inheritDoc} */
    public int getStepId() {
	return stepId;
    }

    /** {@inheritDoc} */
    public void setOldStatus(final String oldStatus) {
	this.oldStatus = oldStatus;

    }

    /** {@inheritDoc} */
    public void setPostFunctions(final List postFunctions) {
	this.postFunctions = postFunctions;

    }

    /** {@inheritDoc} */
    public void setStatus(final String status) {
	this.status = status;

    }

    /** {@inheritDoc} */
    public void setStepId(final int step) {
	this.stepId = step;

    }

}
