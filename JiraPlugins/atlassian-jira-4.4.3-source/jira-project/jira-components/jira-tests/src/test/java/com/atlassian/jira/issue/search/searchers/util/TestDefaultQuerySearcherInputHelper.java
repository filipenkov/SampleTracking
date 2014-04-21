package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import java.util.List;
import java.util.Map;

/**
 * @since v4.0
 */
public class TestDefaultQuerySearcherInputHelper extends MockControllerTestCase
{
    private static final String SEARCHER_ID = "searcherId";

    @Test
    public void testConvertNullClause() throws Exception
    {
        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, mockController.getMock(JqlOperandResolver.class));
        mockController.replay();
        assertNull(helper.convertClause(null, null));
    }
    
    @Test
    public void testStructureCheckFails() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.GREATER_THAN_EQUALS, "input");

        JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final QuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver)
        {
            @Override
            List<TerminalClause> validateClauseStructure(final Clause clause)
            {
                return null;
            }
        };

        mockController.replay();

        assertNull(helper.convertClause(clause, null));
        mockController.verify();
    }

    @Test
    public void testConvertSingleClauseString() throws Exception
    {
        final SingleValueOperand value = new SingleValueOperand("value");
        TerminalClause terminalClause = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, value);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        operandResolver.isEmptyOperand(value);
        mockController.setReturnValue(false);
        operandResolver.getSingleValue((com.atlassian.crowd.embedded.api.User) null, value, terminalClause);
        mockController.setReturnValue(createLiteral("value"));
        mockController.replay();

        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver);

        final Map<String, String> result = helper.convertClause(terminalClause, null);
        assertEquals(2, result.keySet().size());
        assertTrue(result.containsKey(SEARCHER_ID));
        assertEquals("value", result.get(SEARCHER_ID));
        assertTrue(result.containsKey(IssueFieldConstants.SUMMARY));
        assertEquals("true", result.get(IssueFieldConstants.SUMMARY));
    }

    @Test
    public void testConvertSingleClauseLong() throws Exception
    {
        final SingleValueOperand value = new SingleValueOperand(12345L);
        TerminalClause terminalClause = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, value);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        operandResolver.isEmptyOperand(value);
        mockController.setReturnValue(false);
        operandResolver.getSingleValue((com.atlassian.crowd.embedded.api.User) null, value, terminalClause);
        mockController.setReturnValue(createLiteral(12345L));
        mockController.replay();

        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver);

        final Map<String, String> result = helper.convertClause(terminalClause, null);
        assertEquals(2, result.keySet().size());
        assertTrue(result.containsKey(SEARCHER_ID));
        assertEquals("12345", result.get(SEARCHER_ID));
        assertTrue(result.containsKey(IssueFieldConstants.SUMMARY));
        assertEquals("true", result.get(IssueFieldConstants.SUMMARY));
    }

    @Test
    public void testConvertSingleIrrelevantClause() throws Exception
    {
        final SingleValueOperand value = new SingleValueOperand("value");
        TerminalClause terminalClause = new TerminalClauseImpl("irrelevantField", Operator.LIKE, value);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver);

        final Map<String, String> result = helper.convertClause(terminalClause, null);
        assertNull(result);
    }

    @Test
    public void testConvertTwoClausesSameValueUnderAnd() throws Exception
    {
        final SingleValueOperand value = new SingleValueOperand("value");
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, value);
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, value);
        AndClause andClause = new AndClause(terminalClause1, terminalClause2);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver);
        assertNull(helper.convertClause(andClause, null));
    }

    @Test
    public void testConvertTwoClausesSameValueUnderOr() throws Exception
    {
        final SingleValueOperand value = new SingleValueOperand("value");
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, value);
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, value);
        OrClause orClause = new OrClause(terminalClause2, terminalClause1);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        operandResolver.isEmptyOperand(value);
        mockController.setReturnValue(false);
        operandResolver.isEmptyOperand(value);
        mockController.setReturnValue(false);
        operandResolver.getSingleValue((com.atlassian.crowd.embedded.api.User) null, value, terminalClause2);
        mockController.setReturnValue(createLiteral("value"));
        operandResolver.getSingleValue((com.atlassian.crowd.embedded.api.User) null, value, terminalClause1);
        mockController.setReturnValue(createLiteral("value"));
        mockController.replay();

        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver);

        final Map<String, String> result = helper.convertClause(orClause, null);
        assertEquals(3, result.keySet().size());
        assertTrue(result.containsKey(SEARCHER_ID));
        assertEquals("value", result.get(SEARCHER_ID));
        assertTrue(result.containsKey(IssueFieldConstants.SUMMARY));
        assertEquals("true", result.get(IssueFieldConstants.SUMMARY));
        assertTrue(result.containsKey(IssueFieldConstants.ENVIRONMENT));
        assertEquals("true", result.get(IssueFieldConstants.ENVIRONMENT));
    }

    @Test
    public void testConvertFourClausesSameValueUnderAnd() throws Exception
    {
        final SingleValueOperand value = new SingleValueOperand("value");
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, value);
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, value);
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, value);
        TerminalClause terminalClause4 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, value);
        AndClause andClause = new AndClause(terminalClause1, terminalClause2, terminalClause3, terminalClause4);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver);
        assertNull(helper.convertClause(andClause, null));
    }

    @Test
    public void testConvertFourClausesSameValueUnderOr() throws Exception
    {
        final SingleValueOperand value = new SingleValueOperand("value");
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, value);
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, value);
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, value);
        TerminalClause terminalClause4 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, value);
        OrClause orClause = new OrClause(terminalClause1, terminalClause2, terminalClause3, terminalClause4);

        final JqlOperandResolver operandResolver = mockController.getNiceMock(JqlOperandResolver.class);
        operandResolver.isEmptyOperand(value);
        mockController.setReturnValue(false);
        operandResolver.isEmptyOperand(value);
        mockController.setReturnValue(false);
        operandResolver.isEmptyOperand(value);
        mockController.setReturnValue(false);
        operandResolver.isEmptyOperand(value);
        mockController.setReturnValue(false);
        operandResolver.getSingleValue((com.atlassian.crowd.embedded.api.User) null, value, terminalClause1);
        mockController.setReturnValue(createLiteral("value"));
        operandResolver.getSingleValue((com.atlassian.crowd.embedded.api.User) null, value, terminalClause2);
        mockController.setReturnValue(createLiteral("value"));
        operandResolver.getSingleValue((com.atlassian.crowd.embedded.api.User) null, value, terminalClause3);
        mockController.setReturnValue(createLiteral("value"));
        operandResolver.getSingleValue((com.atlassian.crowd.embedded.api.User) null, value, terminalClause4);
        mockController.setReturnValue(createLiteral("value"));
        mockController.replay();

        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver);

        final Map<String, String> result = helper.convertClause(orClause, null);
        assertEquals(5, result.keySet().size());
        assertTrue(result.containsKey(SEARCHER_ID));
        assertEquals("value", result.get(SEARCHER_ID));
        assertTrue(result.containsKey(SystemSearchConstants.forSummary().getUrlParameter()));
        assertEquals("true", result.get(SystemSearchConstants.forSummary().getUrlParameter()));
        assertTrue(result.containsKey(SystemSearchConstants.forEnvironment().getUrlParameter()));
        assertEquals("true", result.get(SystemSearchConstants.forEnvironment().getUrlParameter()));
        assertTrue(result.containsKey(SystemSearchConstants.forComments().getUrlParameter()));
        assertEquals("true", result.get(SystemSearchConstants.forComments().getUrlParameter()));
        assertTrue(result.containsKey(SystemSearchConstants.forDescription().getUrlParameter()));
        assertEquals("true", result.get(SystemSearchConstants.forDescription().getUrlParameter()));
    }

    @Test
    public void testConvertFourClausesNotSameValuesUnderOr() throws Exception
    {
        final SingleValueOperand value = new SingleValueOperand("value");
        final SingleValueOperand fishyValue = new SingleValueOperand("fish");

        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, fishyValue);
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, value);
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, value);
        TerminalClause terminalClause4 = new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, value);
        OrClause orClause = new OrClause(terminalClause1, terminalClause2, terminalClause3, terminalClause4);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        operandResolver.isEmptyOperand(fishyValue);
        mockController.setReturnValue(false);
        operandResolver.isEmptyOperand(value);
        mockController.setReturnValue(false);
        operandResolver.isEmptyOperand(value);
        mockController.setReturnValue(false);
        operandResolver.isEmptyOperand(value);
        mockController.setReturnValue(false);
        operandResolver.getSingleValue((com.atlassian.crowd.embedded.api.User) null, fishyValue, terminalClause1);
        mockController.setReturnValue(createLiteral("fish"));
        operandResolver.getSingleValue((com.atlassian.crowd.embedded.api.User) null, value, terminalClause2);
        mockController.setReturnValue(createLiteral("value"));
        operandResolver.getSingleValue((com.atlassian.crowd.embedded.api.User) null, value, terminalClause3);
        mockController.setReturnValue(createLiteral("value"));
        operandResolver.getSingleValue((com.atlassian.crowd.embedded.api.User) null, value, terminalClause4);
        mockController.setReturnValue(createLiteral("value"));
        mockController.replay();

        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver);

        final Map<String, String> result = helper.convertClause(orClause, null);
        assertNull(result);
    }

    @Test
    public void testConvertFourClausesNotSameValuesUnderAnd() throws Exception
    {
        final SingleValueOperand value = new SingleValueOperand("value");
        TerminalClause terminalClause4 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, "fish");
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, value);
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, value);
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, value);
        AndClause andClause = new AndClause(terminalClause1, terminalClause2, terminalClause3, terminalClause4);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver);

        final Map<String, String> result = helper.convertClause(andClause, null);
        assertNull(result);
    }

    @Test
    public void testConvertFourClausesNotSameValuesUnderDifferentParents() throws Exception
    {
        final SingleValueOperand value = new SingleValueOperand("value");
        TerminalClause terminalClause4 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, "fish");
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, value);
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, value);
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, value);
        AndClause andClause = new AndClause(new OrClause(terminalClause1, terminalClause2), new OrClause(terminalClause3, terminalClause4));

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver);

        final Map<String, String> result = helper.convertClause(andClause, null);
        assertNull(result);
    }

    @Test
    public void testConvertFourClausesSameValuesUnderDifferentParents1() throws Exception
    {
        final SingleValueOperand value = new SingleValueOperand("value");
        TerminalClause terminalClause4 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, value);
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, value);
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, value);
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, value);
        AndClause andClause = new AndClause(new OrClause(terminalClause1, terminalClause2), new OrClause(terminalClause3, terminalClause4));

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver);

        final Map<String, String> result = helper.convertClause(andClause, null);
        assertNull(result);
    }

    @Test
    public void testConvertFourClausesSameValuesUnderDifferentParents2() throws Exception
    {
        final SingleValueOperand value = new SingleValueOperand("value");
        TerminalClause terminalClause4 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, value);
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, value);
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, value);
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, value);
        AndClause andClause = new AndClause(new OrClause(terminalClause1, terminalClause2), terminalClause3, terminalClause4);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver);

        final Map<String, String> result = helper.convertClause(andClause, null);
        assertNull(result);
    }

    @Test
    public void testConvertFourClausesSameValuesUnderTwoOrs() throws Exception
    {
        final SingleValueOperand value = new SingleValueOperand("value");
        TerminalClause terminalClause4 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, value);
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, value);
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, value);
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, value);
        OrClause queryOr = new OrClause(terminalClause1, terminalClause2, terminalClause3, terminalClause4);
        OrClause upperOr = new OrClause(queryOr, new TerminalClauseImpl("field", Operator.EQUALS, "value"));
        AndClause andClause = new AndClause(upperOr, new TerminalClauseImpl("field", Operator.EQUALS, "value"));

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver);

        final Map<String, String> result = helper.convertClause(andClause, null);
        assertNull(result);
    }

    @Test
    public void testConvertEmptyClause() throws Exception
    {
        final Operand value = new EmptyOperand();
        TerminalClause terminalClause = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, value);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        operandResolver.isEmptyOperand(value);
        mockController.setReturnValue(true);
        mockController.replay();

        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver);

        assertNull(helper.convertClause(terminalClause, null));
    }

    @Test
    public void testConvertEmptyLiteral() throws Exception
    {
        final Operand value = new FunctionOperand("generateEmpty");
        TerminalClause terminalClause = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, value);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        operandResolver.isEmptyOperand(value);
        mockController.setReturnValue(false);

        operandResolver.getSingleValue((com.atlassian.crowd.embedded.api.User) null, value, terminalClause);
        mockController.setReturnValue(new QueryLiteral());

        mockController.replay();

        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver);

        assertNull(helper.convertClause(terminalClause, null));
    }

    @Test
    public void testConvertOneEmptyClauseAndOneNonEmpty() throws Exception
    {
        final Operand value1 = new EmptyOperand();
        final Operand value2 = new SingleValueOperand("value");
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, value1);
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, value2);
        OrClause orClause = new OrClause(terminalClause1, terminalClause2);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        operandResolver.isEmptyOperand(value1);
        mockController.setReturnValue(true);
        mockController.replay();

        DefaultQuerySearcherInputHelper helper = new DefaultQuerySearcherInputHelper(SEARCHER_ID, operandResolver);

        assertNull(helper.convertClause(orClause, null));
    }

}
