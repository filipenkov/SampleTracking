package com.atlassian.jira.issue.vote;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.NotNull;

import java.util.Locale;

/**
 * Get all voters for an issue.
 *
 * @since v4.1
 */
public interface IssueVoterAccessor
{
    boolean isVotingEnabled();

    /**
     * Convenience function that simply returns the User names.
     *
     * @param issue the issue to get the voters for
     * @return an Iterable of the user names, empty if no voters
     */
    @NotNull
    Iterable<String> getVoterNames(final @NotNull Issue issue);

    /**
     * Convenience function that simply returns the User objects.
     *
     * @param displayLocale for sorting.
     * @param issue the issue to get the voters for
     * @return an Iterable of the users, empty if no voters
     * @deprecated Use {@link #getVoters(java.util.Locale, com.atlassian.jira.issue.Issue)}. Since 4.3
     */
    @NotNull
    Iterable<com.opensymphony.user.User> getDetails(final @NotNull Locale displayLocale, final @NotNull Issue issue);

    /**
     * Convenience function that simply returns the User objects.
     *
     * @param displayLocale for sorting.
     * @param issue the issue to get the voters for
     * @return an Iterable of the users, empty if no voters
     */
    @NotNull
    Iterable<User> getVoters(final @NotNull Locale displayLocale, final @NotNull Issue issue);

    /**
     * Return an Iterable of all voters for the supplied issue. The elements are of the type the transformer function returns.
     * 
     * @param <T> the type of element in the returned iterable.
     * @param displayLocale for sorting.
     * @param issue the issue to get the voters for
     * @param transformer to get the required details from the {@link com.opensymphony.user.User} objects.
     * @return an Iterable of the user details, empty if no voters
     * @deprecated Use {@link #getVoters(java.util.Locale, com.atlassian.jira.issue.Issue)}. Since 4.3
     */
    @NotNull
    <T> Iterable<T> getDetails(final @NotNull Locale displayLocale, @NotNull final Issue issue, final @NotNull Function<com.opensymphony.user.User, T> transformer);
}
