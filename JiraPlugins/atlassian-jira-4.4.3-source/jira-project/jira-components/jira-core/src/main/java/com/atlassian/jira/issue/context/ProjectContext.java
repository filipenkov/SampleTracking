package com.atlassian.jira.issue.context;

import com.atlassian.bandana.BandanaContext;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.collect.MapBuilder;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

public class ProjectContext extends AbstractJiraContext
{
    // ------------------------------------------------------------------------------------------------- Type Properties
    protected Long projectCategory;
    protected Long projectId;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final JiraContextTreeManager treeManager;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public ProjectContext(final Long projectId)
    {
        this.projectId = projectId;
        treeManager = ComponentManager.getComponentInstanceOfType(JiraContextTreeManager.class);
    }

    public ProjectContext(final GenericValue project, final JiraContextTreeManager treeManager)
    {
        projectId = project != null ? project.getLong("id") : null;

        if (treeManager != null)
        {
            this.treeManager = treeManager;
        }
        else
        {
            this.treeManager = ComponentManager.getComponentInstanceOfType(JiraContextTreeManager.class);
        }
    }

    public ProjectContext(final IssueContext issueContext, final JiraContextTreeManager treeManager)
    {
        this(issueContext.getProject(), treeManager);
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    public BandanaContext getParentContext()
    {
        return new ProjectCategoryContext(getProjectCategory(), treeManager);
    }

    public boolean hasParentContext()
    {
        return true;
    }

    public List<JiraContextNode> getChildContexts()
    {
        return null;
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods
    public Map<String, Object> appendToParamsMap(final Map<String, Object> input)
    {
        return MapBuilder.newBuilder(input).add(FIELD_PROJECT, getProject() != null ? getProject().getLong("id") : null).toMap();
        //        props.put(FIELD_PROJECT_CATEGORY, getProjectCategory() != null ? getProjectCategory().getLong("id") : null);
        //        props.put(FIELD_ISSUE_TYPE, null);
    }

    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public Project getProjectObject()
    {
        if (projectId == null)
        {
            return null;
        }
        return treeManager.getProjectManager().getProjectObj(projectId);
    }

    public GenericValue getProject()
    {
        return projectId != null ? treeManager.getProjectManager().getProject(projectId) : null;
    }

    public GenericValue getProjectCategory()
    {
        if ((projectCategory == null) && (projectId != null))
        {
            final GenericValue projectCategoryGv = treeManager.getProjectManager().getProjectCategoryFromProject(getProject());
            projectCategory = projectCategoryGv != null ? projectCategoryGv.getLong("id") : null;
        }

        return projectCategory != null ? treeManager.getProjectManager().getProjectCategory(projectCategory) : null;
    }

    public IssueType getIssueTypeObject()
    {
        return null;
    }

    public GenericValue getIssueType()
    {
        return null;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || !(o instanceof JiraContextNode))
        {
            return false;
        }

        final ProjectContext projectContext = (ProjectContext) o;
        if (projectId != null ? !projectId.equals(projectContext.projectId) : projectContext.projectId != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        final int result = 59 * (projectId != null ? projectId.hashCode() : 0) + 397;
        return result;
    }
}
