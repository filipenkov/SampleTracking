package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.index.ChangeHistoryFieldConfigurationManager;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.operand.PredicateOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.history.AndHistoryPredicate;
import com.atlassian.query.history.HistoryPredicate;
import com.atlassian.query.history.TerminalHistoryPredicate;
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
    private final SearchHandlerManager searchHandlerManager;
    private final JqlDateSupport jqlDateSupport;
    private final JiraAuthenticationContext authContext;
    private final ChangeHistoryFieldConfigurationManager configurationManager;
    private final HistoryFieldValueValidator historyFieldValueValidator;

    public HistoryPredicateValidator(final JiraAuthenticationContext authContext, final PredicateOperandResolver predicateOperandResolver, final JqlDateSupport jqlDateSupport, SearchHandlerManager searchHandlerManager, ChangeHistoryFieldConfigurationManager configurationManager, HistoryFieldValueValidator historyFieldValueValidator)
    {

        this.authContext = authContext;
        this.predicateOperandResolver = predicateOperandResolver;
        this.jqlDateSupport = jqlDateSupport;
        this.searchHandlerManager = searchHandlerManager;
        this.configurationManager = configurationManager;
        this.historyFieldValueValidator = historyFieldValueValidator;
    }

    public MessageSet validate(User searcher, WasClause clause, HistoryPredicate predicate)
    {
        return validate(predicate, clause.getField(), searcher);
    }

    public MessageSet validate(User searcher, ChangedClause clause, HistoryPredicate predicate)
    {
        return validate( predicate, clause.getField(), searcher);
    }

    private MessageSet validate(HistoryPredicate predicate, String fieldName, User searcher)
    {
        final I18nHelper i18nHelper = authContext.getI18nHelper();
        final MessageSet messageSet = new MessageSetImpl();
        if (predicate instanceof AndHistoryPredicate)
        {
            for (HistoryPredicate historyPredicate  : ((AndHistoryPredicate) predicate).getPredicates())
            {
                validateTerminalPredicate(searcher, i18nHelper, messageSet, (TerminalHistoryPredicate)historyPredicate, fieldName);
            }
        }
        else
        {
            validateTerminalPredicate(searcher, i18nHelper, messageSet, (TerminalHistoryPredicate)predicate, fieldName);
        }
        return messageSet;
    }


    private void validateTerminalPredicate(User searcher, I18nHelper i18nHelper, MessageSet messageSet, TerminalHistoryPredicate predicate, String fieldName)
    {
        final Operand operand = predicate.getOperand();
        final List<QueryLiteral> values = predicateOperandResolver.getValues(searcher, operand);
        if (predicate.getOperator().equals(Operator.BY))
        {
            messageSet.addMessageSet(validateUsers(searcher, i18nHelper, Operator.BY.getDisplayString(), values));
        }
        if (OperatorClasses.CHANGE_HISTORY_DATE_PREDICATES.contains(predicate.getOperator()))
        {
            messageSet.addMessageSet(validateDatePredicates(searcher, i18nHelper, predicate, fieldName, values));
        }
        //JRADEV-7244
        if (OperatorClasses.CHANGE_HISTORY_VALUE_PREDICATES.contains(predicate.getOperator()))
        {
            messageSet.addMessageSet(historyFieldValueValidator.validateValues(OSUserConverter.convertToOSUser(searcher), fieldName, values));
        }

    }

    private  MessageSet validateDatePredicates(User searcher, I18nHelper i18nHelper, TerminalHistoryPredicate predicate, String fieldName, List<QueryLiteral> values)
    {
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
                    if (predicateOperandResolver.isFunctionOperand(searcher, value.getSourceOperand()))
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

    private MessageSet validateUsers(User searcher, I18nHelper i18nHelper, String predicateName, List<QueryLiteral> values)
    {
        final MessageSet messageSet = new MessageSetImpl();
        for (QueryLiteral rawValue : values)
        {
            if (rawValue.getStringValue() != null)
            {
                if (getUserName(rawValue.getStringValue()) == null)
                {
                    if (predicateOperandResolver.isFunctionOperand(searcher, rawValue.getSourceOperand()))
                    {
                        messageSet.addErrorMessage(i18nHelper.getText("jira.jql.predicate.by.no.value.for.name.from.function", rawValue.getSourceOperand().getName(), predicateName));
                    }
                    else
                    {
                        messageSet.addErrorMessage(i18nHelper.getText("jira.jql.predicate.by.no.value.for.name", rawValue.getStringValue()));
                    }
                }
            }
            else if (rawValue.getLongValue() != null)
            {
                messageSet.addErrorMessage(i18nHelper.getText("jira.jql.predicate.by.no.value.for.id", rawValue.getLongValue().toString()));
            }
        }
        return messageSet;
    }

    private User getUserName(String name)
    {
        return UserUtils.getUser(name.toLowerCase());
    }

}
