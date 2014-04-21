package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.validator.FreeTextFieldValidator;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;

/**
 * The {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer} for free text custom fields.
 *
 * @since v4.0
 */
public class FreeTextCustomFieldSearchInputTransformer extends AbstractSingleValueCustomFieldSearchInputTransformer implements SearchInputTransformer
{
    private static final Logger log = Logger.getLogger(FreeTextCustomFieldSearchInputTransformer.class);
    private final CustomField customField;
    private final ClauseNames clauseNames;

    public FreeTextCustomFieldSearchInputTransformer(CustomField customField, ClauseNames clauseNames, String urlParameterName,
            final CustomFieldInputHelper customFieldInputHelper)
    {
        super(customField, clauseNames, urlParameterName, customFieldInputHelper);
        this.customField = customField;
        this.clauseNames = clauseNames;
    }

    @Override
    Clause createSearchClause(final User searcher, final String value)
    {
        return new TerminalClauseImpl(getClauseName(searcher, clauseNames), Operator.LIKE, value);
    }
    
    public boolean doRelevantClausesFitFilterForm(final User searcher, final Query query, final SearchContext searchContext)
    {
        return convertForNavigator(query).fitsNavigator();
    }


    @Override
    public void validateParams(final User searcher, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final I18nHelper i18nHelper, final ErrorCollection errors)
    {
        if (fieldValuesHolder.containsKey(customField.getId()))
        {
            final CustomFieldParams customFieldParams = (CustomFieldParams) fieldValuesHolder.get(customField.getId());
            if (customField.getReleventConfig(searchContext) != null)
            {
                String paramValue = getFieldValueAsString(customField.getCustomFieldType(), customFieldParams, errors);
                if (paramValue != null)
                {
                    String firstLetter = String.valueOf(paramValue.charAt(0));
                    if (FreeTextFieldValidator.INVALID_FIRST_CHAR_LIST.contains(firstLetter))
                    {
                        errors.addError(customField.getId(), i18nHelper.getText("navigator.error.query.invalid.start", firstLetter));
                    }
                    else if (!isQueryValid(paramValue, customField))
                    {
                        errors.addError(customField.getId(), i18nHelper.getText("navigator.error.parse"));
                    }
                }
            }
            else
            {
                log.warn("Searcher " +getCustomField(). getId() + " (" + ClassUtils.getShortClassName(getClass())
                    + ") tried to search with context it does not exist for. The search context is " + searchContext);
            }
        }
    }

    /**
     * We know the field value will be a String, except if some other part of the system is completely broken.
     *
     * @param customFieldType   the custom field type
     * @param customFieldParams the field params, should be single value String.
     * @param errors            errors to add any problems to.
     * @return the String value, possibly null if there were errors.
     */
    private String getFieldValueAsString(CustomFieldType customFieldType, CustomFieldParams customFieldParams, ErrorCollection errors)
    {
        String paramValue = null;
        Object paramValueObject = new Object();
        try
        {
            paramValueObject = customFieldType.getValueFromCustomFieldParams(customFieldParams);
            paramValue = (String) paramValueObject;
        }
        catch (FieldValidationException e)
        {
            // this should never happen because we should always just be getting a string
            errors.addError(getCustomField().getId(), e.getMessage());
        }
        catch (ClassCastException e)
        {
            // shouldn't happen
            errors.addError(getCustomField().getId(), "Internal error attempting to validate the search term.");
            String mesg = "Expected to be able to get String value out of custom customField that has a " +
                "text searcher, actual value type is " + paramValueObject.getClass();
            log.error(mesg);
        }
        return paramValue;
    }

    private boolean isQueryValid(String query, CustomField customField)
    {
        QueryParser qp = createQueryParser(customField);
        try
        {
            qp.parse(query);
            // if it didn't throw ParseException it must be valid
            return true;
        }
        catch (ParseException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Can't parse text query, user notified." + e);
            }
            return false;
        }
    }

    ///CLOVER:OFF
    QueryParser createQueryParser(final CustomField customField)
    {
        return new QueryParser(customField.getId(), DefaultIndexManager.ANALYZER_FOR_SEARCHING);
    }
    ///CLOVER:ON
}