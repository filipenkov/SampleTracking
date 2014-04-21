package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

/**
 * @since v4.0
 */
public class TestQueryNavigatorCollectorVisitor extends ListeningTestCase
{
    @Test
    public void testIsQuerySingleClause() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, "operand1");

        QueryNavigatorCollectorVisitor collector = new QueryNavigatorCollectorVisitor();
        terminalClause1.accept(collector);

        assertTrue(collector.isValid());
        assertEquals(1, collector.getClauses().size());
        assertTrue(collector.getClauses().contains(terminalClause1));
    }

    @Test
    public void testIsQueryNotLikeSingleClause() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.NOT_LIKE, "operand1");

        QueryNavigatorCollectorVisitor collector = new QueryNavigatorCollectorVisitor();
        terminalClause1.accept(collector);

        assertFalse(collector.isValid());
        assertEquals(1, collector.getClauses().size());
        assertTrue(collector.getClauses().contains(terminalClause1));
    }

    @Test
    public void testIsNonQuerySingleClause() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("field", Operator.LIKE, "operand1");

        QueryNavigatorCollectorVisitor collector = new QueryNavigatorCollectorVisitor();
        terminalClause1.accept(collector);

        assertTrue(collector.isValid());
        assertEquals(0, collector.getClauses().size());
    }

    @Test
    public void testIsQueryOrClauseOneLevelAllQueries() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, "operand2");
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, "operand3");
        TerminalClause terminalClause4 = new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, "operand4");

        OrClause orClause = new OrClause(terminalClause1, terminalClause2, terminalClause3, terminalClause4);

        QueryNavigatorCollectorVisitor collector = new QueryNavigatorCollectorVisitor();
        orClause.accept(collector);

        assertTrue(collector.isValid());
        assertEquals(4, collector.getClauses().size());
        assertTrue(collector.getClauses().contains(terminalClause1));
        assertTrue(collector.getClauses().contains(terminalClause2));
        assertTrue(collector.getClauses().contains(terminalClause3));
        assertTrue(collector.getClauses().contains(terminalClause4));
    }

    @Test
    public void testIsQueryOrClauseOneLevelAllQueriesOneNotLike() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, "operand2");
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, "operand3");
        TerminalClause terminalClause4 = new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.NOT_LIKE, "operand4");

        OrClause orClause = new OrClause(terminalClause1, terminalClause2, terminalClause3, terminalClause4);

        QueryNavigatorCollectorVisitor collector = new QueryNavigatorCollectorVisitor();
        orClause.accept(collector);

        assertFalse(collector.isValid());
        assertEquals(4, collector.getClauses().size());
        assertTrue(collector.getClauses().contains(terminalClause1));
        assertTrue(collector.getClauses().contains(terminalClause2));
        assertTrue(collector.getClauses().contains(terminalClause3));
        assertTrue(collector.getClauses().contains(terminalClause4));
    }

    @Test
    public void testIsQueryOrClauseOneLevelWrongClause() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, "operand2");
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, "operand3");
        TerminalClause terminalClause4 = new TerminalClauseImpl("fieldName", Operator.LIKE, "operand4");

        OrClause orClause = new OrClause(terminalClause1, terminalClause2, terminalClause3, terminalClause4);

        QueryNavigatorCollectorVisitor collector = new QueryNavigatorCollectorVisitor();
        orClause.accept(collector);

        assertFalse(collector.isValid());
        assertEquals(3, collector.getClauses().size());
        assertTrue(collector.getClauses().contains(terminalClause1));
        assertTrue(collector.getClauses().contains(terminalClause2));
        assertTrue(collector.getClauses().contains(terminalClause3));
    }

    @Test
    public void testIsQueryOrClauseTwoLevelSplitQueryClauses() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, "operand2");
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, "operand3");
        AndClause andClause = new AndClause(terminalClause2, terminalClause3);

        OrClause orClause = new OrClause(terminalClause1, andClause);

        QueryNavigatorCollectorVisitor collector = new QueryNavigatorCollectorVisitor();
        orClause.accept(collector);

        assertFalse(collector.isValid());
        assertEquals(3, collector.getClauses().size());
        assertTrue(collector.getClauses().contains(terminalClause1));
        assertTrue(collector.getClauses().contains(terminalClause2));
        assertTrue(collector.getClauses().contains(terminalClause3));
    }

    @Test
    public void testIsQueryOrClauseTwoLevelQueryInOnePlace() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("field", Operator.LIKE, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, "operand2");
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, "operand3");
        OrClause orClause = new OrClause(terminalClause2, terminalClause3);

        AndClause andClause = new AndClause(terminalClause1, orClause);

        QueryNavigatorCollectorVisitor collector = new QueryNavigatorCollectorVisitor();
        andClause.accept(collector);

        assertTrue(collector.isValid());
        assertEquals(2, collector.getClauses().size());
        assertTrue(collector.getClauses().contains(terminalClause2));
        assertTrue(collector.getClauses().contains(terminalClause3));
    }

    @Test
    public void testIsQueryOrClauseTwoLevelQueryInOnePlaceNotLike() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("field", Operator.LIKE, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, "operand2");
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.NOT_LIKE, "operand3");
        OrClause orClause = new OrClause(terminalClause2, terminalClause3);

        AndClause andClause = new AndClause(terminalClause1, orClause);

        QueryNavigatorCollectorVisitor collector = new QueryNavigatorCollectorVisitor();
        andClause.accept(collector);

        assertFalse(collector.isValid());
        assertEquals(2, collector.getClauses().size());
        assertTrue(collector.getClauses().contains(terminalClause2));
        assertTrue(collector.getClauses().contains(terminalClause3));
    }

    @Test
    public void testIsQueryOrClauseAllQueriesNotted() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, "operand2");
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, "operand3");
        TerminalClause terminalClause4 = new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, "operand4");
        OrClause orClause = new OrClause(terminalClause1, terminalClause2, terminalClause3, terminalClause4);
        NotClause notClause = new NotClause(orClause);

        QueryNavigatorCollectorVisitor collector = new QueryNavigatorCollectorVisitor();
        notClause.accept(collector);

        assertFalse(collector.isValid());
        assertEquals(4, collector.getClauses().size());
        assertTrue(collector.getClauses().contains(terminalClause1));
        assertTrue(collector.getClauses().contains(terminalClause2));
        assertTrue(collector.getClauses().contains(terminalClause3));
        assertTrue(collector.getClauses().contains(terminalClause4));
    }

    @Test
    public void testIsQueryOrClauseTwoGroupsOfQueries() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, "operand2");
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, "operand3");
        TerminalClause terminalClause4 = new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, "operand4");
        OrClause orClause1 = new OrClause(terminalClause1, terminalClause2);
        OrClause orClause2 = new OrClause(terminalClause3, terminalClause4);
        AndClause andClause = new AndClause(orClause1, orClause2);

        QueryNavigatorCollectorVisitor collector = new QueryNavigatorCollectorVisitor();
        andClause.accept(collector);

        assertFalse(collector.isValid());
        assertEquals(4, collector.getClauses().size());
        assertTrue(collector.getClauses().contains(terminalClause1));
        assertTrue(collector.getClauses().contains(terminalClause2));
        assertTrue(collector.getClauses().contains(terminalClause3));
        assertTrue(collector.getClauses().contains(terminalClause4));
    }

    @Test
    public void testIsQueryQueryOrWithAndQueryFirst() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, "operand1");
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, "operand1");
        OrClause orClause1 = new OrClause(terminalClause1, terminalClause2);
        AndClause andClause = new AndClause(terminalClause3, orClause1);

        QueryNavigatorCollectorVisitor collector = new QueryNavigatorCollectorVisitor();
        andClause.accept(collector);

        assertFalse(collector.isValid());
        assertEquals(3, collector.getClauses().size());
        assertTrue(collector.getClauses().contains(terminalClause1));
        assertTrue(collector.getClauses().contains(terminalClause2));
        assertTrue(collector.getClauses().contains(terminalClause3));
    }

    @Test
    public void testIsQueryQueryOrWithAndQuerySecond() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, "operand1");
        TerminalClause terminalClause2 = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, "operand1");
        TerminalClause terminalClause3 = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, "operand1");
        OrClause orClause1 = new OrClause(terminalClause1, terminalClause2);
        AndClause andClause = new AndClause(orClause1, terminalClause3);

        QueryNavigatorCollectorVisitor collector = new QueryNavigatorCollectorVisitor();
        andClause.accept(collector);

        assertFalse(collector.isValid());
        assertEquals(3, collector.getClauses().size());
        assertTrue(collector.getClauses().contains(terminalClause1));
        assertTrue(collector.getClauses().contains(terminalClause2));
        assertTrue(collector.getClauses().contains(terminalClause3));
    }
}
