package com.atlassian.jira.mock.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.permission.ClausePermissionHandler;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;

/**
 * @since v4.0
 */
public class MockClausePermissionHandler implements ClausePermissionHandler
{
    private final boolean hasPerm;

    public MockClausePermissionHandler(final boolean hasPerm)
    {
         this.hasPerm = hasPerm;
    }

    public MockClausePermissionHandler()
    {
        this(true);
    }

    public Clause sanitise(final User user, final TerminalClause clause)
    {
        return clause;
    }

    public boolean hasPermissionToUseClause(final User user)
    {
        return hasPerm;
    }
}
