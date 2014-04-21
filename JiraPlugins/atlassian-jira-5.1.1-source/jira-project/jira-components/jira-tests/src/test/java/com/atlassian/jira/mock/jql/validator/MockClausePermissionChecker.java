package com.atlassian.jira.mock.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.permission.ClausePermissionChecker;

/**
 * @since v4.0
 */
public class MockClausePermissionChecker implements ClausePermissionChecker
{
    private final boolean hasPerm;

    public MockClausePermissionChecker()
    {
        this(true);
    }

    public MockClausePermissionChecker(boolean hasPerm)
    {
        this.hasPerm = hasPerm;
    }

    public boolean hasPermissionToUseClause(final User user)
    {
        return hasPerm;
    }
}
