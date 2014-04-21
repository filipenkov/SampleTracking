package com.atlassian.jira.jql.validator;


import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.JqlChangeItemMapping;
import com.atlassian.jira.issue.index.ChangeHistoryFieldConfigurationManager;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.ValueGeneratingClauseHandler;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.jql.values.ClauseValuesGenerator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.I18nBean;
import com.google.common.base.Function;
import com.google.common.collect.Lists;


import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Validates that the values in a history clause are valid for a field.  Has to take into account historical name changes.
 *
 * @since v4.4
 */
public class HistoryFieldValueValidator
{
    private final SearchHandlerManager searchHandlerManager;
    private final JqlChangeItemMapping jqlChangeItemMapping;
    private final ChangeHistoryManager changeHistoryManager;
    private final JqlOperandResolver operandResolver;
    private final ChangeHistoryFieldConfigurationManager configurationManager;

    public HistoryFieldValueValidator(SearchHandlerManager searchHandlerManager, JqlChangeItemMapping jqlChangeItemMapping, ChangeHistoryManager changeHistoryManager, JqlOperandResolver operandResolver, ChangeHistoryFieldConfigurationManager configurationManager) {
        this.searchHandlerManager = searchHandlerManager;
        this.jqlChangeItemMapping = jqlChangeItemMapping;
        this.changeHistoryManager = changeHistoryManager;
        this.operandResolver = operandResolver;
        this.configurationManager = configurationManager;
    }

    private boolean stringValueExists(User searcher, String fieldName, String rawValue)
    {
        final String valuePrefix = "";
        final Collection<ClauseHandler> clauseHandlers = searchHandlerManager.getClauseHandler(searcher, fieldName);
        if (clauseHandlers != null && clauseHandlers.size() == 1)
        {
            ClauseHandler clauseHandler = clauseHandlers.iterator().next();
            if (clauseHandler instanceof ValueGeneratingClauseHandler)
            {

                final ClauseValuesGenerator.Results generatorResults = ((ValueGeneratingClauseHandler) (clauseHandler))
                        .getClauseValuesGenerator().getPossibleValues(searcher, jqlChangeItemMapping.mapJqlClauseToFieldName(fieldName), valuePrefix, Integer.MAX_VALUE);
                final List<ClauseValuesGenerator.Result> list = generatorResults.getResults();
                final List<String> possibleValues = Lists.transform(list,
                        new Function<ClauseValuesGenerator.Result, String>()
                        {
                            public String apply(ClauseValuesGenerator.Result from)
                            {
                                return from.getValue().toLowerCase();
                            }
                        });
                if (!possibleValues.contains(rawValue.toLowerCase()))
                {
                    final Map<String, String> possibleValuesMap =  changeHistoryManager.findAllPossibleValues( jqlChangeItemMapping.mapJqlClauseToFieldName(fieldName).toLowerCase());
                    return possibleValuesMap.containsKey(rawValue.toLowerCase());
                }
                return possibleValues.contains(rawValue.toLowerCase());
            }
        }
        return false;
    }

    public MessageSet validateValues(User searcher, String fieldName, List<QueryLiteral> rawValues)
    {
        final MessageSet messages = new MessageSetImpl();
        for (QueryLiteral rawValue : rawValues)
        {
            if (rawValue.getStringValue() != null)
            {
                if (!stringValueExists(searcher, fieldName, rawValue.getStringValue()))
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
                if (stringValueExists(searcher, fieldName, rawValue.getLongValue().toString()))
                {
                    return messages;
                }
                if (!configurationManager.supportsIdSearching(fieldName.toLowerCase()))
                {
                    if (operandResolver.isFunctionOperand(rawValue.getSourceOperand()))
                    {
                        messages.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.no.value.for.name.from.function", rawValue.getSourceOperand().getName(), fieldName));
                    }
                    else
                    {
                        messages.addErrorMessage(getI18n(searcher).getText("jira.jql.history.clause.not.string", rawValue.getSourceOperand().getName(), fieldName));
                    }
                }
                else
                {
                    if (!longValueExists(searcher, fieldName, rawValue.getLongValue()))
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

    private boolean longValueExists(User searcher, String fieldName, Long longValue)
    {
        NameResolver resolver  = configurationManager.getNameResolver(fieldName.toLowerCase());
        return resolver.idExists(longValue);
    }

    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }
}
