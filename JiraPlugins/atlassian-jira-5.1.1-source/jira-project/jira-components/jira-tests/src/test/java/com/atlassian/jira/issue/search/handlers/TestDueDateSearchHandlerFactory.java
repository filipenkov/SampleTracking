package com.atlassian.jira.issue.search.handlers;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.DueDateSearcher;
import com.atlassian.jira.jql.query.DueDateClauseQueryFactory;
import com.atlassian.jira.jql.validator.DueDateValidator;

/**
 * Test for {@link com.atlassian.jira.issue.search.handlers.DueDateSearchHandlerFactory}.
 *
 * @since v4.0
 */
public class TestDueDateSearchHandlerFactory extends AbstractTestSimpleSearchHandlerFactory
{
    @Test
    public void testCreateSearchHandler()
    {
        _testSystemSearcherHandler(DueDateSearchHandlerFactory.class,
                DueDateClauseQueryFactory.class,
                DueDateValidator.class,
                SystemSearchConstants.forDueDate(),
                DueDateSearcher.class, null);
    }

}
