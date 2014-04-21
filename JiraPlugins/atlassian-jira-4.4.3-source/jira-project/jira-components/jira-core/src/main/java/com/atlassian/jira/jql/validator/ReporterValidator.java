package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.UserResolver;

/**
 * A Validator for the Reporter field clauses
 *
 * @since v4.0
 */
public class ReporterValidator extends AbstractUserValidator implements ClauseValidator
{
    public ReporterValidator(UserResolver userResolver, JqlOperandResolver operandResolver)
    {
        super(userResolver, operandResolver);
    }
}