package com.atlassian.jira.issue.search.searchers.util;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Set;
import java.util.HashSet;

/**
 * @since v4.0
 */
public class TestRecursiveClauseVisitor extends ListeningTestCase
{
    @Test
    public void testAndChildren() throws Exception
    {
        Clause child1 = new TerminalClauseImpl("blah1", Operator.EQUALS, "blah1");
        Clause child2 = new TerminalClauseImpl("blah2", Operator.EQUALS, "blah2");
        final AndClause andClause = new AndClause(child1, child2);

        Collector collector = new Collector();
        andClause.accept(collector);

        Set<Clause> expectedResult = CollectionBuilder.newBuilder(child1, andClause, child2).asSet();

        assertEquals(expectedResult, collector.clauses);
    }

    @Test
    public void testOrChildren() throws Exception
    {
        Clause child1 = new TerminalClauseImpl("blah1", Operator.EQUALS, "blah1");
        Clause child2 = new TerminalClauseImpl("blah2", Operator.EQUALS, "blah2");
        final OrClause orClause = new OrClause(child1, child2);

        Collector collector = new Collector();
        orClause.accept(collector);

        Set<Clause> expectedResult = CollectionBuilder.newBuilder(child1, orClause, child2).asSet();

        assertEquals(expectedResult, collector.clauses);
    }

    @Test
    public void testNotChildren() throws Exception
    {
        Clause child = new TerminalClauseImpl("blah1", Operator.EQUALS, "blah1");
        final NotClause notClause = new NotClause(child);

        Collector collector = new Collector();
        notClause.accept(collector);

        Set<Clause> expectedResult = CollectionBuilder.newBuilder(child, notClause).asSet();

        assertEquals(expectedResult, collector.clauses);
    }

    @Test
    public void testComplex() throws Exception
    {
        Clause child = new TerminalClauseImpl("blah", Operator.EQUALS, "blah");
        final NotClause notClause = new NotClause(child);

        Clause child2 = new TerminalClauseImpl("blah2", Operator.EQUALS, "blah2");
        final AndClause andClause = new AndClause(notClause, child2);

        Clause child3 = new TerminalClauseImpl("blah3", Operator.EQUALS, "blah3");
        Clause child4 = new TerminalClauseImpl("blah4", Operator.EQUALS, "blah4");
        final OrClause orClause = new OrClause(andClause, child3, child4);

        Collector collector = new Collector();
        orClause.accept(collector);

        Set<Clause> expectedResult = CollectionBuilder.newBuilder(child, notClause, child2, andClause, child3, child4, orClause).asSet();

        assertEquals(expectedResult, collector.clauses);
    }



    static class Collector extends RecursiveClauseVisitor
    {
        public Set<Clause> clauses = new HashSet<Clause>();

        @Override
        public Void visit(final AndClause andClause)
        {
            clauses.add(andClause);
            return super.visit(andClause);
        }

        @Override
        public Void visit(final TerminalClause clause)
        {
            clauses.add(clause);
            return super.visit(clause);
        }

        @Override
        public Void visit(final NotClause notClause)
        {
            clauses.add(notClause);
            return super.visit(notClause);
        }

        @Override
        public Void visit(final OrClause orClause)
        {
            clauses.add(orClause);
            return super.visit(orClause);
        }
    }
}
