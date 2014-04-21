/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

/**
 * @author jdoklovic
 * 
 */
public class FlexJiraFieldScreenImpl implements FlexJiraFieldScreen {

    /**
     * 
     */
    private static final long serialVersionUID = 2164356759783691497L;
    private String name;
    private String id;

    public FlexJiraFieldScreenImpl() {
	super();
    }

    /** {@inheritDoc} */
    public String getId() {
	return id;
    }

    /** {@inheritDoc} */
    public String getName() {
	return name;
    }

    /** {@inheritDoc} */
    public void setName(final String name) {
	this.name = name;

    }

    /** {@inheritDoc} */
    public void setId(final String id) {
	this.id = id;

    }
}
