package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.UserResolver;

/**
 * The Affected Version clause validator.
 *
 * @since v4.0
 */
public class UserCustomFieldValidator extends AbstractUserValidator
{
    ///CLOVER:OFF
    public UserCustomFieldValidator(final UserResolver userResolver, final JqlOperandResolver operandResolver)
    {
        super(userResolver, operandResolver);
    }
    ///CLOVER:ON
}
