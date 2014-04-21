package com.atlassian.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.PredicateOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.ChangeHistoryFieldIdResolver;
import com.atlassian.jira.jql.util.DateRange;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.query.history.AndHistoryPredicate;
import com.atlassian.query.history.HistoryPredicate;
import com.atlassian.query.history.TerminalHistoryPredicate;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.Lists;
import org.apache.lucene.search.BooleanQuery;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.assertTrue;

/**
 * *
 * @since v4.4
 */
public class TestHistoryPredicateQueryFactory
{

    private AndHistoryPredicate mockAndHistoryPredicate;
    private TerminalHistoryPredicate mockTerminalHistoryPredicate;
    private PredicateOperandResolver mockPredicateOperandResolver;
    private JqlDateSupport mockJqlDateSupport;
    private User mockSearcher;
    private HistoryPredicateQueryFactory historyPredicateQueryFactory;
    private ChangeHistoryFieldIdResolver mockChangeHistoryFieldIdResolver;


    @Before
    public void setupMocks()
    {
        mockAndHistoryPredicate = EasyMock.createMock(AndHistoryPredicate.class);
        mockTerminalHistoryPredicate = EasyMock.createMock(TerminalHistoryPredicate.class);
        mockPredicateOperandResolver = EasyMock.createMock(PredicateOperandResolver.class);
        mockJqlDateSupport = EasyMock.createMock(JqlDateSupport.class);
        mockSearcher = EasyMock.createMock(User.class);
        mockChangeHistoryFieldIdResolver = EasyMock.createMock(ChangeHistoryFieldIdResolver.class);
        historyPredicateQueryFactory = new HistoryPredicateQueryFactory(mockPredicateOperandResolver, mockJqlDateSupport, mockChangeHistoryFieldIdResolver);
    }


    @Test (expected = NullPointerException.class)
    public void testNullPredicates()
    {
        assertInvalidPredicateQuery(null);
    }

    @Test
    public void testNullAndHistoryPredicate()
    {
        expect(mockAndHistoryPredicate.getPredicates()).andStubReturn(Lists.<HistoryPredicate>newArrayList());
        replayMocks();
        assertInvalidPredicateQuery(mockAndHistoryPredicate);

    }

    @Test
    public void testNullTerminalHistoryPredicate()
    {
        expect(mockTerminalHistoryPredicate.getOperator()).andStubReturn(null);
        expect(mockTerminalHistoryPredicate.getOperand()).andStubReturn(null);
        expect(mockPredicateOperandResolver.getValues(mockSearcher, "field", null)).andStubReturn(null);
        replayMocks();
        assertInvalidPredicateQuery(mockTerminalHistoryPredicate);
    }

    @Test
    public void testValidTerminalPredicate()
    {
        setupTerminalPredicate();
        replayMocks();
        BooleanQuery query = historyPredicateQueryFactory.makePredicateQuery(mockSearcher, "field", mockTerminalHistoryPredicate, false);
        assertTrue("Query should have 2 clauses", query.clauses().size() == 2);
    }

    @Test
    public void testValidAndPredicate()
    {
        setupTerminalPredicate();
        expect(mockAndHistoryPredicate.getPredicates()).andStubReturn(Lists.<HistoryPredicate>newArrayList(mockTerminalHistoryPredicate));
        replayMocks();
        BooleanQuery query = historyPredicateQueryFactory.makePredicateQuery(mockSearcher, "field",  mockTerminalHistoryPredicate, false);
        assertTrue("Query should have 2 clauses", query.clauses().size() == 2);
    }

    private void setupTerminalPredicate()
    {
        Date now = new Date();
        DateRange dateRange = new DateRange(now,now);
        Operand operand = new SingleValueOperand(now.getTime());
        expect(mockTerminalHistoryPredicate.getOperator()).andStubReturn(Operator.BEFORE);
        expect(mockTerminalHistoryPredicate.getOperand()).andStubReturn(operand);
        expect(mockPredicateOperandResolver.getValues(mockSearcher, "field", operand)).andStubReturn(Lists.newArrayList(new QueryLiteral(operand, now.getTime())));
        expect(mockJqlDateSupport.convertToDateRange(now.getTime())).andStubReturn(dateRange);
        expect(mockJqlDateSupport.getIndexedValue(EasyMock.isA(Date.class))).andStubReturn("1");
    }

    private void assertInvalidPredicateQuery(final HistoryPredicate historyPredicate)
    {
        BooleanQuery query = historyPredicateQueryFactory.makePredicateQuery(mockSearcher, "field", historyPredicate, false);
        assertTrue("Query should be empty", query.clauses().isEmpty());
    }

    private void replayMocks()
    {
        replay(mockAndHistoryPredicate, mockTerminalHistoryPredicate, mockPredicateOperandResolver, mockJqlDateSupport);
    }


}

