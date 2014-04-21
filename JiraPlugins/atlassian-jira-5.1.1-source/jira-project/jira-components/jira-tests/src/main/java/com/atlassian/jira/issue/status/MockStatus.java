package com.atlassian.jira.issue.status;

import com.atlassian.jira.issue.MockIssueConstant;

/**
 * @since v3.13
 */
public class MockStatus extends MockIssueConstant implements Status
{
    public MockStatus(final String id, final String name)
    {
        super(id, name);
    }
}
