package com.atlassian.jira.issue.watchers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.Function;
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
     * @deprecated Please use {@link #getWatchers(com.atlassian.jira.issue.Issue, java.util.Locale)}. Since 4.3
     */
    @NotNull
    Iterable<com.opensymphony.user.User> getDetails(final @NotNull Locale displayLocale, final @NotNull Issue issue);

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

    /**
     * Return an Iterable of all watchers for the supplied issue. The elements are of the type the transformer function returns.
     *
     * @param <T> the type of element in the returned iterable.
     * @param displayLocale for sorting.
     * @param issue the issue to get the watchers for
     * @param transformer to get the required details from the {@link User} objects.
     * @return an Iterable of the user details, empty if no watchers
     * @deprecated Please use {@link #getWatchers(com.atlassian.jira.issue.Issue, java.util.Locale)}. Since 4.3
     */
    @NotNull
    <T> Iterable<T> getDetails(final @NotNull Locale displayLocale, @NotNull final Issue issue, final @NotNull Function<com.opensymphony.user.User, T> transformer);
}
