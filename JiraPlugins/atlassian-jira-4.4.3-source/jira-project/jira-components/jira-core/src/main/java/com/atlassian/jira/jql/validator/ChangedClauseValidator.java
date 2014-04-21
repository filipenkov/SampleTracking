package com.atlassian.jira.jql.validator;


import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.JqlChangeItemMapping;
import com.atlassian.jira.issue.index.ChangeHistoryFieldConfigurationManager;
import com.atlassian.jira.issue.index.IndexedChangeHistoryFieldManager;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.PredicateOperandResolver;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.ChangedClause;


/**
 * Validat the chnaged clause against any field.
 *
 * @since v5.0
 */
public class ChangedClauseValidator 
{
    private final IndexedChangeHistoryFieldManager indexedChangeHistoryFieldManager;
    private final SearchHandlerManager searchHandlerManager;
    private final ChangeHistoryManager changeHistoryManager;
    private final PredicateOperandResolver predicateOperandResolver;
    private final JqlDateSupport jqlDateSupport;
    private final HistoryPredicateValidator  historyPredicateValidator;
    private final JiraAuthenticationContext authContext;
    private final JqlChangeItemMapping jqlChangeItemMapping;
    private final ChangeHistoryFieldConfigurationManager configurationManager;
    private final HistoryFieldValueValidator historyFieldValueValidator;


    public ChangedClauseValidator(final JqlOperandResolver operandResolver,
            final SearchHandlerManager searchHandlerManager,
            final IndexedChangeHistoryFieldManager indexedChangeHistoryFieldManager,
            final ChangeHistoryManager changeHistoryManager,
            final PredicateOperandResolver predicateOperandResolver,
            final JqlDateSupport jqlDateSupport,
            final JiraAuthenticationContext authContext,
            final JqlChangeItemMapping jqlChangeItemMapping, ChangeHistoryFieldConfigurationManager configurationManager, HistoryFieldValueValidator historyFieldValueValidator)
    {
        this.searchHandlerManager = searchHandlerManager;
        this.indexedChangeHistoryFieldManager = indexedChangeHistoryFieldManager;
        this.jqlChangeItemMapping = jqlChangeItemMapping;
        this.configurationManager = configurationManager;
        this.changeHistoryManager = changeHistoryManager;
        this.predicateOperandResolver = predicateOperandResolver;
        this.jqlDateSupport = jqlDateSupport;
        this.authContext = authContext;
        this.historyFieldValueValidator = historyFieldValueValidator;
        this.historyPredicateValidator = getHistoryPredicateValidator();
    }

    private HistoryPredicateValidator getHistoryPredicateValidator()
    {
        return new HistoryPredicateValidator(authContext, predicateOperandResolver, jqlDateSupport, searchHandlerManager, configurationManager, historyFieldValueValidator);
    }

    public MessageSet validate(final User searcher, final ChangedClause clause)
    {
        final MessageSet messageSet = new MessageSetImpl();
        validateField(searcher, clause.getField(), messageSet);
        if (clause.getPredicate() != null)
        {
           messageSet.addMessageSet(historyPredicateValidator.validate(searcher, clause, clause.getPredicate()));
        }
        return messageSet;
    }

    private void validateField(User searcher, String fieldName, MessageSet messages)
    {
        if (!indexedChangeHistoryFieldManager.getIndexedChangeHistoryFieldNames().contains(fieldName.toLowerCase()))
        {
            messages.addErrorMessage(getI18n(searcher).getText("jira.jql.history.field.not.supported", fieldName));
        }
    }


    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }


}

