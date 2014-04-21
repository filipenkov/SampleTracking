package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.plugin.jql.function.CurrentUserFunction;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;

/**
 * Generates a {@link com.atlassian.query.clause.TerminalClause} that represents a user custom field and some values
 * provided the "old-style" user custom field parameter XML.
 *
 * @since v4.0
 */
public class UserParameterCustomFieldClauseXmlHandler extends AbstractCustomFieldClauseXmlHandler implements ClauseXmlHandler
{
    public UserParameterCustomFieldClauseXmlHandler()
    {
        super("value");
    }

    Clause createClause(final String jqlFieldName, final String value)
    {
        if (DocumentConstants.ISSUE_CURRENT_USER.equals(value))
        {
            return new TerminalClauseImpl(jqlFieldName, Operator.EQUALS, new FunctionOperand(CurrentUserFunction.FUNCTION_CURRENT_USER));
        }
        else
        {
            return new TerminalClauseImpl(jqlFieldName, Operator.EQUALS, value);
        }
    }

    public boolean isSafeToNamifyValue()
    {
        return false;
    }
}