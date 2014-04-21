package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelComparator;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import org.apache.commons.lang.StringUtils;

import java.util.Comparator;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;

/**
 * A stats mapper for Labels
 *
 * @since v4.2
 */
public class LabelsStatisticsMapper implements StatisticsMapper
{
    private final String clauseName;
    private final String indexedField;
    private final boolean includeEmpty;

    public LabelsStatisticsMapper(final boolean includeEmpty)
    {
        this(SystemSearchConstants.forLabels().getJqlClauseNames().getPrimaryName(),
                SystemSearchConstants.forLabels().getIndexField(), includeEmpty);
    }

    public LabelsStatisticsMapper(String clauseName, String indexedField, boolean includeEmpty)
    {
        this.clauseName = notBlank("clauseName", clauseName);
        this.indexedField = notBlank("indexedField", indexedField);
        this.includeEmpty = includeEmpty;
    }

    public Comparator getComparator()
    {
        return LabelComparator.INSTANCE;
    }

    public boolean isValidValue(Object value)
    {
        return value != null || includeEmpty;
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        if (StringUtils.isEmpty(documentValue) || FieldIndexer.LABELS_NO_VALUE_INDEX_VALUE.equals(documentValue))
        {
            return null;
        }
        else
        {
            return new Label(null, null, documentValue);
        }
    }

    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return true;
    }

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
                builder.labels(((Label) value).getLabel());
            }
            else
            {
                builder.addEmptyCondition(getClauseName());
            }
            return new SearchRequest(builder.buildQuery());
        }
    }

    protected String getClauseName()
    {
        return clauseName;
    }

    public String getDocumentConstant()
    {
        return indexedField;
    }
}
