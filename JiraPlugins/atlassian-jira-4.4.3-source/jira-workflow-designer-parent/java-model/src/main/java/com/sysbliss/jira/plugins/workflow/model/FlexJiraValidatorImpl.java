/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.util.Map;

/**
 * @author jdoklovic
 * 
 */
public class FlexJiraValidatorImpl extends AbstractFlexWorkflowObject implements FlexJiraValidator {
    /**
     * 
     */
    private static final long serialVersionUID = -9212176574734976814L;
    private Map args;
    private String type;

    /** {@inheritDoc} */
    public Map getArgs() {
	return this.args;
    }

    /** {@inheritDoc} */
    public String getType() {
	return this.type;
    }

    /** {@inheritDoc} */
    public void setArgs(final Map args) {
	this.args = args;

    }

    /** {@inheritDoc} */
    public void setType(final String type) {
	this.type = type;

    }
}
