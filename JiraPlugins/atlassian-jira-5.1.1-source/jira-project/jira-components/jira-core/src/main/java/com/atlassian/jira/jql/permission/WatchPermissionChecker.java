package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.watchers.WatcherManager;

/**
 * Checks if watching is enabled.
 *
 * @since v4.1
 */
public class WatchPermissionChecker implements ClausePermissionChecker
{
    private final WatcherManager watcherManager;

    public WatchPermissionChecker(final WatcherManager watcherManager)
    {
        this.watcherManager = watcherManager;
    }

    public boolean hasPermissionToUseClause(final User user)
    {
        return watcherManager.isWatchingEnabled();
    }
}
