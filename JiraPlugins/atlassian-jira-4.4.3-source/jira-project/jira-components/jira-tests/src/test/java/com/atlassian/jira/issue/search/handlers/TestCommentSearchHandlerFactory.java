package com.atlassian.jira.issue.search.handlers;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.QuerySearcher;
import com.atlassian.jira.jql.query.CommentClauseQueryFactory;
import com.atlassian.jira.jql.validator.CommentValidator;

/**
 * Test for {@link com.atlassian.jira.issue.search.handlers.CommentSearchHandlerFactory}.
 *
 * @since v4.0
 */
public class TestCommentSearchHandlerFactory extends AbstractTestSimpleSearchHandlerFactory
{
    @Test
    public void testCreateSearchHandler()
    {
        _testSystemSearcherHandler(CommentSearchHandlerFactory.class, CommentClauseQueryFactory.class, 
                CommentValidator.class, SystemSearchConstants.forComments(), QuerySearcher.class, null);
    }
}
