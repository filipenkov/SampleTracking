package com.atlassian.jira.permission;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.status.Status;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.ofbiz.core.entity.GenericValue;


/**
 * Represents the context in which a permission evaluation is to be made.
 * Permission requests are of the form: subject, verb, object (eg. "User fred wishes to comment on ABC-123"), where:
 * subject = User object
 * verb = permission Id
 * object = Issue, project etc.
 * A PermissionContext encapsulates the object.
 */
public interface PermissionContext
{
    GenericValue getProject();


    public Issue getIssue();

    /** Whether the PermissionContext has an existing (not 'being created') issue.
     * This returns false on the second issue creation page, and on the quick sub-task creation form, where the
     * issue type is unknown
     */
    boolean isHasCreatedIssue();

    Status getStatus();

    /**
     * Given a Permission Context, get the relevant workflow step.
     */
    StepDescriptor getRelevantStepDescriptor();

    /**
     * Whether we have enough information to look up issue-specific (workflow) permissions.
     */
    boolean hasIssuePermissions();
}