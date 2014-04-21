package com.atlassian.jira.issue.customfields.statistics;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.query.operator.Operator;

import java.util.Comparator;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@Internal
public class SelectStatisticsMapper implements StatisticsMapper<Option>
{
    public static final String RAW_VALUE_SUFFIX = "_raw";

    private final CustomField customField;
    private final ClauseNames clauseNames;
    private final SelectConverter selectConverter;
    private JiraAuthenticationContext authenticationContext;
    private CustomFieldInputHelper customFieldInputHelper;

    public SelectStatisticsMapper(CustomField customField,
            SelectConverter selectConverter, final JiraAuthenticationContext authenticationContext,
            final CustomFieldInputHelper customFieldInputHelper)
    {
        this.authenticationContext = authenticationContext;
        this.customFieldInputHelper = customFieldInputHelper;
        this.customField = notNull("customField", customField);
        this.clauseNames = customField.getClauseNames();
        this.selectConverter = notNull("selectConverter", selectConverter);
    }

    protected String getSearchValue(Option value)
    {
        return selectConverter.getString(value);
    }

    public String getDocumentConstant()
    {
        return customField.getId() + RAW_VALUE_SUFFIX;
    }

    public Option getValueFromLuceneField(String documentValue)
    {
        return selectConverter.getObject(documentValue);
    }

    public Comparator getComparator()
    {
        return new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                if (o1 == null && o2 == null)
                {
                    return 0;
                }
                else if (o1 == null)
                {
                    return 1;
                }
                else if (o2 == null)
                {
                    return -1;
                }
                return ((Option) o1).getSequence().compareTo(((Option) o2).getSequence());
            }
        };

    }

    public boolean isValidValue(final Option value)
    {
        return true;
    }

    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return false;
    }

    public SearchRequest getSearchUrlSuffix(final Option option, final SearchRequest searchRequest)
    {
        if (searchRequest == null)
        {
            return null;
        }
        else
        {
            final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder(searchRequest.getQuery()).where().defaultAnd();
            final String clauseName = customFieldInputHelper.getUniqueClauseName(authenticationContext.getLoggedInUser(), clauseNames.getPrimaryName(), customField.getName());
            if (option != null)
            {
                builder.addStringCondition(clauseName, Operator.EQUALS, option.getValue());
            }
            else
            {
                builder.addEmptyCondition(clauseName);
            }
            return new SearchRequest(builder.buildQuery());
        }
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final SelectStatisticsMapper that = (SelectStatisticsMapper) o;

        if (!clauseNames.equals(that.clauseNames))
        {
            return false;
        }
        if (!customField.getId().equals(that.customField.getId()))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = customField.getId().hashCode();
        result = 31 * result + clauseNames.hashCode();
        return result;
    }
}
