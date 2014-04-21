package com.atlassian.jira.jql.validator;

import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.IssueConstantInfoResolver;
import com.atlassian.jira.jql.resolver.StatusResolver;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.opensymphony.user.User;

/**
 * A simple wrapper on the ConstantsClauseValidator.
 *
 * @since v4.0
 */
public class StatusValidator implements ClauseValidator
{
    private final RawValuesExistValidator rawValuesExistValidator;
    private final SupportedOperatorsValidator supportedOperatorsValidator;

    public StatusValidator(final StatusResolver statusResolver, final JqlOperandResolver operandResolver)
    {
        this.rawValuesExistValidator = getRawValuesValidator(statusResolver, operandResolver);
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        final MessageSet messageSet = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!messageSet.hasAnyErrors())
        {
            messageSet.addMessageSet(rawValuesExistValidator.validate(searcher, terminalClause));
        }
        return  messageSet;
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);
    }

    RawValuesExistValidator getRawValuesValidator(final StatusResolver statusResolver, final JqlOperandResolver operandResolver)
    {
        return new RawValuesExistValidator(operandResolver, new IssueConstantInfoResolver<Status>(statusResolver));
    }

}
