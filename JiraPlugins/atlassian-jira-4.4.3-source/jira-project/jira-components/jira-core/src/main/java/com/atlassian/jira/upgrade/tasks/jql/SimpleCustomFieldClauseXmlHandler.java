package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import static com.atlassian.util.concurrent.Assertions.notNull;

/**
 * Generates a {@link com.atlassian.query.clause.TerminalClause} for simple custom field xml parameters.
 *
 * @since v4.0
 */
public class SimpleCustomFieldClauseXmlHandler extends AbstractCustomFieldClauseXmlHandler implements ClauseXmlHandler
{
    private final Operator operator;

    public SimpleCustomFieldClauseXmlHandler(String valueAttribbute, Operator operator)
    {
        super(valueAttribbute);
        this.operator = notNull("operator", operator);
    }

    Clause createClause(final String jqlFieldName, final String value)
    {
        return new TerminalClauseImpl(jqlFieldName, operator, value);
    }
}