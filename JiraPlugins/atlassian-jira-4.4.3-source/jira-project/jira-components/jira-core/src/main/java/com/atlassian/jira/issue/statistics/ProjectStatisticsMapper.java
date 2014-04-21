package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

public class ProjectStatisticsMapper implements StatisticsMapper
{
    private final ProjectManager projectManager;
    private final String clauseName;
    private final String documentConstant;

    public ProjectStatisticsMapper(ProjectManager projectManager)
    {
        this(projectManager, SystemSearchConstants.forProject());
    }

    public ProjectStatisticsMapper(ProjectManager projectManager, SimpleFieldSearchConstants searchConstants)
    {
        this(projectManager, searchConstants.getJqlClauseNames().getPrimaryName(), searchConstants.getIndexField());
    }

    public ProjectStatisticsMapper(ProjectManager projectManager, String clauseName, String documentConstant)
    {
        this.projectManager = projectManager;
        this.clauseName = clauseName;
        this.documentConstant = documentConstant;
    }

    public String getDocumentConstant()
    {
        return documentConstant;
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        //JRA-19121: Project custom field may return a null documentValue here.  System version fields will return -1.
        if (StringUtils.isNotBlank(documentValue))
        {
            long projectId = Long.parseLong(documentValue);
            if (projectId > 0)
            {
                return projectManager.getProject(projectId);
            }
        }
        return null;
    }

    public Comparator getComparator()
    {
        return OfBizComparators.NAME_COMPARATOR;
    }

    public boolean isValidValue(Object value)
    {
        return true;
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
                final Long projectId = ((GenericValue) value).getLong("id");
                final Project project = projectManager.getProjectObj(projectId);
                builder.addClause(new TerminalClauseImpl(getClauseName(), Operator.EQUALS, project.getKey()));
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

        final ProjectStatisticsMapper that = (ProjectStatisticsMapper) o;

        return (getDocumentConstant() != null ? getDocumentConstant().equals(that.getDocumentConstant()) : that.getDocumentConstant() == null);
    }

    public int hashCode()
    {
        return (getDocumentConstant() != null ? getDocumentConstant().hashCode() : 0);
    }
}
