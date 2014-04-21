/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.util.List;

/**
 * @author jdoklovic
 * 
 */
public class FlexJiraConditionsImpl extends AbstractFlexWorkflowObject implements FlexJiraConditions {
    /**
     * 
     */
    private static final long serialVersionUID = -6996848027166003610L;
    private String type;
    private List conditions;

    /** {@inheritDoc} */
    public String getType() {
	return this.type;
    }

    /** {@inheritDoc} */
    public void setType(final String type) {
	this.type = type;

    }

    /** {@inheritDoc} */
    public List getConditions() {
	return this.conditions;
    }

    /** {@inheritDoc} */
    public void setConditions(final List conditions) {
	this.conditions = conditions;

    }
}
