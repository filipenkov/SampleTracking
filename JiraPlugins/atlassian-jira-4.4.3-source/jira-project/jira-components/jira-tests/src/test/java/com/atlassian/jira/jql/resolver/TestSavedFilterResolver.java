package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.MockSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;

import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.CollectionEnclosedIterable;
import com.atlassian.jira.util.collect.MockCloseableIterable;
import org.easymock.EasyMock;

import java.util.Collections;
import java.util.List;

/**
 * @since v4.0
 */
public class TestSavedFilterResolver extends MockControllerTestCase
{
    private SearchRequestManager searchRequestManager;
    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        searchRequestManager = mockController.getMock(SearchRequestManager.class);
    }

    @Test
    public void testGetSearchRequestEmptyLiteral() throws Exception
    {
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        final List<SearchRequest> result = filterResolver.getSearchRequest(theUser, Collections.singletonList(new QueryLiteral()));
        assertTrue(result.isEmpty());
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestEmptyString() throws Exception
    {
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);

        List<SearchRequest> result = filterResolver.getSearchRequest(theUser, Collections.singletonList(createLiteral("")));
        assertTrue(result.isEmpty());

        result = filterResolver.getSearchRequest(theUser, Collections.singletonList(createLiteral("    ")));
        assertTrue(result.isEmpty());

        mockController.verify();
    }

    @Test
    public void testGetSearchRequestNoSearchRequestById() throws Exception
    {
        searchRequestManager.getSearchRequestById(theUser, 123L);
        mockController.setReturnValue(null);
        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder().setName("123").setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.EXACT);
        searchRequestManager.search(builder.toSearchParameters(), theUser, 0, Integer.MAX_VALUE);
        mockController.setReturnValue(new SharedEntitySearchResult(CollectionEnclosedIterable.from(Collections.<SearchRequest>emptyList()), false, 0));
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        filterResolver.getSearchRequest(theUser, Collections.singletonList(createLiteral(123L)));
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestNoSearchRequestByName() throws Exception
    {
        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder().setName("NotAnId").setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.EXACT);
        searchRequestManager.search(builder.toSearchParameters(), theUser, 0, Integer.MAX_VALUE);
        mockController.setReturnValue(new SharedEntitySearchResult(CollectionEnclosedIterable.from(Collections.<SearchRequest>emptyList()), false, 0));
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        filterResolver.getSearchRequest(theUser, Collections.singletonList(createLiteral("NotAnId")));
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestFoundSearchRequestByNameForId() throws Exception
    {
        final MockSearchRequest searchRequest = new MockSearchRequest("dude");

        searchRequestManager.getSearchRequestById(theUser, 123L);
        mockController.setReturnValue(null);
        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder().setName("123").setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.EXACT);
        searchRequestManager.search(builder.toSearchParameters(), theUser, 0, Integer.MAX_VALUE);
        mockController.setReturnValue(new SharedEntitySearchResult(CollectionEnclosedIterable.from(CollectionBuilder.<SearchRequest>newBuilder(searchRequest).asList()), false, 0));
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        final List<SearchRequest> list = filterResolver.getSearchRequest(theUser, Collections.singletonList(createLiteral(123L)));
        assertTrue(list.contains(searchRequest));
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestFoundSearchRequestByIdForName() throws Exception
    {
        final MockSearchRequest searchRequest = new MockSearchRequest("dude");

        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder().setName("123").setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.EXACT);
        searchRequestManager.search(builder.toSearchParameters(), theUser, 0, Integer.MAX_VALUE);
        mockController.setReturnValue(new SharedEntitySearchResult(CollectionEnclosedIterable.from(Collections.emptyList()), false, 0));
        searchRequestManager.getSearchRequestById(theUser, 123L);
        mockController.setReturnValue(searchRequest);
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        final List<SearchRequest> list = filterResolver.getSearchRequest(theUser, Collections.singletonList(createLiteral("123")));
        assertTrue(list.contains(searchRequest));
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestFoundSearchRequestById() throws Exception
    {
        final MockSearchRequest searchRequest = new MockSearchRequest("dude");

        searchRequestManager.getSearchRequestById(theUser, 123L);
        mockController.setReturnValue(searchRequest);
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        final List<SearchRequest> list = filterResolver.getSearchRequest(theUser, Collections.singletonList(createLiteral(123L)));
        assertTrue(list.contains(searchRequest));
        mockController.verify();
    }
    
    @Test
    public void testGetSearchRequestFoundSearchRequestByIdOverrideSecurity() throws Exception
    {
        final MockSearchRequest searchRequest1 = new MockSearchRequest("dude", 1L);
        final MockSearchRequest searchRequest2 = new MockSearchRequest("fred", 2L);

        EasyMock.expect(searchRequestManager.getAll())
                .andReturn(new MockCloseableIterable<SearchRequest>(CollectionBuilder.<SearchRequest>newBuilder(searchRequest1, searchRequest2).asList()));
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        final List<SearchRequest> list = filterResolver.getSearchRequestOverrideSecurity(Collections.singletonList(createLiteral(1L)));
        assertTrue(list.contains(searchRequest1));
        assertEquals(1, list.size());
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestNoneFoundByIdOverrideSecurity() throws Exception
    {
        final MockSearchRequest searchRequest1 = new MockSearchRequest("dude", 1L);
        final MockSearchRequest searchRequest2 = new MockSearchRequest("fred", 2L);

        EasyMock.expect(searchRequestManager.getAll())
                .andReturn(new MockCloseableIterable<SearchRequest>(CollectionBuilder.<SearchRequest>newBuilder(searchRequest1, searchRequest2).asList()))
                .times(2);
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        final List<SearchRequest> list = filterResolver.getSearchRequestOverrideSecurity(Collections.singletonList(createLiteral(3L)));
        assertTrue(list.isEmpty());
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestFoundMultipleSearchRequestByName() throws Exception
    {
        final MockSearchRequest searchRequest1 = new MockSearchRequest("dude");
        final MockSearchRequest searchRequest2 = new MockSearchRequest("sweet");

        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder().setName("123").setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.EXACT);
        searchRequestManager.search(builder.toSearchParameters(), theUser, 0, Integer.MAX_VALUE);
        mockController.setReturnValue(new SharedEntitySearchResult(CollectionEnclosedIterable.from(CollectionBuilder.<SearchRequest>newBuilder(searchRequest1, searchRequest2).asList()), false, 0));
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        final List<SearchRequest> list = filterResolver.getSearchRequest(theUser, Collections.singletonList(createLiteral("123")));
        assertTrue(list.contains(searchRequest1));
        assertTrue(list.contains(searchRequest2));
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestFoundByNameOverrideSecurity() throws Exception
    {
        final MockSearchRequest searchRequest1 = new MockSearchRequest("dude", 1L, "filter1");
        final MockSearchRequest searchRequest2 = new MockSearchRequest("dude", 2L, "filter2");

        EasyMock.expect(searchRequestManager.getAll())
                .andReturn(new MockCloseableIterable<SearchRequest>(CollectionBuilder.<SearchRequest>newBuilder(searchRequest1, searchRequest2).asList()));

        replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        final List<SearchRequest> list = filterResolver.getSearchRequestOverrideSecurity(Collections.singletonList(createLiteral("FILTER1")));
        assertTrue(list.contains(searchRequest1));
        assertEquals(1, list.size());

        verify();
    }
}
