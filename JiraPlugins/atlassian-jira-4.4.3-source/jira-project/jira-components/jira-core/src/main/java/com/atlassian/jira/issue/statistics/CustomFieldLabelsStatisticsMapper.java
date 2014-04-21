package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.jira.security.JiraAuthenticationContext;

/**
 * @since v4.2
 */
public class CustomFieldLabelsStatisticsMapper extends LabelsStatisticsMapper
{
    private CustomField customField;
    private CustomFieldInputHelper customFieldInputHelper;
    private final JiraAuthenticationContext authenticationContext;

    public CustomFieldLabelsStatisticsMapper(CustomField customField, final CustomFieldInputHelper customFieldInputHelper,
            final JiraAuthenticationContext authenticationContext, final boolean includeEmpty)
    {
        super(JqlCustomFieldId.toString(customField.getIdAsLong()), customField.getId(), includeEmpty);
        this.customField = customField;
        this.customFieldInputHelper = customFieldInputHelper;
        this.authenticationContext = authenticationContext;
    }

    @Override
    protected String getClauseName()
    {
        return customFieldInputHelper.getUniqueClauseName(authenticationContext.getUser(), customField.getClauseNames().getPrimaryName(), customField.getName());
    }

    @Override
    public SearchRequest getSearchUrlSuffix(Object value, SearchRequest searchRequest)
    {
        if (searchRequest == null)
        {
            return null;
        }
        else
        {
            JqlClauseBuilder builder = JqlQueryBuilder.newBuilder(searchRequest.getQuery()).where().defaultAnd();
            if (value != null)
            {
                builder.customField(customField.getIdAsLong()).eq(value.toString());
            }
            else
            {
                builder.addEmptyCondition(getClauseName());
            }
            return new SearchRequest(builder.buildQuery());
        }
    }
}
