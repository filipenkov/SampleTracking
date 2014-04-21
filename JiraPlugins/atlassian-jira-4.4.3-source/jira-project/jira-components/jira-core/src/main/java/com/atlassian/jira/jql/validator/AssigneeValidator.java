package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.UserResolver;

/**
 * A Validator for the Assignee field clauses
 *
 * @since v4.0
 */
public class AssigneeValidator extends AbstractUserValidator implements ClauseValidator
{
    public AssigneeValidator(UserResolver userResolver, JqlOperandResolver operandResolver)
    {
        super(userResolver, operandResolver);
    }
}