package com.atlassian.jira.jql.validator;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.jql.resolver.SavedFilterResolver;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;
import org.easymock.classextension.EasyMock;

import java.util.List;

/**
 * @since v4.0
 */
public class TestSavedFilterCycleDetector extends MockControllerTestCase
{
    private SavedFilterResolver savedFilterResolver;
    private JqlOperandResolver jqlOperandResolver;
    private SearchRequest searchRequest;
    private SavedFilterCycleDetector savedFilterCycleDetector;

    private static final String SEARCH_REQUEST_NAME = "My Search Request";
    private static final Long SEARCH_REQUEST_ID = 12345L;

    private User theUser = null;
    private boolean overrideSecurity = false;
    private QueryCreationContext queryCreationContext;

    @Before
    public void setUp() throws Exception
    {
        savedFilterResolver = mockController.getMock(SavedFilterResolver.class);
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        searchRequest = mockController.getMock(SearchRequest.class);
        searchRequest.getName();
        mockController.setDefaultReturnValue(SEARCH_REQUEST_NAME);
        searchRequest.getId();
        mockController.setDefaultReturnValue(SEARCH_REQUEST_ID);

        savedFilterCycleDetector = new SavedFilterCycleDetector(savedFilterResolver, jqlOperandResolver);
        queryCreationContext = new QueryCreationContextImpl(theUser, overrideSecurity);
    }

