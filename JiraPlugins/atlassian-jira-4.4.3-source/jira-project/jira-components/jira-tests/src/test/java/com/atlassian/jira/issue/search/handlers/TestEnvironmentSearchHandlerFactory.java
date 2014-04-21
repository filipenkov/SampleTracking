package com.atlassian.jira.issue.search.handlers;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.QuerySearcher;
import com.atlassian.jira.jql.query.EnvironmentClauseQueryFactory;
import com.atlassian.jira.jql.validator.EnvironmentValidator;

/**
 * Test for {@link com.atlassian.jira.issue.search.handlers.EnvironmentSearchHandlerFactory}.
 *
 * @since v4.0
 */
public class TestEnvironmentSearchHandlerFactory extends AbstractTestSimpleSearchHandlerFactory
{
    @Test
    public void testCreateSearchHandler()
    {
        _testSystemSearcherHandler(EnvironmentSearchHandlerFactory.class,
                EnvironmentClauseQueryFactory.class,
                EnvironmentValidator.class,
                SystemSearchConstants.forEnvironment(),
                QuerySearcher.class, null);
    }

}
