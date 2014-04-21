/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.exception;

/**
 * @author jdoklovic
 * 
 */
public class WorkflowDesignerServiceException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -3078639488601733208L;
    private String msg;

    /**
     * @param message
     * @param cause
     */
    public WorkflowDesignerServiceException(final String message, final Throwable cause) {
	super(message, cause);
	this.msg = message;
    }

    /**
     * @param message
     */
    public WorkflowDesignerServiceException(final String message) {
	super(message);
	this.msg = message;
    }

    /**
     * @param cause
     */
    public WorkflowDesignerServiceException(final Throwable cause) {
	super(cause);
	this.msg = cause.getMessage();
    }

    public String getMessage() {
	return this.msg;
    }

    public void setMessage(final String s) {
	this.msg = s;
    }

}
