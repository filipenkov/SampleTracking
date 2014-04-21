package com.atlassian.jira.issue.search;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.ActionParamsImpl;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import org.easymock.classextension.EasyMock;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since v4.0
 */
public class TestDefaultSearchRequestFactory extends MockControllerTestCase
{
    private IssueSearcherManager issueSearcherManager;
    private SearchSortUtil searchSortUtil;
    private DefaultSearchRequestFactory searchRequestFactory;
    private SearchService searchService;

    @Before
    public void setUp() throws Exception
    {
        issueSearcherManager = mockController.getMock(IssueSearcherManager.class);
        searchSortUtil = mockController.getMock(SearchSortUtil.class);
        searchService = mockController.getMock(SearchService.class);

        searchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService);
    }

    @Test
    public void testCreateNewSearchRequestLoaded() throws Exception
    {
        final SearchRequest oldSR = new SearchRequest(new QueryImpl(), "oldowner", "", "", 123L, 1L);
        final User user = new MockUser("newowner");
        mockController.replay();

        final SearchRequest newSR = searchRequestFactory.createNewSearchRequest(oldSR, user);
        assertEquals("oldowner", newSR.getOwnerUserName());
    }

    @Test
    public void testCreateNewSearchRequestNotLoaded() throws Exception
    {
        final SearchRequest oldSR = new SearchRequest(new QueryImpl(), "oldowner", "", "", null, 1L);
        final User user = new MockUser("newowner");
        mockController.replay();

        final SearchRequest newSR = searchRequestFactory.createNewSearchRequest(oldSR, user);
        assertEquals("newowner", newSR.getOwnerUserName());
    }

    @Test
    public void testCreateNewSearchRequestNullOldSR() throws Exception
    {
        final User user = new MockUser("newowner");
        mockController.replay();

        final SearchRequest newSR = searchRequestFactory.createNewSearchRequest(null, user);
        assertEquals("newowner", newSR.getOwnerUserName());
    }

    @Test
    public void testCreateNewSearchRequestNullOldSRAndUser() throws Exception
    {
        mockController.replay();

        final SearchRequest newSR = searchRequestFactory.createNewSearchRequest(null, null);
        assertNull(newSR.getOwnerUserName());
    }

    @Test
    public void testComparisonOfSearchRequestsStringsEqual() throws Exception
    {
        mockController.replay();
        MockJqlSearchRequest oldSearchRequest = new MockJqlSearchRequest(1l, new QueryImpl(new TerminalClauseImpl("monkey", Operator.EQUALS, "monkey"), new OrderByImpl(Collections.<SearchSort>emptyList()), "monkey = monkey"));
        MockJqlSearchRequest searchRequest = new MockJqlSearchRequest(1l, new QueryImpl(new TerminalClauseImpl("monkey", Operator.EQUALS, "monkey"), new OrderByImpl(Collections.<SearchSort>emptyList()), "monkey = monkey"));
        assertEquiv(oldSearchRequest, searchRequest);
    }

    @Test
    public void testComparisonOfSearchRequestsStringsNotEqual() throws Exception
    {
        mockController.replay();
        MockJqlSearchRequest oldSearchRequest = new MockJqlSearchRequest(1l, new QueryImpl(new TerminalClauseImpl("monkey", Operator.EQUALS, "monkey"), new OrderByImpl(Collections.<SearchSort>emptyList()), "monkey =    monkey"));
        MockJqlSearchRequest searchRequest = new MockJqlSearchRequest(1l, new QueryImpl(new TerminalClauseImpl("monkey", Operator.EQUALS, "monkey"), new OrderByImpl(Collections.<SearchSort>emptyList()), "monkey = monkey"));
        assertNotEquiv(oldSearchRequest, searchRequest);
    }

    @Test
    public void testComparisonOfSearchRequestsNonQueryAttributesUnequal() throws Exception
    {
        mockController.replay();
        SearchRequest oldSearchRequest = new SearchRequest(new QueryImpl(), "dude", "name1", null, 1L, 0L);
        SearchRequest searchRequest = new SearchRequest(new QueryImpl(), "dude", "name2", null, 2L, 0L);
        assertNotEquiv(oldSearchRequest, searchRequest);
    }

    @Test
    public void testComparisonOfSearchRequestsLegacyEqualBothNullQuery() throws Exception
    {
        mockController.replay();
        MockJqlSearchRequest oldSearchRequest = createSR(1L, null);
        MockJqlSearchRequest searchRequest = createSR(2L, null);
        assertEquiv(oldSearchRequest, searchRequest);
    }

    @Test
    public void testComparisonOfSearchRequestsLegacyEqualOneNullQuery() throws Exception
    {
        mockController.replay();
        MockJqlSearchRequest oldSearchRequest = createSR(1L, null);
        MockJqlSearchRequest searchRequest = createSR(2L, new QueryImpl(new TerminalClauseImpl("f", Operator.EQUALS, "v")));
        assertNotEquiv(oldSearchRequest, searchRequest);
        assertNotEquiv(searchRequest, oldSearchRequest);
    }

    @Test
    public void testComparisonOfSearchRequestsLegacyEqualOneWhereClause() throws Exception
    {
        mockController.replay();
        MockJqlSearchRequest oldSearchRequest = createSR(1L, null);
        MockJqlSearchRequest searchRequest = createSR(2L, JqlQueryBuilder.newBuilder().where().project(1234L).buildQuery());
        assertNotEquiv(oldSearchRequest, searchRequest);
        assertNotEquiv(searchRequest, oldSearchRequest);
    }

    @Test
    public void testComparisonOfSearchRequestsBothNullWhereClause() throws Exception
    {
        mockController.replay();
        MockJqlSearchRequest oldSearchRequest = createSR(1L, new QueryImpl());
        MockJqlSearchRequest searchRequest = createSR(2L, new QueryImpl());
        assertEquiv(oldSearchRequest, searchRequest);
        assertEquiv(searchRequest, oldSearchRequest);
    }

    @Test
    public void testComparisonOfSearchRequestsOneNullWhereClause() throws Exception
    {
        mockController.replay();
        MockJqlSearchRequest oldSearchRequest = createSR(1L, new QueryImpl());
        MockJqlSearchRequest searchRequest = createSR(2L, new QueryImpl(new TerminalClauseImpl("f", Operator.EQUALS, "v")));
        assertNotEquiv(oldSearchRequest, searchRequest);
        assertNotEquiv(searchRequest, oldSearchRequest);
    }

    @Test
    public void testGetClausesFromSearchersHappyPath() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchInputTransformer transformer = mockController.getMock(SearchInputTransformer.class);
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        transformer.populateFromParams(null, valuesHolder, actionParams);
        transformer.getSearchClause(null, valuesHolder);
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", "test");
        mockController.setReturnValue(clause);

        final IssueSearcher issueSearcher = mockController.getMock(IssueSearcher.class);
        issueSearcher.getSearchInputTransformer();
        mockController.setReturnValue(transformer);

        issueSearcherManager.getAllSearchers();
        mockController.setReturnValue(Collections.singleton(issueSearcher));

        mockController.replay();

        final List<Clause> clauses = searchRequestFactory.getClausesFromSearchers(null, actionParams);

        assertTrue(clauses.contains(clause));

        mockController.verify();
    }

    @Test
    public void testGetClausesFromSearchersNoClauseGenerated() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchInputTransformer transformer = mockController.getMock(SearchInputTransformer.class);
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        transformer.populateFromParams(null, valuesHolder, actionParams);
        transformer.getSearchClause(null, valuesHolder);
        mockController.setReturnValue(null);

        final IssueSearcher issueSearcher = mockController.getMock(IssueSearcher.class);
        issueSearcher.getSearchInputTransformer();
        mockController.setReturnValue(transformer);

        issueSearcherManager.getAllSearchers();
        mockController.setReturnValue(Collections.singleton(issueSearcher));

        mockController.replay();

        final List<Clause> clauses = searchRequestFactory.getClausesFromSearchers(null, actionParams);

        assertTrue(clauses.isEmpty());

        mockController.verify();
    }

    @Test
    public void testGetClauseOneClause() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", "test");
        mockController.replay();

        final Clause generatedClause = searchRequestFactory.getClause(Collections.<Clause>singletonList(clause));
        assertEquals(clause, generatedClause);

        mockController.verify();
    }

    @Test
    public void testGetClauseMultipleClauses() throws Exception
    {
        final TerminalClauseImpl clause1 = new TerminalClauseImpl("test1", "test");
        final TerminalClauseImpl clause2 = new TerminalClauseImpl("test1", "test");
        mockController.replay();

        final Clause generatedClause = searchRequestFactory.getClause(CollectionBuilder.<Clause>newBuilder(clause1, clause2).asList());
        Clause expectedClause = new AndClause(clause1, clause2);
        assertEquals(expectedClause, generatedClause);

        mockController.verify();
    }

    @Test
    public void testGetClauseNoClauses() throws Exception
    {
        mockController.replay();

        final Clause generatedClause = searchRequestFactory.getClause(Collections.<Clause>emptyList());
        assertNull(generatedClause);

        mockController.verify();
    }

    @Test
    public void testCheckWhereClauses() throws Exception
    {
        final TerminalClauseImpl clause1 = new TerminalClauseImpl("test1", "test");
        mockController.replay();
        assertTrue(searchRequestFactory.checkWhereClauses(null, null));
        assertFalse(searchRequestFactory.checkWhereClauses(null, clause1));
        assertTrue(searchRequestFactory.checkWhereClauses(clause1, clause1));
        mockController.verify();
    }

    @Test
    public void testCheckOrderByClauses() throws Exception
    {
        mockController.replay();
        assertTrue(searchRequestFactory.checkOrderByClauses(null, null));
        assertFalse(searchRequestFactory.checkOrderByClauses(null, new OrderByImpl()));
        assertTrue(searchRequestFactory.checkOrderByClauses(new OrderByImpl(), new OrderByImpl()));
        mockController.verify();
    }

    @Test
    public void testComparisonOfSearchRequestsLegacyEqualQueriesEquiv() throws Exception
    {
        mockController.replay();
        MockJqlSearchRequest oldSearchRequest = createSR(1L, new QueryImpl(new TerminalClauseImpl("f", Operator.EQUALS, "v")));
        MockJqlSearchRequest searchRequest = createSR(2L, new QueryImpl(new TerminalClauseImpl("f", Operator.EQUALS, "v")));
        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(null, null, searchService)
        {
            @Override
            boolean checkClauseEquivalence(final Clause oldClause, final Clause clause)
            {
                return true;
            }
        };
        assertTrue(factory.searchRequestsSameOrQueriesEquivalent(oldSearchRequest, searchRequest));
    }

    @Test
    public void testComparisonOfSearchRequestsLegacyEqualQueriesNotEquiv() throws Exception
    {
        mockController.replay();
        MockJqlSearchRequest oldSearchRequest = createSR(1L, new QueryImpl(new TerminalClauseImpl("f", Operator.EQUALS, "v")));
        MockJqlSearchRequest searchRequest = createSR(2L, new QueryImpl(new TerminalClauseImpl("f", Operator.EQUALS, "v")));
        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(null, null, searchService)
        {
            @Override
            boolean checkClauseEquivalence(final Clause oldClause, final Clause clause)
            {
                return false;
            }
        };
        assertFalse(factory.searchRequestsSameOrQueriesEquivalent(oldSearchRequest, searchRequest));
    }

    @Test
    public void testCombineSortsOnlyParamsSorts() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchSort sortFromParams = new SearchSort("fieldFromParams");

        final Query oldQuery = new QueryImpl(null);
        final SearchRequest oldSR = new SearchRequest(oldQuery);

        final OrderByImpl orderByFromParams = new OrderByImpl(sortFromParams);

        final List<SearchSort> list1 = CollectionBuilder.newBuilder(sortFromParams).asList();

        EasyMock.expect(searchSortUtil.getOrderByClause(actionParams.getKeysAndValues()))
                .andReturn(orderByFromParams);

        mockController.replay();

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService);

        final List<SearchSort> result = defaultSearchRequestFactory.combineSorts(oldSR, null, actionParams);

        assertEquals(list1, result);

        mockController.verify();
    }

    @Test
    public void testCombineSortsOnlyOldSorts() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchSort sortFromOldSr = new SearchSort("fieldFromOldSr");

        final Query oldQuery = new QueryImpl(null, new OrderByImpl(sortFromOldSr), "");
        final SearchRequest oldSR = new SearchRequest(oldQuery);

        final OrderByImpl orderByFromParams = new OrderByImpl();

        final List<SearchSort> list1 = CollectionBuilder.newBuilder(sortFromOldSr).asList();

        EasyMock.expect(searchSortUtil.getOrderByClause(actionParams.getKeysAndValues()))
                .andReturn(orderByFromParams);

        EasyMock.expect(searchSortUtil.mergeSearchSorts((com.atlassian.crowd.embedded.api.User) null, Collections.<SearchSort>emptyList(), Collections.singletonList(sortFromOldSr), Integer.MAX_VALUE))
                .andReturn(list1);

        mockController.replay();

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService);

        final List<SearchSort> result = defaultSearchRequestFactory.combineSorts(oldSR, null, actionParams);

        assertEquals(list1, result);

        mockController.verify();
    }

    @Test
    public void testCombineSortsNoJqlSortsDropDuplicates() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchSort sortFromOldSr = new SearchSort("fieldFromOldSr");
        final SearchSort sortFromParams = new SearchSort("fieldFromParams");

        final Query oldQuery = new QueryImpl(null, new OrderByImpl(sortFromOldSr), "");
        final SearchRequest oldSR = new SearchRequest(oldQuery);

        final OrderByImpl orderByFromParams = new OrderByImpl(sortFromParams);

        final List<SearchSort> list1 = CollectionBuilder.newBuilder(sortFromParams, sortFromOldSr).asList();

        EasyMock.expect(searchSortUtil.getOrderByClause(actionParams.getKeysAndValues()))
                .andReturn(orderByFromParams);

        EasyMock.expect(searchSortUtil.mergeSearchSorts((com.atlassian.crowd.embedded.api.User) null, Collections.singletonList(sortFromParams), Collections.singletonList(sortFromOldSr), Integer.MAX_VALUE))
                .andReturn(list1);

        mockController.replay();

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService);

        final List<SearchSort> result = defaultSearchRequestFactory.combineSorts(oldSR, null, actionParams);

        assertEquals(list1, result);

        mockController.verify();
    }

    @Test
    public void testCombineSortsNoActionParamSorts() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchSort sortFromOldSr = new SearchSort("fieldFromOldSr");

        final Query oldQuery = new QueryImpl(null, new OrderByImpl(sortFromOldSr), "");
        final SearchRequest oldSR = new SearchRequest(oldQuery);

        final OrderByImpl orderByFromParams = new OrderByImpl();

        final List<SearchSort> list1 = CollectionBuilder.newBuilder(sortFromOldSr).asList();
        final List<SearchSort> list2 = CollectionBuilder.newBuilder(sortFromOldSr).asList();

        EasyMock.expect(searchSortUtil.getOrderByClause(actionParams.getKeysAndValues()))
                .andReturn(orderByFromParams);

        EasyMock.expect(searchSortUtil.mergeSearchSorts((com.atlassian.crowd.embedded.api.User) null, Collections.<SearchSort>emptyList(), Collections.singletonList(sortFromOldSr), Integer.MAX_VALUE))
                .andReturn(list1);

        mockController.replay();

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService);

        final List<SearchSort> result = defaultSearchRequestFactory.combineSorts(oldSR, null, actionParams);

        assertEquals(list2, result);

        mockController.verify();
    }

    @Test
    public void testCombineSortsNoOldSearchSorts() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchSort sortFromParams = new SearchSort("fieldFromParams");

        final Query oldQuery = new QueryImpl(null);
        final SearchRequest oldSR = new SearchRequest(oldQuery);

        final OrderByImpl orderByFromParams = new OrderByImpl(sortFromParams);

        final List<SearchSort> list1 = CollectionBuilder.newBuilder(sortFromParams).asList();

        EasyMock.expect(searchSortUtil.getOrderByClause(actionParams.getKeysAndValues()))
                .andReturn(orderByFromParams);

        mockController.replay();

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService);

        final List<SearchSort> result = defaultSearchRequestFactory.combineSorts(oldSR, null, actionParams);

        assertEquals(list1, result);

        mockController.verify();
    }

    @Test
    public void testCreateFromParametersNullActionParams() throws Exception
    {
        ActionParams actionParams = null;

        final SearchRequest oldSR = mockController.getMock(SearchRequest.class);
        mockController.replay();

        try
        {
            searchRequestFactory.createFromParameters(oldSR, null, actionParams);
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }

        mockController.verify();
    }

    @Test
    public void testCreateFromParametersNotModified() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchRequest newSR = mockController.getMock(SearchRequest.class);
        final SearchRequest oldSR = mockController.getMock(SearchRequest.class);
        final Clause clause = mockController.getMock(Clause.class);

        final QueryImpl query = new QueryImpl(clause, new OrderByImpl(Collections.<SearchSort>emptyList()), null);
        newSR.setQuery(query);

        expect(oldSR.getQuery()).andReturn(query);
        newSR.setQuery(query);
        expect(oldSR.isModified()).andReturn(false);
        newSR.setModified(false);

        mockController.replay();

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            List<Clause> getClausesFromSearchers(final User searchUser, final ActionParams actionParams)
            {
                return Collections.singletonList(clause);
            }

            @Override
            Clause getClause(final List<Clause> clauses)
            {
                return clause;
            }

            @Override
            List<SearchSort> combineSorts(final SearchRequest oldSearchRequest, final User searchUser, final ActionParams actionParameters)
            {
                return Collections.emptyList();
            }

            @Override
            SearchRequest createNewSearchRequest(final SearchRequest oldSearchRequest, final User searchUser)
            {
                return newSR;
            }

            @Override
            boolean simpleSearchRequestsSameOrQueriesEquivalent(final User user, final SearchRequest oldSearchRequest, final SearchRequest newSearchRequest)
            {
                return true;
            }
        };

        defaultSearchRequestFactory.createFromParameters(oldSR, null, actionParams);

        mockController.verify();
    }

    @Test
    public void testCreateFromParametersModified() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchRequest newSR = mockController.getMock(SearchRequest.class);
        final SearchRequest oldSR = mockController.getMock(SearchRequest.class);
        final Clause clause = mockController.getMock(Clause.class);

        final QueryImpl query = new QueryImpl(clause, new OrderByImpl(Collections.<SearchSort>emptyList()), null);
        newSR.setQuery(query);

        newSR.setModified(true);

        mockController.replay();

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            List<Clause> getClausesFromSearchers(final User searchUser, final ActionParams actionParams)
            {
                return Collections.singletonList(clause);
            }

            @Override
            Clause getClause(final List<Clause> clauses)
            {
                return clause;
            }

            @Override
            List<SearchSort> combineSorts(final SearchRequest oldSearchRequest, final User searchUser, final ActionParams actionParameters)
            {
                return Collections.emptyList();
            }

            @Override
            SearchRequest createNewSearchRequest(final SearchRequest oldSearchRequest, final User searchUser)
            {
                return newSR;
            }

            @Override
            boolean simpleSearchRequestsSameOrQueriesEquivalent(final User user, final SearchRequest oldSearchRequest, final SearchRequest newSearchRequest)
            {
                return false;
            }
        };

        defaultSearchRequestFactory.createFromParameters(oldSR, null, actionParams);

        mockController.verify();
    }
    
    @Test
    public void testCreateFromParametersAlreadyModified() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchRequest newSR = mockController.getMock(SearchRequest.class);
        final SearchRequest oldSR = mockController.getMock(SearchRequest.class);
        final Clause clause = mockController.getMock(Clause.class);

        final QueryImpl query = new QueryImpl(clause, new OrderByImpl(Collections.<SearchSort>emptyList()), null);
        newSR.setQuery(query);

        expect(oldSR.getQuery()).andReturn(query);
        newSR.setQuery(query);
        expect(oldSR.isModified()).andReturn(true);
        newSR.setModified(true);

        mockController.replay();

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            List<Clause> getClausesFromSearchers(final User searchUser, final ActionParams actionParams)
            {
                return Collections.singletonList(clause);
            }

            @Override
            Clause getClause(final List<Clause> clauses)
            {
                return clause;
            }

            @Override
            List<SearchSort> combineSorts(final SearchRequest oldSearchRequest, final User searchUser, final ActionParams actionParameters)
            {
                return Collections.emptyList();
            }

            @Override
            SearchRequest createNewSearchRequest(final SearchRequest oldSearchRequest, final User searchUser)
            {
                return newSR;
            }

            @Override
            boolean simpleSearchRequestsSameOrQueriesEquivalent(final User user, final SearchRequest oldSearchRequest, final SearchRequest newSearchRequest)
            {
                return true;
            }
        };

        defaultSearchRequestFactory.createFromParameters(oldSR, null, actionParams);

        mockController.verify();
    }

    @Test
    public void testCreateFromParametersNoOldSR() throws Exception
    {
        ActionParams actionParams = new ActionParamsImpl();

        final SearchRequest newSR = mockController.getMock(SearchRequest.class);
        final Clause clause = mockController.getMock(Clause.class);

        final QueryImpl query = new QueryImpl(clause, new OrderByImpl(Collections.<SearchSort>emptyList()), null);
        newSR.setQuery(query);

        newSR.setModified(false);

        mockController.replay();

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            List<Clause> getClausesFromSearchers(final User searchUser, final ActionParams actionParams)
            {
                return Collections.singletonList(clause);
            }

            @Override
            Clause getClause(final List<Clause> clauses)
            {
                return clause;
            }

            @Override
            List<SearchSort> combineSorts(final SearchRequest oldSearchRequest, final User searchUser, final ActionParams actionParameters)
            {
                return Collections.emptyList();
            }

            @Override
            SearchRequest createNewSearchRequest(final SearchRequest oldSearchRequest, final User searchUser)
            {
                return newSR;
            }

            @Override
            boolean simpleSearchRequestsSameOrQueriesEquivalent(final User user, final SearchRequest oldSearchRequest, final SearchRequest newSearchRequest)
            {
                return true;
            }
        };

        defaultSearchRequestFactory.createFromParameters(null, null, actionParams);

        mockController.verify();
    }


    @Test
    public void testCreateFromQuerySameOldModifiedQuery() throws Exception
    {
        final SearchRequest oldSearchRequest = EasyMock.createStrictMock(SearchRequest.class);
        EasyMock.expect(oldSearchRequest.isModified()).andReturn(true);
        EasyMock.replay(oldSearchRequest);

        Query query = new QueryImpl();

        final SearchRequest request = mockController.getMock(SearchRequest.class);
        request.setQuery(query);
        request.setModified(true);
        mockController.replay();

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(null, null, searchService)
        {
            @Override
            SearchRequest createNewSearchRequest(final SearchRequest oldSearchRequest, final User searchUser)
            {
                return request;
            }

            @Override
            boolean searchRequestsSameOrQueriesEquivalent(final SearchRequest oldSearchRequest, final SearchRequest searchRequest)
            {
                return true;
            }
        };

        defaultSearchRequestFactory.createFromQuery(oldSearchRequest, null, query);

        mockController.verify();
        EasyMock.verify(oldSearchRequest);
    }

    @Test
    public void testCreateFromQuerySameOldNotModifiedQuery() throws Exception
    {
        final SearchRequest oldSearchRequest = EasyMock.createStrictMock(SearchRequest.class);
        EasyMock.expect(oldSearchRequest.isModified()).andReturn(false);
        EasyMock.replay(oldSearchRequest);

        Query query = new QueryImpl();

        final SearchRequest request = mockController.getMock(SearchRequest.class);
        request.setQuery(query);
        request.setModified(false);
        mockController.replay();

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(null, null, searchService)
        {
            @Override
            SearchRequest createNewSearchRequest(final SearchRequest oldSearchRequest, final User searchUser)
            {
                return request;
            }

            @Override
            boolean searchRequestsSameOrQueriesEquivalent(final SearchRequest oldSearchRequest, final SearchRequest searchRequest)
            {
                return true;
            }
        };

        defaultSearchRequestFactory.createFromQuery(oldSearchRequest, null, query);

        mockController.verify();
        EasyMock.verify(oldSearchRequest);
    }

    @Test
    public void testCreateFromQueryNullOldQuery() throws Exception
    {
        Query query = new QueryImpl();

        final SearchRequest request = mockController.getMock(SearchRequest.class);
        request.setQuery(query);
        request.setModified(false);
        mockController.replay();

        DefaultSearchRequestFactory defaultSearchRequestFactory = new DefaultSearchRequestFactory(null, null, searchService)
        {
            @Override
            SearchRequest createNewSearchRequest(final SearchRequest oldSearchRequest, final User searchUser)
            {
                return request;
            }
        };

        defaultSearchRequestFactory.createFromQuery(null, null, query);

        mockController.verify();
    }

    @Test
    public void testSimpleSearchRequestSameOrQueriesEquuivalentAttributesNotEqual() throws Exception
    {
        final SearchRequest oldSR = mockController.getMock(SearchRequest.class);
        final SearchRequest newSR = mockController.getMock(SearchRequest.class);

        final AtomicBoolean attribuesEqualCalled = new AtomicBoolean(false);

        mockController.replay();
        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            boolean nonQueryAttributesEquals(final SearchRequest searchRequest, final SearchRequest otherSearchRequest)
            {
                attribuesEqualCalled.set(true);
                return false;
            }
        };

        assertFalse(factory.simpleSearchRequestsSameOrQueriesEquivalent(null, oldSR, newSR));
        assertTrue(attribuesEqualCalled.get());
        mockController.verify();
    }

    @Test
    public void testSimpleSearchRequestSameOrQueriesOrderByClausesNotEqual() throws Exception
    {
        final SearchRequest oldSR = mockController.getMock(SearchRequest.class);
        final SearchRequest newSR = mockController.getMock(SearchRequest.class);

       expect(oldSR.getQuery()).andReturn(new QueryImpl());
       expect(newSR.getQuery()).andReturn(new QueryImpl());

        final AtomicBoolean attribuesEqualCalled = new AtomicBoolean(false);
        final AtomicBoolean checkOrderByClausesCalled = new AtomicBoolean(false);

        mockController.replay();
        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            boolean nonQueryAttributesEquals(final SearchRequest searchRequest, final SearchRequest otherSearchRequest)
            {
                attribuesEqualCalled.set(true);
                return true;
            }

            @Override
            boolean checkOrderByClauses(final OrderBy oldOrderByClause, final OrderBy newOrderByClause)
            {
                checkOrderByClausesCalled.set(true);
                return false;
            }
        };

        assertFalse(factory.simpleSearchRequestsSameOrQueriesEquivalent(null, oldSR, newSR));
        assertTrue(attribuesEqualCalled.get());
        assertTrue(checkOrderByClausesCalled.get());
        mockController.verify();
    }

    @Test
    public void testSimpleSearchRequestSameOrQueriesQueryContextsNotEqual() throws Exception
    {
        final SearchRequest oldSR = EasyMock.createMock(SearchRequest.class);
        final SearchRequest newSR = EasyMock.createMock(SearchRequest.class);

        final QueryContext newQC = EasyMock.createMock(QueryContext.class);
        final QueryContext oldQC = EasyMock.createMock(QueryContext.class);

        final QueryImpl oldQuery = new QueryImpl();
        final QueryImpl newQuery = new QueryImpl();

        expect(oldSR.getQuery()).andReturn(oldQuery).anyTimes();
        expect(newSR.getQuery()).andReturn(newQuery).anyTimes();

        expect(searchService.getQueryContext((User) null, newQuery)).andReturn(newQC);
        expect(searchService.getQueryContext((User) null, oldQuery)).andReturn(oldQC);

        final AtomicBoolean attribuesEqualCalled = new AtomicBoolean(false);
        final AtomicBoolean checkOrderByClausesCalled = new AtomicBoolean(false);

        EasyMock.replay(newQC, oldQC, newSR, oldSR);
        mockController.replay();

        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            boolean nonQueryAttributesEquals(final SearchRequest searchRequest, final SearchRequest otherSearchRequest)
            {
                attribuesEqualCalled.set(true);
                return true;
            }

            @Override
            boolean checkOrderByClauses(final OrderBy oldOrderByClause, final OrderBy newOrderByClause)
            {
                checkOrderByClausesCalled.set(true);
                return true;
            }
        };

        assertFalse(factory.simpleSearchRequestsSameOrQueriesEquivalent(null, oldSR, newSR));
        assertTrue(attribuesEqualCalled.get());
        assertTrue(checkOrderByClausesCalled.get());

        mockController.verify();
        EasyMock.verify(newQC, oldQC, newSR, oldSR);
    }

    @Test
    public void testSimpleSearchRequestSameOrQueriesSearchContextsNotEqual() throws Exception
    {
        final SearchRequest oldSR = EasyMock.createMock(SearchRequest.class);
        final SearchRequest newSR = EasyMock.createMock(SearchRequest.class);

        final QueryContext queryContext = EasyMock.createMock(QueryContext.class);

        final SearchContext newSC = EasyMock.createMock(SearchContext.class);
        final SearchContext oldSC = EasyMock.createMock(SearchContext.class);

        final QueryImpl oldQuery = new QueryImpl();
        final QueryImpl newQuery = new QueryImpl();

        expect(oldSR.getQuery()).andReturn(oldQuery).anyTimes();
        expect(newSR.getQuery()).andReturn(newQuery).anyTimes();

        expect(searchService.getQueryContext((User) null, newQuery)).andReturn(queryContext);
        expect(searchService.getQueryContext((User) null, oldQuery)).andReturn(queryContext);

        expect(searchService.getSearchContext((User) null, newQuery)).andReturn(newSC);
        expect(searchService.getSearchContext((User) null, oldQuery)).andReturn(oldSC);

        final AtomicBoolean attribuesEqualCalled = new AtomicBoolean(false);
        final AtomicBoolean checkOrderByClausesCalled = new AtomicBoolean(false);

        EasyMock.replay(newSC, oldSC, newSR, oldSR, queryContext);
        mockController.replay();

        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            boolean nonQueryAttributesEquals(final SearchRequest searchRequest, final SearchRequest otherSearchRequest)
            {
                attribuesEqualCalled.set(true);
                return true;
            }

            @Override
            boolean checkOrderByClauses(final OrderBy oldOrderByClause, final OrderBy newOrderByClause)
            {
                checkOrderByClausesCalled.set(true);
                return true;
            }
        };

        assertFalse(factory.simpleSearchRequestsSameOrQueriesEquivalent(null, oldSR, newSR));
        assertTrue(attribuesEqualCalled.get());
        assertTrue(checkOrderByClausesCalled.get());

        mockController.verify();
        EasyMock.verify(newSC, oldSC, newSR, oldSR, queryContext);
    }

    @Test
    public void testSimpleSearchRequestSameOrQueriesClauseNotEqual() throws Exception
    {
        final FieldValuesHolder newHolder = new FieldValuesHolderImpl();
        final FieldValuesHolder oldHolder = new FieldValuesHolderImpl();

        final IssueSearcher issueSearcher = mockController.getMock(IssueSearcher.class);
        final SearchInputTransformer transformer = mockController.getMock(SearchInputTransformer.class);

        final QueryContext queryContext = EasyMock.createMock(QueryContext.class);

        final SearchRequest oldSR = EasyMock.createMock(SearchRequest.class);
        final SearchRequest newSR = EasyMock.createMock(SearchRequest.class);

        final SearchContext searchContext = EasyMock.createMock(SearchContext.class);

        final QueryImpl oldQuery = new QueryImpl();
        final QueryImpl newQuery = new QueryImpl();

        expect(oldSR.getQuery()).andReturn(oldQuery).anyTimes();
        expect(newSR.getQuery()).andReturn(newQuery).anyTimes();

        expect(searchService.getQueryContext((User) null, newQuery)).andReturn(queryContext);
        expect(searchService.getQueryContext((User) null, oldQuery)).andReturn(queryContext);

        expect(searchService.getSearchContext((User) null, newQuery)).andReturn(searchContext);
        expect(searchService.getSearchContext((User) null, oldQuery)).andReturn(searchContext);

        final AtomicBoolean attribuesEqualCalled = new AtomicBoolean(false);
        final AtomicBoolean checkOrderByClausesCalled = new AtomicBoolean(false);

        expect(issueSearcherManager.getAllSearchers()).andReturn(CollectionBuilder.<IssueSearcher<?>>newBuilder(issueSearcher).asList());
        expect(issueSearcher.getSearchInputTransformer()).andReturn(transformer).anyTimes();

        transformer.populateFromQuery(null, newHolder, newQuery, searchContext);
        transformer.populateFromQuery(null, oldHolder, newQuery, searchContext);

        EasyMock.replay(searchContext, newSR, oldSR);
        mockController.replay();

        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            boolean nonQueryAttributesEquals(final SearchRequest searchRequest, final SearchRequest otherSearchRequest)
            {
                attribuesEqualCalled.set(true);
                return true;
            }

            @Override
            boolean checkOrderByClauses(final OrderBy oldOrderByClause, final OrderBy newOrderByClause)
            {
                checkOrderByClausesCalled.set(true);
                return true;
            }

            @Override
            boolean holdersEqual(final FieldValuesHolder oldHolder, final FieldValuesHolder newHolder)
            {
                return false;
            }
        };

        assertFalse(factory.simpleSearchRequestsSameOrQueriesEquivalent(null, oldSR, newSR));
        assertTrue(attribuesEqualCalled.get());
        assertTrue(checkOrderByClausesCalled.get());

        mockController.verify();
        EasyMock.verify(searchContext, newSR, oldSR);
    }
    
    @Test
    public void testSimpleSearchRequestSameOrQueriesClauseEqual() throws Exception
    {
        final FieldValuesHolder newHolder = new FieldValuesHolderImpl();
        final FieldValuesHolder oldHolder = new FieldValuesHolderImpl();

        final IssueSearcher issueSearcher = mockController.getMock(IssueSearcher.class);
        final SearchInputTransformer transformer = mockController.getMock(SearchInputTransformer.class);

        final QueryContext queryContext = EasyMock.createMock(QueryContext.class);

        final SearchRequest oldSR = EasyMock.createMock(SearchRequest.class);
        final SearchRequest newSR = EasyMock.createMock(SearchRequest.class);

        final SearchContext searchContext = EasyMock.createMock(SearchContext.class);

        final QueryImpl oldQuery = new QueryImpl();
        final QueryImpl newQuery = new QueryImpl();

        expect(oldSR.getQuery()).andReturn(oldQuery).anyTimes();
        expect(newSR.getQuery()).andReturn(newQuery).anyTimes();

        expect(searchService.getQueryContext((User) null, newQuery)).andReturn(queryContext);
        expect(searchService.getQueryContext((User) null, oldQuery)).andReturn(queryContext);

        expect(searchService.getSearchContext((User) null, newQuery)).andReturn(searchContext);
        expect(searchService.getSearchContext((User) null, oldQuery)).andReturn(searchContext);

        final AtomicBoolean attribuesEqualCalled = new AtomicBoolean(false);
        final AtomicBoolean checkOrderByClausesCalled = new AtomicBoolean(false);

        expect(issueSearcherManager.getAllSearchers()).andReturn(CollectionBuilder.<IssueSearcher<?>>newBuilder(issueSearcher).asList());
        expect(issueSearcher.getSearchInputTransformer()).andReturn(transformer).anyTimes();

        transformer.populateFromQuery(null, newHolder, newQuery, searchContext);
        transformer.populateFromQuery(null, oldHolder, newQuery, searchContext);

        EasyMock.replay(searchContext, newSR, oldSR);
        mockController.replay();

        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(issueSearcherManager, searchSortUtil, searchService)
        {
            @Override
            boolean nonQueryAttributesEquals(final SearchRequest searchRequest, final SearchRequest otherSearchRequest)
            {
                attribuesEqualCalled.set(true);
                return true;
            }

            @Override
            boolean checkOrderByClauses(final OrderBy oldOrderByClause, final OrderBy newOrderByClause)
            {
                checkOrderByClausesCalled.set(true);
                return true;
            }

            @Override
            boolean holdersEqual(final FieldValuesHolder oldHolder, final FieldValuesHolder newHolder)
            {
                return true;
            }
        };

        assertTrue(factory.simpleSearchRequestsSameOrQueriesEquivalent(null, oldSR, newSR));
        assertTrue(attribuesEqualCalled.get());
        assertTrue(checkOrderByClausesCalled.get());

        mockController.verify();
        EasyMock.verify(searchContext, newSR, oldSR);
    }


    private MockJqlSearchRequest createSR(Long id, Query query)
    {
        return new MockJqlSearchRequest(id, query);
    }

    private void assertNotEquiv(final SearchRequest oldSearchRequest, final SearchRequest searchRequest)
    {
        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(null, null, searchService);
        assertFalse(factory.searchRequestsSameOrQueriesEquivalent(oldSearchRequest, searchRequest));
    }

    private void assertEquiv(final MockJqlSearchRequest oldSearchRequest, final MockJqlSearchRequest searchRequest)
    {
        final DefaultSearchRequestFactory factory = new DefaultSearchRequestFactory(null, null, searchService);
        assertTrue(factory.searchRequestsSameOrQueriesEquivalent(oldSearchRequest, searchRequest));
    }
}
