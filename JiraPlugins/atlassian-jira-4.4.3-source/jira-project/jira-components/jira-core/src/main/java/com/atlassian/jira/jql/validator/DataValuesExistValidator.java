package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.util.dbc.Assertions;
import com.opensymphony.user.User;

/**
 * A clause validator that can be used for multiple constant (priority, status, resolution) clause types that
 * uses the {@link com.atlassian.jira.jql.resolver.NameResolver} to determine if the value exists.
 *
 */
class DataValuesExistValidator extends ValuesExistValidator
{
    private final NameResolver nameResolver;

    DataValuesExistValidator(final JqlOperandResolver operandResolver, NameResolver nameResolver)
    {
        super(operandResolver);
        this.nameResolver = Assertions.notNull("nameResolver", nameResolver);
    }

    boolean stringValueExists(final User searcher, final String value)
    {
        return nameResolver.nameExists(value);
    }

    boolean longValueExist(final User searcher, final Long value)
    {
        return nameResolver.idExists(value);
    }
}