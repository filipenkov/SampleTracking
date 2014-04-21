package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.jql.operand.PredicateOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operand.registry.JqlFunctionHandlerRegistry;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.history.AndHistoryPredicate;
import com.atlassian.query.history.HistoryPredicate;
import com.atlassian.query.history.TerminalHistoryPredicate;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;

import java.util.List;

/**
 * Validates the operand in a HistoryPredicate
 *
 * @since v4.3
 */
public class HistoryPredicateValidator
{
    private final PredicateOperandResolver predicateOperandResolver;
    private final JqlDateSupport jqlDateSupport;
    private final JiraAuthenticationContext authContext;
    private final HistoryFieldValueValidator historyFieldValueValidator;
    private final JqlFunctionHandlerRegistry handlerRegistry;

    public HistoryPredicateValidator(
            final JiraAuthenticationContext authContext,
            final PredicateOperandResolver predicateOperandResolver,
            final JqlDateSupport jqlDateSupport,
            final HistoryFieldValueValidator historyFieldValueValidator,
            final JqlFunctionHandlerRegistry handlerRegistry)
    {

        this.authContext = authContext;
        this.predicateOperandResolver = predicateOperandResolver;
        this.jqlDateSupport = jqlDateSupport;
        this.historyFieldValueValidator = historyFieldValueValidator;
        this.handlerRegistry = handlerRegistry;
    }

    public MessageSet validate(User searcher, WasClause clause, HistoryPredicate predicate)
    {
        return validate(predicate, clause.getField(), searcher);
    }

    public MessageSet validate(User searcher, ChangedClause clause, HistoryPredicate predicate)
    {
        return validate(predicate, clause.getField(), searcher);
    }

    private MessageSet validate(HistoryPredicate predicate, String fieldName, User searcher)
    {
        final MessageSet messageSet = new MessageSetImpl();
        if (predicate instanceof AndHistoryPredicate)
        {
            for (HistoryPredicate historyPredicate : ((AndHistoryPredicate) predicate).getPredicates())
            {
                validateTerminalPredicate(searcher, messageSet, (TerminalHistoryPredicate)historyPredicate, fieldName);
            }
        }
        else
        {
            validateTerminalPredicate(searcher, messageSet, (TerminalHistoryPredicate)predicate, fieldName);
        }
        return messageSet;
    }


    private void validateTerminalPredicate(User searcher, MessageSet messageSet, TerminalHistoryPredicate predicate, String fieldName)
    {
        final Operand operand = predicate.getOperand();
        // empty operands are always valid for predicate searches
        if (predicateOperandResolver.isEmptyOperand(searcher, fieldName, operand))
        {
            return;
        }
        final List<QueryLiteral> values = predicateOperandResolver.getValues(searcher, fieldName, operand);
        if (predicateOperandResolver.isFunctionOperand(searcher, fieldName, operand))
        {
            messageSet.addMessageSet(validateFunctionOperand((FunctionOperand)operand));
        }
        if (predicate.getOperator().equals(Operator.BY))
        {
            messageSet.addMessageSet(validateUsers(searcher, fieldName, Operator.BY.getDisplayString(), values));
        }
        if (OperatorClasses.CHANGE_HISTORY_DATE_PREDICATES.contains(predicate.getOperator()))
        {
            messageSet.addMessageSet(validateDatePredicates(searcher, predicate, fieldName, values));
        }
        //JRADEV-7244
        if (OperatorClasses.CHANGE_HISTORY_VALUE_PREDICATES.contains(predicate.getOperator()))
        {
            messageSet.addMessageSet(historyFieldValueValidator.validateValues(searcher, fieldName, values));
        }
    }

    private  MessageSet validateDatePredicates(User searcher, TerminalHistoryPredicate predicate, String fieldName, List<QueryLiteral> values)
    {
        final I18nHelper i18nHelper = authContext.getI18nHelper();
        final MessageSet messageSet = new MessageSetImpl();
        if (predicate.getOperator().equals(Operator.DURING)) {
            if (values.size() != 2)
            {
                messageSet.addErrorMessage(i18nHelper.getText("jira.jql.predicate.during.length.invalid.func"));
            }
        }
        if (predicate.getOperator().equals(Operator.AFTER)) {
            if (values.size() != 1)
            {
                messageSet.addErrorMessage(i18nHelper.getText("jira.jql.predicate.after.length.invalid.func"));
            }
        }
        if (predicate.getOperator().equals(Operator.BEFORE)) {
            if (values.size() != 1)
            {
                messageSet.addErrorMessage(i18nHelper.getText("jira.jql.predicate.before.length.invalid.func"));
            }
        }
        for (QueryLiteral value : values)
        {
            // longValues can always be turned into a valid date
            final String str = value.getStringValue();
            if (str != null)
            {
                if (!jqlDateSupport.validate(str))
                {
                    if (predicateOperandResolver.isFunctionOperand(searcher, fieldName, value.getSourceOperand()))
                    {
                        messageSet.addErrorMessage(i18nHelper.getText("jira.jql.predicate.date.format.invalid.from.func", predicate.getOperator().getDisplayString(), value.getSourceOperand().getName()));
                    }
                    else
                    {
                        messageSet.addErrorMessage(i18nHelper.getText("jira.jql.predicate.date.format.invalid", str, predicate.getOperator().getDisplayString()));
                    }
                }
            }
        }
        return messageSet;
    }

    private MessageSet validateUsers(User searcher, String fieldName, String predicateName, List<QueryLiteral> values)
    {
        final MessageSet messageSet = new MessageSetImpl();
        for (QueryLiteral rawValue : values)
        {
            if (rawValue.getStringValue() != null)
            {
                if (getUserName(rawValue.getStringValue()) == null)
                {
                    messageSet.addMessageSet(validateUser(searcher, fieldName, predicateName, rawValue.getSourceOperand(), rawValue.getStringValue()));
                }
            }
            else if (rawValue.getLongValue() != null)
            {
                if (getUserName(rawValue.getLongValue().toString()) == null)
                {
                    messageSet.addMessageSet(validateUser(searcher, fieldName, predicateName, rawValue.getSourceOperand(), rawValue.getLongValue().toString()));
                }
            }
        }
        return messageSet;
    }

    private MessageSet validateUser(User searcher, String fieldName, String predicateName, final Operand operand, String rawValue)
    {
        final I18nHelper i18nHelper = authContext.getI18nHelper();
        final MessageSet messageSet = new MessageSetImpl();
        if (predicateOperandResolver.isFunctionOperand(searcher, fieldName, operand))
        {
            messageSet.addErrorMessage(i18nHelper.getText("jira.jql.predicate.by.no.value.for.name.from.function", operand.getName(), predicateName));
        }
        else
        {
            messageSet.addErrorMessage(i18nHelper.getText("jira.jql.predicate.by.no.value.for.name", rawValue));
        }
        return messageSet;
    }

    private MessageSet validateFunctionOperand(FunctionOperand operand)
    {
        final I18nHelper i18nHelper = authContext.getI18nHelper();
        final MessageSet messageSet = new MessageSetImpl();
        final OperandHandler<FunctionOperand> handler = handlerRegistry.getOperandHandler(operand);
        if (handler == null)
        {
            messageSet.addErrorMessage(i18nHelper.getText("jira.jql.operand.illegal.function", operand.getDisplayString()));
        }
        return messageSet;
    }

    private User getUserName(String name)
    {
        return UserUtils.getUser(name.toLowerCase());
    }

}
