/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.exception;

/**
 * @author jdoklovic
 * 
 */
public class FlexNoPermissionException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -1177209701597283987L;
    private String msg;

    /**
     * @param message
     * @param cause
     */
    public FlexNoPermissionException(final String message, final Throwable cause) {
	super(message, cause);
	this.msg = message;
    }

    /**
     * @param message
     */
    public FlexNoPermissionException(final String message) {
	super(message);
	this.msg = message;
    }

    /**
     * @param cause
     */
    public FlexNoPermissionException(final Throwable cause) {
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
