package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;
import com.opensymphony.user.User;

import java.util.List;

/**
 * A clause validator that can be used for multiple constant (priority, status, resolution) clause types.
 *
 */
abstract class ValuesExistValidator
{
    private final JqlOperandResolver operandResolver;


    ValuesExistValidator(final JqlOperandResolver operandResolver)
    {
        this.operandResolver = Assertions.notNull("operandResolver", operandResolver);
    }

    MessageSet validate(final User searcher, TerminalClause terminalClause)
    {
        final Operand operand = terminalClause.getOperand();
        final String fieldName = terminalClause.getName();

        MessageSet messages = new MessageSetImpl();

        if (operandResolver.isValidOperand(operand))
        {
            // visit every query literal and determine lookup failures
            final List<QueryLiteral> rawValues = operandResolver.getValues(searcher, operand, terminalClause);
            for (QueryLiteral rawValue : rawValues)
            {
                if (rawValue.getStringValue() != null)
                {
                    if (!stringValueExists(searcher, rawValue.getStringValue()))
                    {
                        if (operandResolver.isFunctionOperand(rawValue.getSourceOperand()))
                        {
                            messages.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.no.value.for.name.from.function", rawValue.getSourceOperand().getName(), fieldName));
                        }
                        else
                        {
                            messages.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.no.value.for.name", fieldName, rawValue.getStringValue()));
                        }
                    }
                }
                else if (rawValue.getLongValue() != null)
                {
                    if (!longValueExist(searcher, rawValue.getLongValue()))
                    {
                        if (operandResolver.isFunctionOperand(rawValue.getSourceOperand()))
                        {
                            messages.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.no.value.for.name.from.function", rawValue.getSourceOperand().getName(), fieldName));
                        }
                        else
                        {
                            messages.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.no.value.for.id", fieldName, rawValue.getLongValue().toString()));
                        }
                    }
                }
            }
        }

        return messages;
    }

    abstract boolean stringValueExists(final User searcher, String value);

    abstract boolean longValueExist(final User searcher, Long value);

    ///CLOVER:OFF
    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }
    ///CLOVER:ON
}