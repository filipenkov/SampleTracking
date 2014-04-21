package com.atlassian.jira.issue.watchers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.NotNull;

/**
 * Get all issue ids someone is watching.
 *
 * @since v4.1
 */
public interface WatchedIssuesAccessor
{
    enum Security
    {
        /**
         * Only return issue ids the user can see.
         */
        RESPECT,

        /**
         * return all issues the user
         */
        OVERRIDE;
    }

    boolean isWatchingEnabled();

    /**
     * Get the issues a particular user is watching.
     *
     * @param watcher the user whose watches we are searching for.
     * @param searcher the user who is searching for the watched issues.
     * @param security whether to respect or override security.
     * @return the ids of the found issues.
     */
    @NotNull
    Iterable<Long> getWatchedIssueIds(@NotNull User watcher, @NotNull User searcher, @NotNull Security security);

    /**
     * Get the issues a particular user is watching.
     *
     * @param watcher the user whose watches we are searching for.
     * @param searcher the user who is searching for the watched issues.
     * @param security whether to respect or override security.
     * @return the ids of the found issues.
     *
     * @deprecated use {@link #getWatchedIssueIds(com.atlassian.crowd.embedded.api.User, com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.watchers.WatchedIssuesAccessor.Security)}. Since v4.3
     */
    @NotNull
    Iterable<Long> getWatchedIssueIds(@NotNull com.opensymphony.user.User watcher, @NotNull com.opensymphony.user.User searcher, @NotNull Security security);
}
