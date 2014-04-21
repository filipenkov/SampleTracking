package com.atlassian.jira.dev.reference.plugin.jql;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.NotNull;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.google.common.collect.Iterables;
import com.opensymphony.user.User;

import java.util.Collections;
import java.util.List;

/**
 * Echoes the the string passed in as an argument.
 *
 * @since v4.4
 */
public class EchoJqlFunction extends AbstractJqlFunction
{
    public MessageSet validate(User searcher, @NotNull FunctionOperand operand, @NotNull TerminalClause terminalClause)
    {
        return validateNumberOfArgs(operand, 1);
    }

    public List<QueryLiteral> getValues(@NotNull QueryCreationContext queryCreationContext,
            @NotNull FunctionOperand operand, @NotNull TerminalClause terminalClause)
    {
        return Collections.singletonList(new QueryLiteral(operand, Iterables.get(operand.getArgs(), 0)));
    }

    public int getMinimumNumberOfExpectedArguments()
    {
        return 1;
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.TEXT;
    }
}
