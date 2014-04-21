package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.renderer.StatusSearchRenderer;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Set;

/**
 * @since v4.0
 */
public class TestStatusSearchContextVisibilityChecker extends MockControllerTestCase
{
    private StatusSearchRenderer statusSearchRenderer;
    private SearchContext searchContext;

    @Before
    public void setUp() throws Exception
    {
        statusSearchRenderer = mockController.getMock(StatusSearchRenderer.class);
        searchContext = mockController.getMock(SearchContext.class);
    }

    @Test
    public void testMultiple() throws Exception
    {
        statusSearchRenderer.getSelectListOptions(searchContext);
        mockController.setReturnValue(CollectionBuilder.newBuilder(new MockStatus("10", "blah"), new MockStatus("30", "blah")).asList());

        mockController.replay();
        final StatusSearchContextVisibilityChecker checker = new StatusSearchContextVisibilityChecker(statusSearchRenderer);
        final Set<String> result = checker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("10", "20").asList());
        
        assertEquals(1, result.size());
        assertTrue(result.contains("10"));
        mockController.verify();
    }
}
