package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.comparator.VersionComparator;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.query.operator.Operator;
import org.apache.commons.lang.StringUtils;

import java.util.Comparator;

public class VersionStatisticsMapper implements StatisticsMapper
{
    private final VersionManager versionManager;
    private final String clauseName;
    private final String documentConstant;
    boolean includeArchived = false;

    public VersionStatisticsMapper(final String clauseName, final String documentConstant, VersionManager versionManager, boolean includeArchived)
    {
        this.clauseName = clauseName;
        this.documentConstant = documentConstant;
        this.versionManager = versionManager;
        this.includeArchived = includeArchived;
    }

    public Comparator getComparator()
    {
        return VersionComparator.COMPARATOR;
    }

    public boolean isValidValue(Object value)
    {
        if (value == null)
        {
            return true;
        }
        if (!includeArchived)
        {
            return !((Version) value).isArchived();
        }
        else
        {
            return true;
        }
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        //JRA-19118: Version custom field may return a null documentValue here.  System version fields will return -1.
        if (StringUtils.isNotBlank(documentValue))
        {
            long versionId = Long.parseLong(documentValue);
            if (versionId > 0)
            {
                return versionManager.getVersion(new Long(versionId));
            }
        }
        return null;
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
                final Version version = (Version) value;
                builder.project(version.getProjectObject().getKey());
                builder.addStringCondition(getClauseName(), Operator.EQUALS, version.getName());
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

    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return true;
    }

    public String getDocumentConstant()
    {
        return documentConstant;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final VersionStatisticsMapper that = (VersionStatisticsMapper) o;

        if (includeArchived != that.includeArchived)
        {
            return false;
        }

        return (getDocumentConstant() != null ? getDocumentConstant().equals(that.getDocumentConstant()) : that.getDocumentConstant() == null);

    }

    public int hashCode()
    {
        int result;
        result = (includeArchived ? 1 : 0);
        result = 29 * result + (getDocumentConstant() != null ? getDocumentConstant().hashCode() : 0);
        return result;
    }
}
