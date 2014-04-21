package com.atlassian.jira.mock.issue.search;

import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.search.SearchContext;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * Simple search context for testing. None of the method actually work.
 *
 * @since v4.0
 */
public class MockSearchContext implements SearchContext
{
    public boolean isForAnyProjects()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isForAnyIssueTypes()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isSingleProjectContext()
    {
        throw new UnsupportedOperationException();
    }

    public List getProjectCategoryIds()
    {
        throw new UnsupportedOperationException();
    }

    public List<Long> getProjectIds()
    {
        throw new UnsupportedOperationException();
    }

    public GenericValue getOnlyProject()
    {
        throw new UnsupportedOperationException();
    }

    public List<String> getIssueTypeIds()
    {
        throw new UnsupportedOperationException();
    }

    public List<IssueContext> getAsIssueContexts()
    {
        throw new UnsupportedOperationException();
    }

    public void verify()
    {
        throw new UnsupportedOperationException();
    }
}
