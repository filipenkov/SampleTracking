package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.ProjectIndexInfoResolver;
import com.atlassian.jira.jql.resolver.SelectCustomFieldIndexInfoResolver;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.CaseFolding;
import com.atlassian.jira.util.NonInjectableComponent;
import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for producing clauses for the cascading select custom fields
 *
 * @since v4.0
 */
@NonInjectableComponent
public class SelectCustomFieldClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;

    public SelectCustomFieldClauseQueryFactory(final CustomField customField, final JqlSelectOptionsUtil jqlSelectOptionsUtil,
            final JqlOperandResolver operandResolver)
    {
        final SelectCustomFieldIndexInfoResolver selectIndexInfoResolver = new SelectCustomFieldIndexInfoResolver();
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        operatorFactories.add(new EqualityQueryFactory<CustomField>(selectIndexInfoResolver));
        delegateClauseQueryFactory = new GenericClauseQueryFactory(customField.getId(), operatorFactories, operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }
}
