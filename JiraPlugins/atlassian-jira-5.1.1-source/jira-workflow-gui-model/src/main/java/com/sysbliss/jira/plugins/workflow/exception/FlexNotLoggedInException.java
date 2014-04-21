/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.exception;

/**
 * @author jdoklovic
 * 
 */
public class FlexNotLoggedInException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -7770913732551760037L;
    private String msg;

    /**
     * @param message
     * @param cause
     */
    public FlexNotLoggedInException(final String message, final Throwable cause) {
	super(message, cause);
	this.msg = message;
    }

    /**
     * @param message
     */
    public FlexNotLoggedInException(final String message) {
	super(message);
	this.msg = message;
    }

    /**
     * @param cause
     */
    public FlexNotLoggedInException(final Throwable cause) {
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
