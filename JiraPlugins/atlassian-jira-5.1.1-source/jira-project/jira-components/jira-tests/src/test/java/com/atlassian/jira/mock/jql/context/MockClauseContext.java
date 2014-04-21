package com.atlassian.jira.mock.jql.context;

import com.atlassian.jira.jql.context.ClauseContext;
import com.atlassian.jira.jql.context.ProjectIssueTypeContext;
import com.atlassian.jira.jql.context.ProjectIssueTypeContextImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * @since v4.0
 */
public class MockClauseContext implements ClauseContext
{
    private final Set<ProjectIssueTypeContext> contexts;

    public MockClauseContext()
    {
        this.contexts = new HashSet<ProjectIssueTypeContext>();
    }

    public MockClauseContext(Set<ProjectIssueTypeContext> contexts)
    {
        this.contexts = contexts;
    }

    public Set<ProjectIssueTypeContext> getContexts()
    {
        return contexts;
    }

    public boolean containsGlobalContext()
    {
        return contexts.contains(ProjectIssueTypeContextImpl.createGlobalContext());
    }
}
