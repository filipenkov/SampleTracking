package com.atlassian.jira.projectconfig.beans;

import com.atlassian.jira.issue.issuetype.IssueType;

/**
 * Representation of an IssueType in ignite. This is basically an issue type with the additional {@link #isDefault} flag.
 *
 * @since v4.4
 */
public interface SimpleIssueType extends NamedDefault
{
    String getIconUrl();
    String getDescription();
    String getId();
    boolean isSubTask();
    boolean isDefaultIssueType();
    IssueType getConstant();
}
