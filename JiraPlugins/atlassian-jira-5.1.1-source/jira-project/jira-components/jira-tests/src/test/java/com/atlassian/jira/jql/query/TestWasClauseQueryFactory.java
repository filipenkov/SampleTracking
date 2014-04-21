package com.atlassian.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.changehistory.ChangeHistoryFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.index.ChangeHistoryFieldConfigurationManager;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.filters.IssueIdFilter;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.ChangeHistoryFieldIdResolver;
import com.atlassian.jira.jql.resolver.VersionResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.mock.MockConstantsManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.clause.WasClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.Lists;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.IndexSearcher;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertTrue;

/**
 * *
 *
 * @since v4.4
 */
public class TestWasClauseQueryFactory
{

    private SearchProviderFactory mockSearchProviderFactory;
    private MockJqlOperandResolver mockOperandResolver;
    private ChangeHistoryManager mockChangeHistoryManager;
    private ConstantsManager mockConstantsManager;
    private HistoryPredicateQueryFactory mockHistoryPredicateQueryFactory;
    private ChangeHistoryFieldConstants mockChangeHistoryFieldConstants;
    private IndexSearcher mockSearcher;
    private User mockUser;
    private ChangeHistoryFieldConfigurationManager mockChangeHistoryFieldConfigurationManager;
    private OperandHandler<FunctionOperand> mockOperandHandler;
    private VersionResolver mockVersionResolver;
    private ChangeHistoryFieldIdResolver mockChangeHistoryFieldIdResolver;

    @Before
    public void setupMocks()
    {
        mockSearchProviderFactory = EasyMock.createMock(SearchProviderFactory.class);
        mockOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        mockChangeHistoryManager = EasyMock.createMock(ChangeHistoryManager.class);
        mockChangeHistoryFieldConfigurationManager = EasyMock.createMock(ChangeHistoryFieldConfigurationManager.class);
        mockConstantsManager = new MockConstantsManager();
        mockSearcher = EasyMock.createMock(IndexSearcher.class);
        mockHistoryPredicateQueryFactory = EasyMock.createMock(HistoryPredicateQueryFactory.class);
        mockChangeHistoryFieldConstants = EasyMock.createMock(ChangeHistoryFieldConstants.class);
        mockOperandHandler = EasyMock.createMock(OperandHandler.class);
        mockChangeHistoryFieldIdResolver = EasyMock.createMock(ChangeHistoryFieldIdResolver.class);
        mockUser = new MockUser("Fred");
        mockVersionResolver = EasyMock.createMock(VersionResolver.class);
    }


    @Test
    public void testSupportedOperator() throws Exception
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand("testOperand");
        final QueryLiteral queryLiteral = new QueryLiteral(singleValueOperand, "testOperand");

        EasyMock.expect(mockChangeHistoryFieldConstants.getIdsForField("status", queryLiteral))
                .andStubReturn(Collections.singleton("1"));
        EasyMock.expect(mockSearchProviderFactory.getSearcher("changes")).andStubReturn(mockSearcher);
        EasyMock.expect(mockChangeHistoryFieldIdResolver.resolveIdsForField("status", queryLiteral, false)).andStubReturn(Collections.singleton("1"));
        replayAllMocks();
        WasClauseQueryFactory wasClauseQueryFactory = new WasClauseQueryFactory(mockSearchProviderFactory,
                mockOperandResolver, mockHistoryPredicateQueryFactory, null, mockChangeHistoryFieldIdResolver);

        WasClause wasClause = new WasClauseImpl("status", Operator.WAS, singleValueOperand, null);

