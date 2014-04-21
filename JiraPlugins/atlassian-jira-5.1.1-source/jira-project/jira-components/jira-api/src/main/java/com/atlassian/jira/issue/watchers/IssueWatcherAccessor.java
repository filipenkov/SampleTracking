package com.atlassian.jira.issue.watchers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.NotNull;

import java.util.Locale;

/**
 * Get all watchers for an issue.
 *
 * @since v4.1
 */
public interface IssueWatcherAccessor
{
    boolean isWatchingEnabled();

    /**
     * Convenience function that simply returns the User names.
     * 
     * @param issue the issue to get the watchers for
     * @return an Iterable of the user names, empty if no watchers
     */
    @NotNull
    Iterable<String> getWatcherNames(final @NotNull Issue issue);

    /**
     * Convenience function that simply returns the User objects.
     *
     * @param displayLocale for sorting.
     * @param issue the issue to get the watchers for
     * @return an Iterable of the users, empty if no watchers
     * @since v4.3
     */
    @NotNull
    Iterable<User> getWatchers(final @NotNull Issue issue, final @NotNull Locale displayLocale);
}
