package com.atlassian.jira.issue.context;

import com.atlassian.bandana.BandanaContext;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.collect.MapBuilder;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ProjectCategoryContext extends AbstractJiraContext
{
    // ------------------------------------------------------------------------------------------------- Type Properties
    protected GenericValue projectCategory;
    private List<JiraContextNode> children;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final JiraContextTreeManager treeManager;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public ProjectCategoryContext(final GenericValue projectCategory, final JiraContextTreeManager treeManager)
    {
        this.projectCategory = projectCategory;
        this.treeManager = treeManager;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    public BandanaContext getParentContext()
    {
        return GlobalIssueContext.getInstance();
    }

    public boolean hasParentContext()
    {
        return true;
    }

    public List<JiraContextNode> getChildContexts()
    {
        if (children == null)
        {
            final GenericValue projectCategory = getProjectCategory();

            // If project category == null means that you should return projects with no categores
            final Collection<GenericValue> projects;
            if (projectCategory != null)
            {
                projects = treeManager.getProjectManager().getProjectsFromProjectCategory(projectCategory);
            }
            else
            {
                projects = treeManager.getProjectManager().getProjectsWithNoCategory();
            }

            if (projects != null)
            {
                children = new ArrayList<JiraContextNode>(projects.size());
                for (final GenericValue gv : projects)
                {
                    children.add(new ProjectContext(gv, treeManager));
                }
            }
        }
        return children;
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods
    public Map<String, Object> appendToParamsMap(final Map<String, Object> input)
    {
        return MapBuilder.newBuilder(input).add(FIELD_PROJECT_CATEGORY, getProjectCategory() != null ? getProjectCategory().getLong("id") : null).add(
            FIELD_PROJECT, null).toMap();
        //        props.put(FIELD_ISSUE_TYPE, null);
    }

    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public Project getProjectObject()
    {
        return null;
    }

    public GenericValue getProject()
    {
        return null;
    }

    public GenericValue getProjectCategory()
    {
        return projectCategory;
    }

    public IssueType getIssueTypeObject()
    {
        return null;
    }

    public GenericValue getIssueType()
    {
        return null;
    }
}
