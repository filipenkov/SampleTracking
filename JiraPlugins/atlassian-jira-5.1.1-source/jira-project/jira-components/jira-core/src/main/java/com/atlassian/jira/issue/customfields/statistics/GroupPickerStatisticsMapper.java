package com.atlassian.jira.issue.customfields.statistics;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.GroupNameComparator;
import com.atlassian.query.operator.Operator;
import org.apache.commons.lang.StringUtils;

import java.util.Comparator;

import static com.atlassian.util.concurrent.Assertions.notNull;

public class GroupPickerStatisticsMapper implements StatisticsMapper<Group>
{
    private final CustomField customField;
    private final ClauseNames clauseNames;
    private JiraAuthenticationContext authenticationContext;
    private CustomFieldInputHelper customFieldInputHelper;
    private GroupManager groupManager;

    public GroupPickerStatisticsMapper(CustomField customField, GroupManager groupManager,
            final JiraAuthenticationContext authenticationContext, final CustomFieldInputHelper customFieldInputHelper)
    {
        this.groupManager = groupManager;
        this.authenticationContext = notNull("authenticationContext", authenticationContext);
        this.customFieldInputHelper = notNull("customFieldInputHelper", customFieldInputHelper);
        this.customField = notNull("customField", customField);
        this.clauseNames = customField.getClauseNames();
    }

    public String getDocumentConstant()
    {
        return customField.getId();
    }

    public Group getValueFromLuceneField(String documentValue)
    {
        if (StringUtils.isBlank(documentValue))
        {
            return null;
        }
        else
        {
            return groupManager.getGroup(documentValue);
        }
    }

    public Comparator<Group> getComparator()
    {
        return new GroupNameComparator();
    }

    protected String getSearchValue(Object value)
    {
        Group group = (Group) value;
        return group.getName();
    }

    public boolean isValidValue(final Group value)
    {
        return true;
    }

    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return false;
    }

    public SearchRequest getSearchUrlSuffix(Group value, SearchRequest searchRequest)
    {
        if (searchRequest == null)
        {
            return null;
        }
        else
        {
            String clauseName = customFieldInputHelper.getUniqueClauseName(authenticationContext.getLoggedInUser(), clauseNames.getPrimaryName(), customField.getName());
            JqlClauseBuilder builder = JqlQueryBuilder.newBuilder(searchRequest.getQuery()).where().defaultAnd();
            if (value != null)
            {
                builder.addStringCondition(clauseName, Operator.EQUALS, value.getName());
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

        final GroupPickerStatisticsMapper that = (GroupPickerStatisticsMapper) o;

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
