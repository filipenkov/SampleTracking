/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

/**
 * @author jdoklovic
 * 
 */
public class FlexJiraUserPrefsImpl implements FlexJiraUserPrefs {

    /**
     * 
     */
    private static final long serialVersionUID = 2940023897788016188L;
    private String username;
    private boolean confirmDeleteSelection;
    private boolean confirmDeleteWorkflow;

    /** {@inheritDoc} */
    public String getUsername() {
	return username;
    }

    /** {@inheritDoc} */
    public void setUsername(final String name) {
	this.username = name;

    }

    /** {@inheritDoc} */
    public boolean getConfirmDeleteSelection() {
	return confirmDeleteSelection;
    }

    /** {@inheritDoc} */
    public boolean getConfirmDeleteWorkflow() {
	return confirmDeleteWorkflow;
    }

    /** {@inheritDoc} */
    public void setConfirmDeleteSelection(final boolean b) {
	this.confirmDeleteSelection = b;

    }

    /** {@inheritDoc} */
    public void setConfirmDeleteWorkflow(final boolean b) {
	this.confirmDeleteWorkflow = b;

    }

}
