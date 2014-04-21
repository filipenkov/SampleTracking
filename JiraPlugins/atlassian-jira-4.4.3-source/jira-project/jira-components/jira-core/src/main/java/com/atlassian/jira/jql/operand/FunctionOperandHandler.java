package com.atlassian.jira.jql.operand;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.JqlFunction;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Adapter to convert the plugin point {@link com.atlassian.jira.plugin.jql.function.JqlFunction} into
 * {@link com.atlassian.jira.jql.operand.OperandHandler}.
 *
 * @since v4.0
 */
public class FunctionOperandHandler implements OperandHandler<FunctionOperand>
{
    protected final JqlFunction jqlFunction;

    public FunctionOperandHandler(final JqlFunction jqlFunction)
    {
        this.jqlFunction = notNull("jqlFunction", jqlFunction);
    }

    public MessageSet validate(final User searcher, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        return jqlFunction.validate(OSUserConverter.convertToOSUser(searcher), operand, terminalClause);
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        return jqlFunction.getValues(queryCreationContext, operand, terminalClause);
    }

    public boolean isList()
    {
        return jqlFunction.isList();
    }

    public boolean isEmpty()
    {
        return false;
    }

    public boolean isFunction()
    {
        return true;
    }

    public JqlFunction getJqlFunction()
    {
        return jqlFunction;
    }
}
