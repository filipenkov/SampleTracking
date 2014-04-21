package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlDurationSupport;
import com.atlassian.jira.jql.util.JqlDurationSupportImpl;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;
import com.opensymphony.user.User;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Validates duration values e.g. "4h 30m"
 *
 * @since v4.0
 */
class PositiveDurationValueValidator
{
    private final JqlOperandResolver operandResolver;
    private final JqlDurationSupport jqlDurationSupport;

    PositiveDurationValueValidator(final JqlOperandResolver operandResolver)
    {
        this.operandResolver = notNull("operandResolver", operandResolver);
        this.jqlDurationSupport = new JqlDurationSupportImpl();
    }

    MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        notNull("terminalClause", terminalClause);

        final Operand operand = terminalClause.getOperand();
        final MessageSet messages = new MessageSetImpl();

        if (operandResolver.isValidOperand(operand))
        {
            final I18nHelper i18n = getI18n(searcher);
            final List<QueryLiteral> values = operandResolver.getValues(searcher, operand, terminalClause);
            final String fieldName = terminalClause.getName();

            for (QueryLiteral value : values)
            {
                // we are ok with positive longValues -- "minutes" is the implied scale
                boolean isValid = true;

                final Long longValue = value.getLongValue();
                if (longValue != null)
                {
                    isValid = longValue >= 0;
                }
                else
                {
                    final String str = value.getStringValue();
                    if (str != null)
                    {
                        isValid = jqlDurationSupport.validate(str, false);
                    }
                }

                if (!isValid)
                {
                    String msg;
                    if (operandResolver.isFunctionOperand(value.getSourceOperand()))
                    {
                        msg = i18n.getText("jira.jql.clause.positive.duration.format.invalid.from.func", value.getSourceOperand().getName(), fieldName);
                    }
                    else
                    {
                        msg = i18n.getText("jira.jql.clause.positive.duration.format.invalid", value.toString(), fieldName);
                    }
                    messages.addErrorMessage(msg);
                }
            }
        }
        return messages;
    }

    ///CLOVER:OFF
    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }
    ///CLOVER:ON
}
