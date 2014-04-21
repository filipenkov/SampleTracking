package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple object that can determine if a query string has been provided AND that it is relevant across all the
 * text system fields AND which of those fields are relevant.
 *
 * @since v4.0
 */
public class DefaultQuerySearcherInputHelper implements QuerySearcherInputHelper
{
    private final String queryParameter;
    private final JqlOperandResolver operandResolver;

    public DefaultQuerySearcherInputHelper(final String queryParameter, JqlOperandResolver operandResolver)
    {
        this.queryParameter = Assertions.notNull("queryParameter", queryParameter);
        this.operandResolver = Assertions.notNull("operandResolver", operandResolver);
    }

    public Map<String, String> convertClause(final Clause clause, final User user)
    {
        if (clause == null)
        {
            return null;
        }

        final List<TerminalClause> queryClauses = validateClauseStructure(clause);
        if (queryClauses == null)
        {
            return null;
        }

        String descriptionQueryString = getValueForField(queryClauses, user, SystemSearchConstants.forDescription().getJqlClauseNames().getJqlFieldNames());
        String environmentQueryString = getValueForField(queryClauses, user, SystemSearchConstants.forEnvironment().getJqlClauseNames().getJqlFieldNames());
        String summaryQueryString = getValueForField(queryClauses, user, SystemSearchConstants.forSummary().getJqlClauseNames().getJqlFieldNames());
        String commentQueryString = getValueForField(queryClauses, user, SystemSearchConstants.forComments().getJqlClauseNames().getJqlFieldNames());

        final String queryString = queryStringAllSameIgnoreNull(descriptionQueryString, environmentQueryString, summaryQueryString, commentQueryString);

        // We only want to populate the field values holder if all the text fields have the same query string or
        // some have not been specified
        Map<String, String> fieldValuesHolder = null;
        if (queryString != null)
        {
            fieldValuesHolder = new LinkedHashMap<String, String>();

            fieldValuesHolder.put(queryParameter, queryString);
            if (descriptionQueryString != null)
            {
                fieldValuesHolder.put(SystemSearchConstants.forDescription().getUrlParameter(), "true");
            }
            if (environmentQueryString != null)
            {
                fieldValuesHolder.put(SystemSearchConstants.forEnvironment().getUrlParameter(), "true");
            }
            if (summaryQueryString != null)
            {
                fieldValuesHolder.put(SystemSearchConstants.forSummary().getUrlParameter(), "true");
            }
            if (commentQueryString != null)
            {
                fieldValuesHolder.put(SystemSearchConstants.forComments().getUrlParameter(), "true");
            }
        }
        return fieldValuesHolder;
    }

    /**
     * Checks the clause structure for validity, and returns the needed clauses from the tree if valid.
     * The clause must be fit for the Navigator, and any Query clauses that appear must all be under the same
     * OrClause.
     *
     * @param clause the clause to check
     * @return a list of clauses for the Query fields, or null if the clause was invalid or there were no
     * clauses in the tree
     */
    List<TerminalClause> validateClauseStructure(Clause clause)
    {
        final QueryNavigatorCollectorVisitor collector = new QueryNavigatorCollectorVisitor();
        clause.accept(collector);
        final List<TerminalClause> foundChildren = collector.getClauses();

        if (!collector.isValid() || foundChildren.isEmpty() || checkForDuplicates(foundChildren) || checkForEmpties(foundChildren))
        {
            return null;
        }

        return foundChildren;
    }

    private boolean checkForDuplicates(final List<TerminalClause> foundChildren)
    {
        Set<String> containsSet = new HashSet<String>();
        for (TerminalClause foundChild : foundChildren)
        {
            if (!containsSet.add(foundChild.getName()))
            {
                return true;
            }
        }
        return false;
    }

    private boolean checkForEmpties(final List<TerminalClause> foundChildren)
    {
        for (TerminalClause foundChild : foundChildren)
        {
            final Operand operand = foundChild.getOperand();
            if (operandResolver.isEmptyOperand(operand))
            {
                return true;
            }
        }
        return false;
    }

    private String getValueForField(final List<TerminalClause> terminalClauses, final User user, Set<String> jqlClauseNames)
    {
        TerminalClause theClause = null;
        for (TerminalClause terminalClause : terminalClauses)
        {
            if (jqlClauseNames.contains(terminalClause.getName()))
            {
                // if there was already a clause with the same name, then return null
                if (theClause != null)
                {
                    return null;
                }
                else
                {
                    theClause = terminalClause;
                }
            }
        }

        if (theClause != null)
        {
            final Operand operand = theClause.getOperand();
            final QueryLiteral rawValue = operandResolver.getSingleValue(user, operand, theClause);
            if (rawValue != null && !rawValue.isEmpty())
            {
                return rawValue.asString();
            }
        }
        return null;
    }

    private String queryStringAllSameIgnoreNull(final String descriptionQueryString, final String environmentQueryString, final String summaryQueryString, final String commentQueryString)
    {
        Set<String> queryStringSet = new HashSet<String>();
        if (descriptionQueryString != null)
        {
            queryStringSet.add(descriptionQueryString);
        }
        if (environmentQueryString != null)
        {
            queryStringSet.add(environmentQueryString);
        }
        if (summaryQueryString != null)
        {
            queryStringSet.add(summaryQueryString);
        }
        if (commentQueryString != null)
        {
            queryStringSet.add(commentQueryString);
        }
        return (queryStringSet.size() == 1) ? queryStringSet.iterator().next() : null;
    }
}
