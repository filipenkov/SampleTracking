package com.atlassian.jira.issue.search.managers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroup;

import java.util.Collection;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultIssueSearcherManager implements IssueSearcherManager
{
    private final SearchHandlerManager manager;

    public DefaultIssueSearcherManager(final SearchHandlerManager manager)
    {
        this.manager = notNull("manager", manager);
    }

    @Override
    public Collection<IssueSearcher<?>> getSearchers(com.opensymphony.user.User searcher, SearchContext context)
    {
        return getSearchers((User) searcher, context);
    }

    public Collection<IssueSearcher<?>> getSearchers(final User searcher, final SearchContext context)
    {
        return manager.getSearchers(searcher, context);
    }

    public Collection<IssueSearcher<?>> getAllSearchers()
    {
        return manager.getAllSearchers();
    }

    public Collection<SearcherGroup> getSearcherGroups(final SearchContext searchContext)
    {
        return manager.getSearcherGroups(searchContext);
    }

    public IssueSearcher<?> getSearcher(final String id)
    {
        return manager.getSearcher(id);
    }

    public void refresh()
    {
        manager.refresh();
    }
}
