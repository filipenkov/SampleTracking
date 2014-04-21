package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.NumberIndexValueConverter;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.opensymphony.user.User;

import static com.atlassian.util.concurrent.Assertions.notNull;

/**
 * A validator for number custom fields.
 *
 * @since v4.0
 */
public class NumberCustomFieldValidator implements ClauseValidator
{
    private final IndexValuesValidator indexValuesValidator;
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final JqlOperandResolver jqlOperandResolver;

    public NumberCustomFieldValidator(final JqlOperandResolver jqlOperandResolver, NumberIndexValueConverter indexValueConverter)
    {
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
        this.indexValuesValidator = getIndexValuesValidator(indexValueConverter);
    }
    //CLOVER:OFF
    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        MessageSet errors = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!errors.hasAnyErrors())
        {
            errors = indexValuesValidator.validate(searcher, terminalClause);
        }
        return errors;
    }
    ///CLOVER:ON

    IndexValuesValidator getIndexValuesValidator(NumberIndexValueConverter indexValueConverter)
    {
        return new IndexValuesValidator(jqlOperandResolver, indexValueConverter)
        {
            @Override
            void addError(final MessageSet messageSet, final User searcher, TerminalClause terminalClause, final QueryLiteral literal)
            {
                String fieldName = terminalClause.getName();
                if (jqlOperandResolver.isFunctionOperand(literal.getSourceOperand()))
                {
                    messageSet.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.invalid.number.value.function", literal.getSourceOperand().getName(), fieldName));
                }
                else
                {
                    messageSet.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.invalid.number.value", fieldName, literal.asString()));
                }
            }
        };
    }

    ///CLOVER:OFF
    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, OperatorClasses.RELATIONAL_ONLY_OPERATORS);
    }
    ///CLOVER:ON

     ///CLOVER:OFF
    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }
    ///CLOVER:ON

}
