package com.atlassian.jira.bc.issue.search;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.query.clause.Clause;
import com.atlassian.jira.local.ListeningTestCase;

/**
 * Test for {@link com.atlassian.jira.bc.issue.search.HistoryIssuePickerSearchProvider}.
 */
public class TestHistoryIssuePickerSearchProvider extends ListeningTestCase
{
    @Test
    public void testCreateQuery() throws Exception
    {
        final HistoryIssuePickerSearchProvider searchProvider = new HistoryIssuePickerSearchProvider(null, null, null);
        final SearchRequest request = searchProvider.getRequest(null);
        assertNotNull(request);
        final Clause expectedClause = JqlQueryBuilder.newBuilder().where().issueInHistory().buildClause();
        assertEquals(expectedClause, request.getQuery().getWhereClause());
        assertNull(request.getQuery().getOrderByClause());
    }

    @Test
    public void testHandlesParameters() throws Exception
    {
        final HistoryIssuePickerSearchProvider searchProvider = new HistoryIssuePickerSearchProvider(null, null, null);
        assertTrue(searchProvider.handlesParameters(null, new IssuePickerSearchService.IssuePickerParameters(null, null, null, null, false, false, 10)));
        assertTrue(searchProvider.handlesParameters(null, null));
    }

    @Test
    public void testGetId() throws Exception
    {
        final HistoryIssuePickerSearchProvider searchProvider = new HistoryIssuePickerSearchProvider(null, null, null);
        assertEquals("hs", searchProvider.getId());
    }

    @Test
    public void testGetKey() throws Exception
    {
        final HistoryIssuePickerSearchProvider searchProvider = new HistoryIssuePickerSearchProvider(null, null, null);
        assertEquals("jira.ajax.autocomplete.history.search", searchProvider.getLabelKey());
    }
}
