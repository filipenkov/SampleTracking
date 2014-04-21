package com.atlassian.jira.bc.issue.search;

import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ListOrderedMessageSetImpl;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.Query;
import com.atlassian.query.order.SortOrder;
import com.opensymphony.user.User;
import com.atlassian.jira.local.ListeningTestCase;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

/**
 * Test for {@link com.atlassian.jira.bc.issue.search.LuceneCurrentSearchIssuePickerSearchProvider}.
 *
 * @since v4.2
 */
public class TestLuceneCurrentSearchIssuePickerSearchProvider extends ListeningTestCase
{
    @Test
    public void testGetId()
    {
        final LuceneCurrentSearchIssuePickerSearchProvider searchProvider = new LuceneCurrentSearchIssuePickerSearchProvider(null, null, null, null, null);
        assertEquals("cs", searchProvider.getId());
    }

    @Test
    public void testGetKey()
    {
        final LuceneCurrentSearchIssuePickerSearchProvider searchProvider = new LuceneCurrentSearchIssuePickerSearchProvider(null, null, null, null, null);
        assertEquals("jira.ajax.autocomplete.current.search", searchProvider.getLabelKey());
    }

    @Test
    public void testGetRequestNullJql()
    {
        final LuceneCurrentSearchIssuePickerSearchProvider searchProvider = new LuceneCurrentSearchIssuePickerSearchProvider(null, null, null, null, null);
        assertNull(searchProvider.getRequest(createParameters()));
    }

    /*
     * Test when the JQL parses correctly.
     */
    @Test
    public void testGetRequestNoErrors()
    {
        final String jql = "this should pass through";
        final Query expectedQuery = JqlQueryBuilder.newBuilder().where().assignee().eq("jack").endWhere().orderBy().assignee(SortOrder.DESC).buildQuery();
        final User user = new User("test", new MockProviderAccessor(), new MockCrowdService());
        final JiraAuthenticationContext ctx  = new MockSimpleAuthenticationContext(user);

        final IMocksControl mockControl = EasyMock.createControl();

        final SearchService service = mockControl.createMock(SearchService.class);
        EasyMock.expect(service.parseQuery(user, jql)).andReturn(new SearchService.ParseResult(expectedQuery, new ListOrderedMessageSetImpl()));

        mockControl.replay();

        final LuceneCurrentSearchIssuePickerSearchProvider searchProvider = new LuceneCurrentSearchIssuePickerSearchProvider(ctx, null, null, service, null);
        final SearchRequest request = searchProvider.getRequest(createParameters(null, jql));

        assertNotNull(request);
        assertEquals(expectedQuery, request.getQuery());

        mockControl.verify();
    }

    /*
     * Test when the JQL does not parse correctly.
     */
    @Test
    public void testGetRequestWithErrors()
    {
        final String jql = "this should pass through";
        final Query expectedQuery = JqlQueryBuilder.newBuilder().where().assignee().eq("jack").endWhere().orderBy().assignee(SortOrder.DESC).buildQuery();
        final User user = new User("test", new MockProviderAccessor(), new MockCrowdService());
        final JiraAuthenticationContext ctx  = new MockSimpleAuthenticationContext(user);
        final MessageSet errors = new ListOrderedMessageSetImpl();
        errors.addErrorMessage("I have an error");

        final IMocksControl mockControl = EasyMock.createControl();

        final SearchService service = mockControl.createMock(SearchService.class);
        EasyMock.expect(service.parseQuery(user, jql)).andReturn(new SearchService.ParseResult(expectedQuery, errors));

        mockControl.replay();

        final LuceneCurrentSearchIssuePickerSearchProvider searchProvider = new LuceneCurrentSearchIssuePickerSearchProvider(ctx, null, null, service, null);
        final SearchRequest request = searchProvider.getRequest(createParameters(null, jql));

        assertNull(request);
        mockControl.verify();
    }

    @Test
    public void testHandlesParameters() throws Exception
    {
        final LuceneCurrentSearchIssuePickerSearchProvider searchProvider = new LuceneCurrentSearchIssuePickerSearchProvider(null, null, null, null, null);

        //Should not accept null.
        assertFalse(searchProvider.handlesParameters(null, createParameters("", "something = bad")));
        assertFalse(searchProvider.handlesParameters(null, createParameters(null, "something = bad")));
        assertFalse(searchProvider.handlesParameters(null, createParameters("H", null)));
        assertTrue(searchProvider.handlesParameters(null, createParameters("H", "")));
        assertTrue(searchProvider.handlesParameters(null, createParameters("H", "sosmesms")));
    }

    private static IssuePickerSearchService.IssuePickerParameters createParameters(String query, String jql)
    {
        return new IssuePickerSearchService.IssuePickerParameters(query, jql, null, null, true, true, 10);
    }

    private static IssuePickerSearchService.IssuePickerParameters createParameters()
    {
        return createParameters(null, null);
    }
}
