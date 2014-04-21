package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A context factory for issue keys and id clauses. The project and issue type is taken from the issues. If the operator
 * is a negating operator then this returns a context with {@link com.atlassian.jira.jql.context.AllProjectsContext} and
 * {@link com.atlassian.jira.jql.context.AllIssueTypesContext}.
 *
 * @since v4.0
 */
public class IssueIdClauseContextFactory implements ClauseContextFactory
{
    private final JqlIssueSupport jqlIssueSupport;
    private final JqlOperandResolver jqlOperandResolver;
    private final boolean supportsRelational;

    IssueIdClauseContextFactory(final JqlIssueSupport jqlIssueSupport, final JqlOperandResolver jqlOperandResolver, final boolean supportsRelational)
    {
        this.jqlIssueSupport = jqlIssueSupport;
        this.jqlOperandResolver = jqlOperandResolver;
        this.supportsRelational = supportsRelational;
    }

    public ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        final Operator operator = terminalClause.getOperator();
        if (!handlesOperator(operator) || isNegationOperator(operator) || isEmptyOperator(operator))
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }
        else
        {
            final List<QueryLiteral> literals = jqlOperandResolver.getValues(searcher, terminalClause.getOperand(), terminalClause);
            if (literals == null || literals.isEmpty())
            {
                return ClauseContextImpl.createGlobalClauseContext();
            }

            final Set<ProjectIssueTypeContext> contexts = new HashSet<ProjectIssueTypeContext>();
            for (QueryLiteral literal : literals)
            {
                // if we have an empty literal, the Global context will not impact on any existing contexts, so do nothing
                if (!literal.isEmpty())
                {
                    if (isEqualsOperator(operator))
                    {
                        for (Issue issue : getIssues(searcher, literal))
                        {
                            contexts.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(issue.getProjectObject().getId()),
                                    new IssueTypeContextImpl(issue.getIssueTypeObject().getId())));
                        }
                    }
                    else if (isRelationalOperator(operator))
                    {
                        for (Issue issue : getIssues(searcher, literal))
                        {
                            contexts.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(issue.getProjectObject().getId()),
                                    AllIssueTypesContext.INSTANCE));
                        }
                    }
                }
            }
            return contexts.isEmpty() ? ClauseContextImpl.createGlobalClauseContext() : new ClauseContextImpl(contexts);
        }
    }

    private boolean isEmptyOperator(final Operator operator)
    {
        return OperatorClasses.EMPTY_ONLY_OPERATORS.contains(operator);
    }

    private boolean isNegationOperator(final Operator operator)
    {
        return OperatorClasses.NEGATIVE_EQUALITY_OPERATORS.contains(operator);
    }

    private boolean isRelationalOperator(final Operator operator)
    {
        return OperatorClasses.RELATIONAL_ONLY_OPERATORS.contains(operator);
    }

    private boolean isEqualsOperator(final Operator operator)
    {
        return operator == Operator.EQUALS || operator == Operator.IN;
    }

    private boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY.contains(operator)
                || (supportsRelational && OperatorClasses.RELATIONAL_ONLY_OPERATORS.contains(operator));
    }

    /**
     * @param searcher the user performing the search
     * @param literal the query literal; must not be null or the empty literal
     * @return a collection of issues represented by the literal which the user can see; never null.
     */
    private Collection<Issue> getIssues(User searcher, QueryLiteral literal)
    {
        notNull("literal", literal);
        if (literal.getStringValue() != null)
        {
            return jqlIssueSupport.getIssues(literal.getStringValue(), searcher);
        }
        else if (literal.getLongValue() != null)
        {
            final Issue issue = jqlIssueSupport.getIssue(literal.getLongValue(), searcher);
            if (issue != null)
            {
                return Collections.singleton(issue);
            }
            else
            {
                return Collections.emptySet();
            }
        }
        else
        {
            throw new IllegalStateException("Invalid query literal");
        }
    }

    @InjectableComponent
    public static class Factory
    {
        private final JqlIssueSupport issueSupport;
        private final JqlOperandResolver operandResolver;

        public Factory(final JqlIssueSupport issueSupport, final JqlOperandResolver operandResolver)
        {
            this.issueSupport = issueSupport;
            this.operandResolver = operandResolver;
        }

        public IssueIdClauseContextFactory create(boolean supportsRelational)
        {
            return new IssueIdClauseContextFactory(issueSupport, operandResolver, supportsRelational);
        }
    }
}
