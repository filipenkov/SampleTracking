package com.atlassian.jira.issue.search.handlers;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.PrioritySearcher;
import com.atlassian.jira.jql.query.PriorityClauseQueryFactory;
import com.atlassian.jira.jql.validator.PriorityValidator;

/**
 * Test for {@link com.atlassian.jira.issue.search.handlers.PrioritySearchHandlerFactory}.
 *
 * @since v4.0
 */
public class TestPrioritySearchHandlerFactory extends AbstractTestSimpleSearchHandlerFactory
{
    @Test
    public void testCreateSearchHandler()
    {
        _testSystemSearcherHandler(PrioritySearchHandlerFactory.class,
                PriorityClauseQueryFactory.class,
                PriorityValidator.class,
                SystemSearchConstants.forPriority(),
                PrioritySearcher.class, null);
    }
}
