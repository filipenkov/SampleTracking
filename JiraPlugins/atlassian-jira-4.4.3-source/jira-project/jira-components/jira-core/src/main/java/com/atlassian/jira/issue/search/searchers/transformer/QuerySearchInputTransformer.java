package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.QuerySearcher;
import com.atlassian.jira.issue.search.searchers.util.DefaultQuerySearcherInputHelper;
import com.atlassian.jira.issue.search.searchers.util.QuerySearcherInputHelper;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.validator.FreeTextFieldValidator;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The Search Input Transformer for system text fields Comments, Summary, Description and Environment.
 *
 * @since v4.0
 */
public class QuerySearchInputTransformer implements SearchInputTransformer
{
    private final ApplicationProperties applicationProperties;
    private final JqlOperandResolver operandResolver;

    public QuerySearchInputTransformer(final ApplicationProperties applicationProperties, final JqlOperandResolver operandResolver)
    {
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.operandResolver = notNull("operandResolver", operandResolver);
    }

    public void populateFromParams(final User searcher, FieldValuesHolder fieldValuesHolder, ActionParams actionParams)
    {
        fieldValuesHolder.put(QuerySearcher.QUERY_URL_PARAM, actionParams.getFirstValueForKey(QuerySearcher.QUERY_URL_PARAM));

        List<String> queryFields = new ArrayList<String>(4);
        for (String fieldId : QuerySearcher.QUERY_URL_FIELD_PARAMS)
        {
            String value = actionParams.getFirstValueForKey(fieldId);
            if ("true".equals(value))
            {
                fieldValuesHolder.put(fieldId, value);
                queryFields.add(fieldId);
            }
        }

        fieldValuesHolder.put(QuerySearcher.QUERY_FIELDS_ID, queryFields);
    }

    public void populateFromQuery(final User searcher, FieldValuesHolder fieldValuesHolder, Query query, final SearchContext searchContext)
    {
        if (query != null && query.getWhereClause() != null)
        {
            QuerySearcherInputHelper helper = createQuerySearcherInputHelper();

            // We only want to populate the field values holder if all the text fields have the same query string or
            // some have not been specified
            final Map<String, String> result = helper.convertClause(query.getWhereClause(), searcher);
            if (result != null)
            {
                // Set the query string into the field values holder
                fieldValuesHolder.putAll(result);

                List<String> queryFields = new ArrayList<String>();
                // Need to set in the fieldValuesHolder the query field ids that are being used, excluding the
                // "queryString" field (i.e. only the component fields)
                for (String fieldId : result.keySet())
                {
                    if (!QuerySearcher.QUERY_URL_PARAM.equals(fieldId))
                    {
                        queryFields.add(fieldId);
                    }
                }
                fieldValuesHolder.put(QuerySearcher.QUERY_FIELDS_ID, queryFields);
            }
        }
    }

    public boolean doRelevantClausesFitFilterForm(final User searcher, final Query query, final SearchContext searchContext)
    {
        if (query.getWhereClause() != null)
        {
            final Clause whereClause = query.getWhereClause();
            // check that it conforms to simple navigator structure and
            // that the right number of clauses appear with the correct operators
            QuerySearcherInputHelper inputHelper = createQuerySearcherInputHelper();
            if (inputHelper.convertClause(whereClause, searcher) == null)
            {
                return false;
            }
        }
        return true;
    }

    public Clause getSearchClause(final User searcher, final FieldValuesHolder fieldValuesHolder)
    {
        // full text query and if indexing is turned on
        String query = ParameterUtils.getStringParam(fieldValuesHolder, QuerySearcher.QUERY_URL_PARAM);
        if (StringUtils.isNotBlank(query))
        {
            List<Clause> clauses = new ArrayList<Clause>();

            if (fieldValuesHolder.containsKey(SystemSearchConstants.forSummary().getUrlParameter()))
            {
                // build a terminal clause for summary
                clauses.add(new TerminalClauseImpl(SystemSearchConstants.forSummary().getJqlClauseNames().getPrimaryName(), Operator.LIKE, query));
            }

            if (fieldValuesHolder.containsKey(SystemSearchConstants.forDescription().getUrlParameter()))
            {
                // build a terminal clause for description
                clauses.add(new TerminalClauseImpl(SystemSearchConstants.forDescription().getJqlClauseNames().getPrimaryName(), Operator.LIKE, query));
            }

            // NOTE: that here we are translating between the input request parameter 'body' to the JQL construct
            // for comment, 'comment'
            if (fieldValuesHolder.containsKey(SystemSearchConstants.forComments().getUrlParameter()))
            {
                // build a terminal clause for comment
                clauses.add(new TerminalClauseImpl(SystemSearchConstants.forComments().getJqlClauseNames().getPrimaryName(), Operator.LIKE, query));
            }

            if (fieldValuesHolder.containsKey(SystemSearchConstants.forEnvironment().getUrlParameter()))
            {
                // build a terminal clause for summary
                clauses.add(new TerminalClauseImpl(SystemSearchConstants.forEnvironment().getJqlClauseNames().getPrimaryName(), Operator.LIKE, query));
            }

            if (clauses.size() == 1)
            {
                return clauses.get(0);
            }
            else if (!clauses.isEmpty())
            {
                return new OrClause(clauses);
            }
        }
        return null;
    }

    public void validateParams(final User searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, I18nHelper i18nHelper, ErrorCollection errors)
    {
        String query = (String) fieldValuesHolder.get(QuerySearcher.QUERY_URL_PARAM);

        if (StringUtils.isNotBlank(query))
        {
            String firstLetter = String.valueOf(query.charAt(0));
            if (FreeTextFieldValidator.INVALID_FIRST_CHAR_LIST.contains(firstLetter))
            {
                errors.addError(QuerySearcher.ID, i18nHelper.getText("navigator.error.query.invalid.start", firstLetter));
            }
            else if (!isQueryValid(query))
            {
                errors.addError(QuerySearcher.ID, i18nHelper.getText("navigator.error.parse"));
            }
        }
    }

    private boolean isQueryValid(String query)
    {
        QueryParser qp = createQueryParser();
        try
        {
            qp.parse(query);
            // if it didn't throw ParseException it must be valid
            return true;
        }
        catch (ParseException e)
        {
            return false;
        }
    }

///CLOVER:OFF
    QuerySearcherInputHelper createQuerySearcherInputHelper()
    {
        return new DefaultQuerySearcherInputHelper(QuerySearcher.QUERY_URL_PARAM, operandResolver);
    }

    QueryParser createQueryParser()
    {
        // We pass in the summary index field here, because we dont actually care about the lhs of the query, only that
        // user input can be parsed.
        return new QueryParser(SystemSearchConstants.forSummary().getIndexField(), DefaultIndexManager.ANALYZER_FOR_SEARCHING);
    }
///CLOVER:ON

}
