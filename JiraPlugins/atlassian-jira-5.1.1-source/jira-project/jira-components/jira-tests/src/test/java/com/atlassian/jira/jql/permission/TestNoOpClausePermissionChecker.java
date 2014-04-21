package com.atlassian.jira.jql.permission;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @since v4.0
 */
public class TestNoOpClausePermissionChecker extends ListeningTestCase
{
    @Test
    public void testAlwaysReturnsTrue() throws Exception
    {
        assertTrue(NoOpClausePermissionChecker.NOOP_CLAUSE_PERMISSION_CHECKER.hasPermissionToUseClause(null));
    }
}
