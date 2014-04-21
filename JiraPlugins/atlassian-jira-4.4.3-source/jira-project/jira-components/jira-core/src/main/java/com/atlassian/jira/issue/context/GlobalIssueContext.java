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

public class GlobalIssueContext extends AbstractJiraContext
{
    public static final String GLOBAL_CONTEXT_STR = "Global Context";

    private List<JiraContextNode> children = null;

    private final JiraContextTreeManager treeManager;

    public GlobalIssueContext(final JiraContextTreeManager treeManager)
    {
        this.treeManager = treeManager;
    }

    public Map<String, Object> appendToParamsMap(final Map<String, Object> input)
    {
        return MapBuilder.newBuilder(input).add(FIELD_PROJECT_CATEGORY, null).add(FIELD_PROJECT, null).toMap();
        // props.put(FIELD_ISSUE_TYPE, null);
    }

    public IssueType getIssueTypeObject()
    {
        return null;
    }

    public GenericValue getIssueType()
    {
        return null;
    }

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
        return null;
    }

    @Override
    public boolean isInContext(final IssueContext issueContext)
    {
        return true;
    }

    public BandanaContext getParentContext()
    {
        return null;
    }

    public boolean hasParentContext()
    {
        return false;
    }

    public static JiraContextNode getInstance()
    {
        return JiraContextTreeManager.getRootContext();
    }

    @Override
    public String toString()
    {
        return GLOBAL_CONTEXT_STR;
    }

    public List<JiraContextNode> getChildContexts()
    {
        if (children == null)
        {
            final Collection<GenericValue> projectCategories = treeManager.getProjectManager().getProjectCategories();
            if (projectCategories != null)
            {
                children = new ArrayList<JiraContextNode>(projectCategories.size() + 1);
                for (final GenericValue projectCategory : projectCategories)
                {
                    children.add(new ProjectCategoryContext(projectCategory, treeManager));
                }
                children.add(new ProjectCategoryContext(null, treeManager));
            }
        }
        return children;
    }
}