    @Test
    public void testContainsSavedFilterReferenceReferencesSelf() throws Exception
    {
        TerminalClause filterClause = new TerminalClauseImpl("filter", Operator.EQUALS, SEARCH_REQUEST_NAME);
        Query query = new QueryImpl(filterClause);
        searchRequest.getQuery();
        mockController.setReturnValue(query);

        final QueryLiteral queryLiteral = createLiteral(SEARCH_REQUEST_NAME);
        final List<QueryLiteral> queryLiterals = CollectionBuilder.newBuilder(queryLiteral).asList();
        jqlOperandResolver.getValues(queryCreationContext, filterClause.getOperand(), filterClause);
        mockController.setReturnValue(queryLiterals);

        savedFilterResolver.getSearchRequest(theUser, queryLiterals);
        mockController.setReturnValue(CollectionBuilder.newBuilder(searchRequest).asList());

        mockController.replay();

        assertTrue(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest, null));
        mockController.verify();
    }

    @Test
    public void testContainsSavedFilterReferenceReferencesSelfOverrideSecurity() throws Exception
    {
        overrideSecurity = true;
        queryCreationContext = new QueryCreationContextImpl(theUser, overrideSecurity);

        TerminalClause filterClause = new TerminalClauseImpl("filter", Operator.EQUALS, SEARCH_REQUEST_NAME);
        Query query = new QueryImpl(filterClause);
        searchRequest.getQuery();
        mockController.setReturnValue(query);

        final QueryLiteral queryLiteral = createLiteral(SEARCH_REQUEST_NAME);
        final List<QueryLiteral> queryLiterals = CollectionBuilder.newBuilder(queryLiteral).asList();
        jqlOperandResolver.getValues(queryCreationContext, filterClause.getOperand(), filterClause);
        mockController.setReturnValue(queryLiterals);

        savedFilterResolver.getSearchRequestOverrideSecurity(queryLiterals);
        mockController.setReturnValue(CollectionBuilder.newBuilder(searchRequest).asList());

        mockController.replay();

        assertTrue(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest, null));
        mockController.verify();
    }

    @Test
    public void testContainsSavedFilterReferenceReferencesThroughAnother() throws Exception
    {
        TerminalClause otherFilterClause = new TerminalClauseImpl("filter", Operator.EQUALS, SEARCH_REQUEST_NAME);

        final SearchRequest otherSearchRequest = EasyMock.createMock(SearchRequest.class);
        EasyMock.expect(otherSearchRequest.getName()).andReturn("Other Saved Filter").anyTimes();
        EasyMock.expect(otherSearchRequest.getId()).andReturn(54321L).anyTimes();
        EasyMock.expect(otherSearchRequest.getQuery()).andReturn(new QueryImpl(otherFilterClause));

        TerminalClause filterClause = new TerminalClauseImpl("filter", Operator.EQUALS, "Other Saved Filter");
        Query query = new QueryImpl(filterClause);
        searchRequest.getQuery();
        mockController.setReturnValue(query);

        final List<QueryLiteral> queryLiterals = CollectionBuilder.newBuilder(createLiteral("Other Saved Filter")).asList();
        final List<QueryLiteral> otherQueryLiterals = CollectionBuilder.newBuilder(createLiteral(SEARCH_REQUEST_NAME)).asList();
        jqlOperandResolver.getValues(queryCreationContext, filterClause.getOperand(), filterClause);
        mockController.setReturnValue(queryLiterals);
        jqlOperandResolver.getValues(queryCreationContext, otherFilterClause.getOperand(), otherFilterClause);
        mockController.setReturnValue(otherQueryLiterals);

        savedFilterResolver.getSearchRequest(theUser, queryLiterals);
        mockController.setReturnValue(CollectionBuilder.newBuilder(otherSearchRequest).asList());
        savedFilterResolver.getSearchRequest(theUser, otherQueryLiterals);
        mockController.setReturnValue(CollectionBuilder.newBuilder(searchRequest).asList());

        EasyMock.replay(otherSearchRequest);
        mockController.replay();

        assertTrue(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest, null));
        EasyMock.verify(otherSearchRequest);
        mockController.verify();
    }


    @Test
    public void testContainsSavedFilterReferenceReferencesThroughAnotherUseFilterID() throws Exception
    {
        final SearchRequest otherSearchRequest = EasyMock.createMock(SearchRequest.class);
        EasyMock.expect(otherSearchRequest.getName()).andReturn("Other Saved Filter").anyTimes();
        EasyMock.expect(otherSearchRequest.getId()).andReturn(54321L).anyTimes();

        TerminalClause filterClause = new TerminalClauseImpl("filter", Operator.EQUALS, "Other Saved Filter");
        Query query = new QueryImpl(filterClause);
        searchRequest.getQuery();
        mockController.setReturnValue(query);

        final List<QueryLiteral> queryLiterals = CollectionBuilder.newBuilder(createLiteral("Other Saved Filter")).asList();
        jqlOperandResolver.getValues(queryCreationContext, filterClause.getOperand(), filterClause);
        mockController.setReturnValue(queryLiterals);

        savedFilterResolver.getSearchRequest(theUser, queryLiterals);
        mockController.setReturnValue(CollectionBuilder.newBuilder(otherSearchRequest).asList());

        EasyMock.replay(otherSearchRequest);
        mockController.replay();

        assertTrue(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest, 54321L));
        EasyMock.verify(otherSearchRequest);
        mockController.verify();
    }

    @Test
    public void testNoCycle() throws Exception
    {
        TerminalClause anotherFilterClause = new TerminalClauseImpl("project", Operator.EQUALS, "my proj");

        final SearchRequest anotherSearchRequest = EasyMock.createMock(SearchRequest.class);
        EasyMock.expect(anotherSearchRequest.getName()).andReturn("Another Saved Filter").anyTimes();
        EasyMock.expect(anotherSearchRequest.getId()).andReturn(98765L).anyTimes();
        EasyMock.expect(anotherSearchRequest.getQuery()).andReturn(new QueryImpl(anotherFilterClause));

        TerminalClause otherFilterClause = new TerminalClauseImpl("filter", Operator.EQUALS, "Another Saved Filter");

        final SearchRequest otherSearchRequest = EasyMock.createMock(SearchRequest.class);
        EasyMock.expect(otherSearchRequest.getName()).andReturn("Other Saved Filter").anyTimes();
        EasyMock.expect(otherSearchRequest.getId()).andReturn(54321L).anyTimes();
        EasyMock.expect(otherSearchRequest.getQuery()).andReturn(new QueryImpl(otherFilterClause));

        TerminalClause filterClause = new TerminalClauseImpl("filter", Operator.EQUALS, "Other Saved Filter");
        Query query = new QueryImpl(filterClause);
        searchRequest.getQuery();
        mockController.setReturnValue(query);

        final List<QueryLiteral> queryLiterals = CollectionBuilder.newBuilder(createLiteral("Other Saved Filter")).asList();
        final List<QueryLiteral> otherQueryLiterals = CollectionBuilder.newBuilder(createLiteral("Another Saved Filter")).asList();
        jqlOperandResolver.getValues(queryCreationContext, filterClause.getOperand(), filterClause);
        mockController.setReturnValue(queryLiterals);
        jqlOperandResolver.getValues(queryCreationContext, otherFilterClause.getOperand(), otherFilterClause);
        mockController.setReturnValue(otherQueryLiterals);

        savedFilterResolver.getSearchRequest(theUser, queryLiterals);
        mockController.setReturnValue(CollectionBuilder.newBuilder(otherSearchRequest).asList());
        savedFilterResolver.getSearchRequest(theUser, otherQueryLiterals);
        mockController.setReturnValue(CollectionBuilder.newBuilder(anotherSearchRequest).asList());

        EasyMock.replay(otherSearchRequest, anotherSearchRequest);
        mockController.replay();

        assertFalse(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest, null));
        EasyMock.verify(otherSearchRequest, anotherSearchRequest);
        mockController.verify();
    }

    @Test
    public void testPassedANullSavedFilter() throws Exception
    {
        try
        {
            mockController.replay();
            savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, null, null);
            fail("should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testPassedAllSearchQuery() throws Exception
    {
        searchRequest.getQuery();
        mockController.setReturnValue(new QueryImpl());
        mockController.replay();
        assertFalse(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest, null));
    }

    @Test
    public void testPassedAllSearchQueryLetsGoDeeper() throws Exception
    {
        final SearchRequest otherSearcherRequest = EasyMock.createMock(SearchRequest.class);

        TerminalClause filterClause = new TerminalClauseImpl("filter", Operator.EQUALS, SEARCH_REQUEST_NAME);
        Query query = new QueryImpl(filterClause);
        searchRequest.getQuery();
        mockController.setReturnValue(query);

        final QueryLiteral queryLiteral = createLiteral(SEARCH_REQUEST_NAME);
        final List<QueryLiteral> queryLiterals = CollectionBuilder.newBuilder(queryLiteral).asList();
        jqlOperandResolver.getValues(queryCreationContext, filterClause.getOperand(), filterClause);
        mockController.setReturnValue(queryLiterals);

        savedFilterResolver.getSearchRequest(theUser, queryLiterals);
        mockController.setReturnValue(CollectionBuilder.newBuilder(otherSearcherRequest).asList());

        EasyMock.expect(otherSearcherRequest.getId()).andReturn(6L).anyTimes();
        EasyMock.expect(otherSearcherRequest.getQuery()).andReturn(new QueryImpl());

        EasyMock.replay(otherSearcherRequest);
        mockController.replay();

        assertFalse(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest, null));

        mockController.verify();
        EasyMock.verify(otherSearcherRequest);
    }
}
