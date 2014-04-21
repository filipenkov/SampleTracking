package com.atlassian.jira.issue.watchers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Functions;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.collect.Transformed;

import java.util.Locale;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultIssueWatcherAccessor implements IssueWatcherAccessor
{
    private final WatcherManager watcherManager;

    public DefaultIssueWatcherAccessor(final WatcherManager watcherManager)
    {
        this.watcherManager = notNull("voteManager", watcherManager);
    }

    public Iterable<com.opensymphony.user.User> getDetails(final Locale displayLocale, final Issue issue)
    {
        return getDetails(displayLocale, issue, Functions.<com.opensymphony.user.User> identity());
    }

    @Override
    public Iterable<User> getWatchers(@NotNull Issue issue, @NotNull Locale displayLocale)
    {
        return watcherManager.getCurrentWatchList(issue, displayLocale);
    }

    public <T> Iterable<T> getDetails(final Locale displayLocale, final Issue issue, final Function<com.opensymphony.user.User, T> transformer)
    {
        return Transformed.collection(watcherManager.getCurrentWatchList(displayLocale, issue.getGenericValue()), transformer);
    }

    public boolean isWatchingEnabled()
    {
        return watcherManager.isWatchingEnabled();
    }

    public Iterable<String> getWatcherNames(final @NotNull Issue issue)
    {
        return watcherManager.getCurrentWatcherUsernames(issue);
    }
}
