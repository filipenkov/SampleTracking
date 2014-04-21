package org.jcvi.jira.plugins.workflow;

import java.util.HashMap;
import java.util.Map;

/**
 * The variables stored in the transientVariables Map passed into a
 * Workflow PostFunction or Condition
 */
public enum TransientVariableTypes {
    //OSWorkflow Properties
    /** The step the issue is at AFTER the transition
     * actionId = 0
     * @see  com.opensymphony.workflow.spi.Step*/
    CREATED_STEP("createdStep"),
    /** A collection of step objects the issue is at BEFORE the transition,
     * actionId=the transition taken
     * @see  com.opensymphony.workflow.spi.Step*/
    CURRENT_STEP("currentSteps"),
    /** The action id of the transition used, as a String */
    ACTION_ID("actionId"),
    /** Access to the issues workflow history?
     * @see com.opensymphony.workflow.spi.WorkflowStore*/
    STORE("store"),
    /** Access to the workflow definition?
     * @see com.atlassian.jira.workflow.WorkflowDescriptorStore*/
    DESCRIPTOR("descriptor"),
    /** Unknown
     * @see com.opensymphony.workflow.spi.WorkflowEntry*/
    ENTRY("entry"),
    /** Only provides two methods: getCaller() and setRollbackOnly()
     * @see com.opensymphony.workflow.WorkflowContext*/
    CONTEXT("context"),
    /** @see com.opensymphony.workflow.config.Configuration */
    CONFIGURATION("configuration"),

    //JIRA Properties
    /** Project Key (short)m as a String*/
    PROJECT_KEY("pkey"),
    /** Project GenericValue object
     * @see org.ofbiz.core.entity.GenericValue*/
    PROJECT_OBJECT("project"),
    /** Issue Object
     * @see com.atlassian.jira.issue.MutableIssue */
    ISSUE_OBJECT("issue"),
    /** Issue Object before modification, or in the case of a sub-task being
     * created the parent issue.
     * @see com.atlassian.jira.issue.MutableIssue */
    ISSUE_ORIGINAL("originalissueobject"),
    /** Username, as a String*/
    USERNAME("username"),
    /** I'm unsure what values this can have */
    SEND_BULK_NOTIFICATION("SendBulkNotification"),

    //COMMENT Properties
    /** The comment added during the transition, as a String */
    COMMENT_VALUE("comment"),
    /** Only included if a comment was added, value=? */
    COMMENT_LEVEL("commentLevel"),
    /** Only included if a comment was added, value=? */
    COMMENT_ROLE("roleLevel");

    private static Map<String,TransientVariableTypes> lookup = null;

    private final String name;

    TransientVariableTypes(String variableName) {
        this.name = variableName;
    }

    public String getName() {
        return name;
    }

    public static TransientVariableTypes getByName(String variableName) {
        //not thread safe
        if (lookup == null) {
            lookup = new HashMap<String, TransientVariableTypes>();
            for( TransientVariableTypes var : TransientVariableTypes.values()) {
                lookup.put(var.getName(), var);
            }
        }
        return lookup.get(variableName);
    }
}
