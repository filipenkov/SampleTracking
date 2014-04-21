package com.atlassian.jira.plugin.issueoperation;

import com.atlassian.jira.issue.Issue;

/**
 * Abstract operation that allows the opertaions to define action URLs and Simple description in order to provide a
 * list of "Actions" that can be done on an issue.
 *
 * @since v4.0
 */
public abstract class AbstractActionBackedPluggableIssueOperation extends AbstractPluggableIssueOperation implements ActionBackedPluggableIssueOperation
{
    public String getSimpleDescription(Issue issue)
    {
        return descriptor.getName();
    }
}