        final QueryFactoryResult result = wasClauseQueryFactory.create(mockUser, wasClause);
        final ConstantScoreQuery query = (ConstantScoreQuery) result.getLuceneQuery();
        assertTrue("Query wraps an IssueIdFilter", query.getFilter() instanceof IssueIdFilter);
        verifyAllMocks();
    }

    @Test
    public void testSingleValueFunctionSearching()
    {
        final FunctionOperand functionOperand = new FunctionOperand("testOperand");
        final QueryLiteral queryLiteral = new QueryLiteral(functionOperand, "testValue");
        mockOperandResolver.addHandler("testOperand",mockOperandHandler);

        EasyMock.expect(mockChangeHistoryFieldConstants.getIdsForField("status", queryLiteral))
                        .andStubReturn(Collections.singleton("1"));
        EasyMock.expect(mockSearchProviderFactory.getSearcher("changes")).andStubReturn(mockSearcher);
        EasyMock.expect(mockChangeHistoryFieldIdResolver.resolveIdsForField("status", queryLiteral, false)).andStubReturn(Collections.singleton("1"));

        WasClauseQueryFactory wasClauseQueryFactory = new WasClauseQueryFactory(mockSearchProviderFactory,
                 mockOperandResolver, mockHistoryPredicateQueryFactory, null, mockChangeHistoryFieldIdResolver);

        WasClause wasClause = new WasClauseImpl("status", Operator.WAS, functionOperand, null);
        EasyMock.expect(mockOperandHandler.getValues(new QueryCreationContextImpl(mockUser),
                                                     functionOperand,
                                                     wasClause)).andStubReturn(Lists.newArrayList(queryLiteral));
        replayAllMocks();

        wasClauseQueryFactory.create(mockUser, wasClause);
        verifyAllMocks();
    }

    @Test
    public void testListFunctionSearching()
    {
        final FunctionOperand functionOperand = new FunctionOperand("testOperand");
        final QueryLiteral firstQueryLiteral = new QueryLiteral(functionOperand, "testValue1");
        final QueryLiteral secondQueryLiteral = new QueryLiteral(functionOperand, "testValue2");

        mockOperandResolver.addHandler("testOperand",mockOperandHandler);

        EasyMock.expect(mockChangeHistoryFieldConstants.getIdsForField("status", firstQueryLiteral))
                        .andStubReturn(Collections.singleton("1"));
        EasyMock.expect(mockChangeHistoryFieldConstants.getIdsForField("status", secondQueryLiteral))
                        .andStubReturn(Collections.singleton("2"));
        EasyMock.expect(mockSearchProviderFactory.getSearcher("changes")).andStubReturn(mockSearcher);
        EasyMock.expect(mockChangeHistoryFieldIdResolver.resolveIdsForField("status", firstQueryLiteral, false)).andStubReturn(Collections.singleton("1"));
        EasyMock.expect(mockChangeHistoryFieldIdResolver.resolveIdsForField("status", secondQueryLiteral, false)).andStubReturn(Collections.singleton("2"));

        WasClauseQueryFactory wasClauseQueryFactory = new WasClauseQueryFactory(mockSearchProviderFactory,
                 mockOperandResolver, mockHistoryPredicateQueryFactory, null, mockChangeHistoryFieldIdResolver);

        WasClause wasClause = new WasClauseImpl("status", Operator.WAS, functionOperand, null);
        EasyMock.expect(mockOperandHandler.getValues(new QueryCreationContextImpl(mockUser),
                                                     functionOperand,
                                                     wasClause)).andStubReturn(Lists.newArrayList(firstQueryLiteral, secondQueryLiteral));
        replayAllMocks();

        wasClauseQueryFactory.create(mockUser, wasClause);
        verifyAllMocks();
    }
    private void replayAllMocks()
    {
        EasyMock.replay(mockSearchProviderFactory, mockChangeHistoryManager, mockHistoryPredicateQueryFactory, mockOperandHandler, mockChangeHistoryFieldIdResolver);
    }

     private void verifyAllMocks()
    {
        EasyMock.verify(mockSearchProviderFactory, mockChangeHistoryManager, mockHistoryPredicateQueryFactory, mockOperandHandler, mockChangeHistoryFieldIdResolver);
    }
}
