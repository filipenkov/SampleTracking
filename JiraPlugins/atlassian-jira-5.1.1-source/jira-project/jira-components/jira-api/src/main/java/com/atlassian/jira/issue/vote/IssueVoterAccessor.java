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
     */
    @NotNull
    Iterable<User> getVoters(final @NotNull Locale displayLocale, final @NotNull Issue issue);
}
