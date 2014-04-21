package com.atlassian.jira.issue.watchers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.statistics.util.DocumentHitCollector;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.Transformed;
import com.atlassian.query.Query;
import org.apache.lucene.document.Document;

import static com.atlassian.jira.jql.builder.JqlQueryBuilder.newBuilder;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultWatchedIssuesAccessor implements WatchedIssuesAccessor
{
    private final WatcherManager watcherManager;
    private final SearchProvider searchProvider;
    private final SearchProviderFactory factory;

    public DefaultWatchedIssuesAccessor(final @NotNull WatcherManager watcherManager, final @NotNull SearchProvider searchProvider, final @NotNull SearchProviderFactory factory)
    {
        this.watcherManager = notNull("watcherManager", watcherManager);
        this.searchProvider = notNull("searchProvider", searchProvider);
        this.factory = notNull("factory", factory);
    }

    public Iterable<Long> getWatchedIssueIds(final User watcher, final User searcher, final Security security)
    {
        final IssueIdCollector collector = new IssueIdCollector();
        final Query query = getQuery(watcher);
        try
        {
            switch (security)
            {
                case OVERRIDE:
                    searchProvider.searchOverrideSecurity(query, searcher, collector);
                    break;

                case RESPECT:
                    searchProvider.search(query, searcher, collector);
                    break;
            }
        }
        catch (final SearchException e)
        {
            throw new RuntimeException(e);
        }
        return collector.getIds();
    }

    public Iterable<Long> getWatchedIssueIds(final com.opensymphony.user.User watcher, final com.opensymphony.user.User searcher, final Security security)
    {
        return getWatchedIssueIds((User) watcher, (User) searcher, security);
    }

    public boolean isWatchingEnabled()
    {
        return watcherManager.isWatchingEnabled();
    }

    static Query getQuery(final User user)
    {
        return newBuilder().where().watcherUser(user.getName()).endWhere().buildQuery();
    }

    private class IssueIdCollector extends DocumentHitCollector
    {
        private final CollectionBuilder<String> issueIds = CollectionBuilder.newBuilder();

        public IssueIdCollector()
        {
            super(factory.getSearcher(SearchProviderFactory.ISSUE_INDEX));
        }

        @Override
        public void collect(final Document d)
        {
            issueIds.add(d.get(DocumentConstants.ISSUE_ID));
        }

        Iterable<Long> getIds()
        {
            return Transformed.list(issueIds.asList(), new Function<String, Long>()
            {
                public Long get(final String input)
                {
                    return Long.valueOf(input);
                }
            });
        }
    }
}
