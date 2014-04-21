package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.comparator.ComponentComparator;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.query.clause.Clause;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

public class ComponentStatisticsMapper implements StatisticsMapper
{
    private static final Logger log = Logger.getLogger(ComponentStatisticsMapper.class);
    private final SimpleFieldSearchConstantsWithEmpty searchConstants = SystemSearchConstants.forComponent();

    public String getDocumentConstant()
    {
        return searchConstants.getIndexField();
    }

    public Comparator getComparator()
    {
        return ComponentComparator.COMPARATOR;
    }

    public boolean isValidValue(Object value)
    {
        return true;
    }

    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return true;
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        long componentId = Long.parseLong(documentValue);
        if (componentId > 0)
        {
            try
            {
                // Retrieve current version of ProjectComponentManager
                ProjectComponentManager projectComponentManager = getProjectComponentManager();
                return projectComponentManager.convertToGenericValue(projectComponentManager.find(componentId));
            }
            catch (EntityNotFoundException e)
            {
                log.error("Indexes may be corrupt - unable to retrieve component with id '" + componentId + "'.");
            }
        }
        return null;
    }

    protected ProjectComponentManager getProjectComponentManager()
    {
        return ComponentAccessor.getComponent(ProjectComponentManager.class);
    }

    protected ProjectManager getProjectManager()
    {
        return ComponentAccessor.getComponent(ProjectManager.class);
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
                ProjectComponentManager projectComponentManager = getProjectComponentManager();
                ProjectManager projectManager = getProjectManager();
                final GenericValue component = (GenericValue)value;
                final Long id = component.getLong("id");
                final Long projectId = component.getLong("project");
                
                try
                {
                    final ProjectComponent projectComponent = projectComponentManager.find(id);
                    final String componentName = projectComponent.getName();
                    final Project project = projectManager.getProjectObj(projectId);
                    builder.component(componentName).project(project.getKey());
                }
                catch(EntityNotFoundException e)
                {
                    log.error("Unable to retrieve component with id '" + id + "'.");
                    builder.component(id).project(projectId);
                }
            }
            else
            {
                builder.componentIsEmpty();
            }
            
            return new SearchRequest(builder.buildQuery());
        }
    }

    protected Clause getComponentClause(Long value)
    {
        return JqlQueryBuilder.newBuilder().where().component(value).buildClause();
    }

    protected Clause getProjectClause(Long value)
    {
        return JqlQueryBuilder.newBuilder().where().project(value).buildClause();
    }

    public int hashCode()
    {
        return this.getDocumentConstant().hashCode();
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }

        ComponentStatisticsMapper that = (ComponentStatisticsMapper) obj;

        return this.getDocumentConstant().equals(that.getDocumentConstant());
    }


}
