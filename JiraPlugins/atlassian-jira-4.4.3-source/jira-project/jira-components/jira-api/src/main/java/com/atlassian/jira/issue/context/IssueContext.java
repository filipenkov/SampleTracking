package com.atlassian.jira.issue.context;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericValue;

/**
 * A context (scope) for an issue or custom field.
 * For example, global custom fields have an IssueContext whose project and issue type are null.
 */
public interface IssueContext
{
    /**
     * Gets the Project for this IssueContext.
     * <p> A null return value is used to represent that this context applies to all Projects.
     *
     * @return The Project for this IssueContext (can be null).
     */
    Project getProjectObject();
    /**
     * Gets the Project for this IssueContext.
     * <p> A null return value is used to represent that this context applies to all Projects.
     *
     * @return The Project for this IssueContext (can be null).
     * @deprecated Please use {@link #getProjectObject()}. Deprecated since v4.0
     */
    GenericValue getProject();

    /**
     * Gets the IssueType for this IssueContext.
     * <p> A null return value is used to represent that this context applies to all IssueTypes.
     *
     * @return The IssueType for this IssueContext (can be null).
     */
    IssueType getIssueTypeObject();
    /**
     * Gets the IssueType for this IssueContext.
     * <p> A null return value is used to represent that this context applies to all IssueTypes.
     *
     * @return The IssueType for this IssueContext (can be null).
     * @deprecated Please use {@link #getIssueTypeObject()}. Deprecated since v4.0
     */
    GenericValue getIssueType();
}
