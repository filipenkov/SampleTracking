package com.atlassian.jira.issue.search.handlers;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.QuerySearcher;
import com.atlassian.jira.jql.query.SummaryClauseQueryFactory;
import com.atlassian.jira.jql.validator.SummaryValidator;

/**
 * Test for {@link com.atlassian.jira.issue.search.handlers.SummarySearchHandlerFactory}.
 *
 * @since v4.0
 */
public class TestSummarySearchHandlerFactory extends AbstractTestSimpleSearchHandlerFactory
{
    @Test
    public void testCreateHandler() throws Exception
    {
        _testSystemSearcherHandler(SummarySearchHandlerFactory.class,
                SummaryClauseQueryFactory.class,
                SummaryValidator.class,
                SystemSearchConstants.forSummary(),
                QuerySearcher.class, null);
    }
}
