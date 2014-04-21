package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Base class for issue type functions.
 *
 * @since v4.0
 */
public abstract class AbstractIssueTypeFunction extends AbstractJqlFunction
{
    private final SubTaskManager subTaskManager;

    protected AbstractIssueTypeFunction(final SubTaskManager subTaskManager)
    {
        this.subTaskManager = notNull("subTaskManager", subTaskManager);
    }

    public MessageSet validate(final User searcher, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        final MessageSet errors = validateNumberOfArgs(operand, 0);
        if (!errors.hasAnyErrors())
        {
            validateFunctionOperand(errors);
        }
        return errors;
    }

    public int getMinimumNumberOfExpectedArguments()
    {
        return 0;
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.ISSUE_TYPE;
    }

    private void validateFunctionOperand(final MessageSet errors)
    {
        if (!subTaskManager.isSubTasksEnabled())
        {
            errors.addErrorMessage(getI18n().getText("jira.jql.issue.type.function.invalid.if.sub.tasks.disabled", getFunctionName()));
        }
    }
}
