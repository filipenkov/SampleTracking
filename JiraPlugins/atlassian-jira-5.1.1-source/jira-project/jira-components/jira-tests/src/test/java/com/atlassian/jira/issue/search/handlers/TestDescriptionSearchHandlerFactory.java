package com.atlassian.jira.issue.search.handlers;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.QuerySearcher;
import com.atlassian.jira.jql.query.DescriptionClauseQueryFactory;
import com.atlassian.jira.jql.validator.DescriptionValidator;

/**
 * Simple test for {@link com.atlassian.jira.issue.search.handlers.DescriptionSearchHandlerFactory}.
 *
 * @since v4.0
 */
public class TestDescriptionSearchHandlerFactory extends AbstractTestSimpleSearchHandlerFactory
{
    @Test
    public void testCreateSearchHandler()
    {
        _testSystemSearcherHandler(DescriptionSearchHandlerFactory.class,
                DescriptionClauseQueryFactory.class,
                DescriptionValidator.class,
                SystemSearchConstants.forDescription(),
                QuerySearcher.class, null);
    }
}
