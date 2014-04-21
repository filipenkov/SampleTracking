package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.jira.issue.search.searchers.impl.QuerySearcher;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;


/**
 * A visitor that collects the query clauses in a clause tree. It also checks if the structure of the Query clauses
 * will fit into the navigator, this means all the query clauses are under a single OR, or a single query clause
 * is under the root AND, or a single query clause is the root of the tree, and all operators must be the LIKE operator.
 *
 * @since 4.0.
 */
class QueryNavigatorCollectorVisitor extends SimpleNavigatorCollectorVisitor
{
    private boolean seenQueryClauses = false;
    private boolean inQueryOr = false;

    public QueryNavigatorCollectorVisitor()
    {
        super(QuerySearcher.QUERY_JQL_FIELD_NAMES);
    }

    public Void visit(final OrClause orClause)
    {
        boolean oldValidPath = validPath;

        if (!seenQueryClauses && isQueryOrClause(orClause))
        {
            seenQueryClauses = true;
            inQueryOr = true;
        }
        else
        {
            validPath = false;
        }

        for (Clause clause : orClause.getClauses())
        {
            clause.accept(this);
        }

        inQueryOr = false;
        validPath = oldValidPath;

        return null;
    }

    boolean isQueryOrClause(OrClause orClause)
    {
        for (Clause clause : orClause.getClauses())
        {
            if (!QuerySearcher.QUERY_JQL_FIELD_NAMES.contains(clause.getName()))
            {
                return false;
            }
        }
        return true;
    }

    public Void visit(final TerminalClause terminalClause)
    {
        super.visit(terminalClause);
        // Check if we have already seen query clauses and we are not in an Query Or
        if (QuerySearcher.QUERY_JQL_FIELD_NAMES.contains(terminalClause.getName()))
        {
            if ((seenQueryClauses && !inQueryOr) || terminalClause.getOperator() != Operator.LIKE)
            {
                valid = false;
            }
            else if (!seenQueryClauses && !inQueryOr)
            {
                seenQueryClauses = true;
            }
        }
        return null;
    }
}