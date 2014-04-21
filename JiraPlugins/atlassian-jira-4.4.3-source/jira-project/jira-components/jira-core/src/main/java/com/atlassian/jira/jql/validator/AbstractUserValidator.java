package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.opensymphony.user.User;

/**
 * An abstract Validator for the User field clauses
 *
 * @since v4.0
 */
public abstract class AbstractUserValidator implements ClauseValidator
{
    private final DataValuesExistValidator dataValuesExistValidator;
    private final SupportedOperatorsValidator supportedOperatorsValidator;

    public AbstractUserValidator(UserResolver userResolver, JqlOperandResolver operandResolver)
    {
        this.dataValuesExistValidator = getDataValuesValidator(userResolver, operandResolver);
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        MessageSet errors = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!errors.hasAnyErrors())
        {
            errors = dataValuesExistValidator.validate(searcher, terminalClause);
        }
        return errors;
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);
    }

    DataValuesExistValidator getDataValuesValidator(final UserResolver resolver, final JqlOperandResolver operandResolver)
    {
        return new DataValuesExistValidator(operandResolver, resolver);
    }
}