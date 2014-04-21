package com.atlassian.jira.issue.issuetype;

import com.atlassian.jira.issue.MockIssueConstant;

/**
 * @since v3.13
 */
public class MockIssueType extends MockIssueConstant implements IssueType
{
    private boolean subTask;

    /**
     * Constructs a non subtask IssueType with the given id and name.
     * @param id the id.
     * @param name the name.
     */
    public MockIssueType(String id, String name)
    {
        this(id, name, false);
    }

    public MockIssueType(String id, String name, boolean subTask)
    {
        super(id, name);
        this.subTask = subTask;
    }

    public boolean isSubTask()
    {
        return subTask;
    }
}
