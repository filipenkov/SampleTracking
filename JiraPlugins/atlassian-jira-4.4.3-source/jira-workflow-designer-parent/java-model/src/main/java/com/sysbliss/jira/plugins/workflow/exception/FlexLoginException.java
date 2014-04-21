/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.exception;

/**
 * @author jdoklovic
 * 
 */
public class FlexLoginException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 4631477825867354688L;
    private String msg;

    /**
     * @param message
     * @param cause
     */
    public FlexLoginException(final String message, final Throwable cause) {
	super(message, cause);
	this.msg = message;
    }

    /**
     * @param message
     */
    public FlexLoginException(final String message) {
	super(message);
	this.msg = message;
    }

    /**
     * @param cause
     */
    public FlexLoginException(final Throwable cause) {
	super(cause);
	this.msg = cause.getMessage();
    }

    @Override
    public String getMessage() {
	return this.msg;
    }

    public void setMessage(final String s) {
	this.msg = s;
    }
}
