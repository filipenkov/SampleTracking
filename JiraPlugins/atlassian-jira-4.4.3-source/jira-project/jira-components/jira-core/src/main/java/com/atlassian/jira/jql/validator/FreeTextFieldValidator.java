package com.atlassian.jira.jql.validator;

import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A generic validator for text fields
 *
 * @since v4.0
 */
public class FreeTextFieldValidator implements ClauseValidator
{
    public static final List<String> INVALID_FIRST_CHAR_LIST = ImmutableList.of("?", "*", "~", ":", ";", "!", "]", "[", "^", "{", "}", "(", ")");

    private static final Logger log = Logger.getLogger(FreeTextFieldValidator.class);

    private final String indexField;
    private final JqlOperandResolver operandResolver;

    public FreeTextFieldValidator(String indexField, JqlOperandResolver operandResolver)
    {
        this.indexField = notBlank("indexField", indexField);
        this.operandResolver = notNull("operandResolver", operandResolver);
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        final MessageSet messageSet = new MessageSetImpl();
        final I18nHelper i18n = getI18n(searcher);
        final Operator operator = terminalClause.getOperator();
        final String fieldName = terminalClause.getName();
        if (!handlesOperator(operator))
        {
            messageSet.addErrorMessage(i18n.getText("jira.jql.clause.does.not.support.operator", operator.getDisplayString(), fieldName));
            return messageSet;
        }

        final Operand operand = terminalClause.getOperand();

        final List<QueryLiteral> values = operandResolver.getValues(searcher, operand, terminalClause);
        if (values != null)
        {
            for (QueryLiteral literal : values)
            {
                // empty literals are always okay
                if (!literal.isEmpty())
                {
                    final String query = literal.asString();
                    if (StringUtils.isNotBlank(query))
                    {
                        String firstLetter = String.valueOf(query.charAt(0));
                        if (INVALID_FIRST_CHAR_LIST.contains(firstLetter))
                        {
                            if (operandResolver.isFunctionOperand(literal.getSourceOperand()))
                            {
                                messageSet.addErrorMessage(i18n.getText("jira.jql.text.clause.bad.start.function", literal.getSourceOperand().getName(), terminalClause.getName(), firstLetter));
                            }
                            else
                            {
                                messageSet.addErrorMessage(i18n.getText("jira.jql.text.clause.bad.start", query, terminalClause.getName(), firstLetter));
                            }
                        }
                        else
                        {
                            messageSet.addMessageSet(parseQuery(indexField, fieldName, query, i18n, literal.getSourceOperand()));
                        }
                    }
                    else
                    {
                        messageSet.addErrorMessage(i18n.getText("jira.jql.text.clause.does.not.support.empty", fieldName));
                    }
                }
            }
        }
        else
        {
            // This should never be allowed to happen since we do not allow list operands with '~' so lets log it
            log.error("Text field validation was provided an operand handler that gave us back more than one value when validating '" + fieldName + "'.");
        }

        return messageSet;
    }

    private MessageSet parseQuery(final String indexField, final String fieldName, final String value, final I18nHelper i18n, final Operand operand)
    {
        final MessageSetImpl messageSet = new MessageSetImpl();
        final QueryParser parser = getQueryParser(indexField);
        try
        {
            parser.parse(value);
        }
        catch (final ParseException e)
        {
            log.debug(String.format("Unable to parse the text '%s' for field '%s'.", value, fieldName), e);

            if (operandResolver.isFunctionOperand(operand))
            {
                messageSet.addErrorMessage(i18n.getText("jira.jql.text.clause.does.not.parse.function", fieldName, operand.getName()));
            }
            else
            {
                messageSet.addErrorMessage(i18n.getText("jira.jql.text.clause.does.not.parse", value, fieldName));
            }
        }
        return messageSet;
    }

    private boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.TEXT_OPERATORS.contains(operator);
    }

    ///CLOVER:OFF
    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    QueryParser getQueryParser(final String indexField)
    {
        return new QueryParser(indexField, DefaultIndexManager.ANALYZER_FOR_SEARCHING);
    }
    ///CLOVER:ON
}
