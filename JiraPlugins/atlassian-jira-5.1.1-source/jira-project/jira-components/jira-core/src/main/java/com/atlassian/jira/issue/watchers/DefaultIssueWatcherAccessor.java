package com.atlassian.jira.issue.watchers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.NotNull;

import java.util.Locale;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultIssueWatcherAccessor implements IssueWatcherAccessor
{
    private final WatcherManager watcherManager;

    public DefaultIssueWatcherAccessor(final WatcherManager watcherManager)
    {
        this.watcherManager = notNull("voteManager", watcherManager);
    }

    @Override
    public Iterable<User> getWatchers(@NotNull Issue issue, @NotNull Locale displayLocale)
    {
        return watcherManager.getCurrentWatchList(issue, displayLocale);
    }

    @Override
    public boolean isWatchingEnabled()
    {
        return watcherManager.isWatchingEnabled();
    }

    @Override
    public Iterable<String> getWatcherNames(final @NotNull Issue issue)
    {
        return watcherManager.getCurrentWatcherUsernames(issue);
    }
}
