package com.atlassian.jira.jql.validator;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.IssueConstantInfoResolver;
import com.atlassian.jira.jql.resolver.IssueTypeResolver;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.opensymphony.user.User;

/**
 * A Validator for the Issue Types field clauses
 *
 * @since v4.0
 */
public class IssueTypeValidator implements ClauseValidator
{
    private final RawValuesExistValidator rawValuesExistValidator;
    private final SupportedOperatorsValidator supportedOperatorsValidator;

    public IssueTypeValidator(IssueTypeResolver issueTypeResolver, JqlOperandResolver operandResolver)
    {
        this.rawValuesExistValidator = getRawValuesValidator(issueTypeResolver, operandResolver);
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        MessageSet errors = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!errors.hasAnyErrors())
        {
            errors = rawValuesExistValidator.validate(searcher, terminalClause);
        }
        return errors;
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);
    }

    RawValuesExistValidator getRawValuesValidator(final IssueTypeResolver resolver, final JqlOperandResolver operandResolver)
    {
        return new RawValuesExistValidator(operandResolver, new IssueConstantInfoResolver<IssueType>(resolver));
    }
}
