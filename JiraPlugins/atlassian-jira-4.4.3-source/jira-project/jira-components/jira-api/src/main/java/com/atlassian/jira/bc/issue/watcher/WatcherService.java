package com.atlassian.jira.bc.issue.watcher;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.lang.Pair;

import java.util.List;

/**
 * Watcher-related business logic interface.
 *
 * @since v4.2
 */
public interface WatcherService
{
    /**
     * Returns a boolean indicating whether watching is enabled in JIRA.
     *
     * @return a boolean indicating whether watching is enabled
     */
    boolean isWatchingEnabled();

    /**
     * Returns a boolean indicating whether the given user is authorised to view an issue's watcher list.
     *
     * @param issue an Issue
     * @param remoteUser a User
     * @return a boolean indicating whether the user is authorised to view the watcher list
     */
    boolean hasViewWatcherListPermission(Issue issue, com.opensymphony.user.User remoteUser);

    /**
     * Returns a boolean indicating whether the given user is authorised to view an issue's watcher list.
     *
     * @param issue an Issue
     * @param remoteUser a User
     * @return a boolean indicating whether the user is authorised to view the watcher list
     */
    boolean hasViewWatcherListPermission(Issue issue, User remoteUser);

    /**
     * Returns a the total number of watchers for a given issue in the first element of the returned Pair, and the list
     * of visible watchers in the second element of the Pair. Note that if the remote user does not have permission to
     * view the list of watchers, it is possible for the number of elements in the returned user list to be less than
     * the returned number of watchers.
     *
     * @param issue the Issue to find watchers for
     * @param remoteUser the calling User
     * @return a ServiceOutcome containing the total number of watchers, and a list of visible watchers
     * @throws WatchingDisabledException if watching is currently disabled
     * @deprecated since v4.3. Use {@link #getWatchers(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)} instead.
     */
    @Deprecated
    ServiceOutcome<Pair<Integer, List<com.opensymphony.user.User>>> getWatchers(Issue issue, com.opensymphony.user.User remoteUser)
            throws WatchingDisabledException;

    /**
     * Returns a the total number of watchers for a given issue in the first element of the returned Pair, and the list
     * of visible watchers in the second element of the Pair. Note that if the remote user does not have permission to
     * view the list of watchers, it is possible for the number of elements in the returned user list to be less than
     * the returned number of watchers.
     *
     * @param issue the Issue to find watchers for
     * @param remoteUser the calling User
     * @return a ServiceOutcome containing the total number of watchers, and a list of visible watchers
     * @throws WatchingDisabledException if watching is currently disabled
     */
    ServiceOutcome<Pair<Integer, List<User>>> getWatchers(Issue issue, User remoteUser)
            throws WatchingDisabledException;

    /**
     * Adds a watcher to an issue's list of watchers, returning the updated list of watchers.
     *
     * @param issue the issue to update
     * @param remoteUser the remote user on behalf of which
     * @param watcher the watcher to add
     * @return a ServiceOutcome containing a list of User
     * @throws WatchingDisabledException if watching is currently disabled
     * @deprecated since v4.3. Use {@link #addWatcher(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User, com.atlassian.crowd.embedded.api.User)} instead.
     */
    @Deprecated
    ServiceOutcome<List<com.opensymphony.user.User>> addWatcher(Issue issue, com.opensymphony.user.User remoteUser, com.opensymphony.user.User watcher) throws WatchingDisabledException;

    /**
     * Adds a watcher to an issue's list of watchers, returning the updated list of watchers.
     *
     * @param issue the issue to update
     * @param remoteUser the remote user on behalf of which
     * @param watcher the watcher to add
     * @return a ServiceOutcome containing a list of User
     * @throws WatchingDisabledException if watching is currently disabled
     */
    ServiceOutcome<List<User>> addWatcher(Issue issue, User remoteUser, User watcher) throws WatchingDisabledException;

    /**
     * Removes a watcher from an issue's list of watchers, returning the updated list of watchers.
     *
     * @param issue the Issue to update
     * @param remoteUser a User indicating the user on behalf of whom this operation is being performed
     * @param watcher a User representing the User to remove from the watcher list
     * @return a ServiceOutcome containing a list of User
     * @throws WatchingDisabledException if watching is currently disabled
     * @deprecated since v4.3. Use {@link #removeWatcher(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User, com.atlassian.crowd.embedded.api.User)} instead.
     */
    @Deprecated
    ServiceOutcome<List<com.opensymphony.user.User>> removeWatcher(Issue issue, com.opensymphony.user.User remoteUser, com.opensymphony.user.User watcher)
            throws WatchingDisabledException;

    /**
     * Removes a watcher from an issue's list of watchers, returning the updated list of watchers.
     *
     * @param issue the Issue to update
     * @param remoteUser a User indicating the user on behalf of whom this operation is being performed
     * @param watcher a User representing the User to remove from the watcher list
     * @return a ServiceOutcome containing a list of User
     * @throws WatchingDisabledException if watching is currently disabled
     */
    ServiceOutcome<List<User>> removeWatcher(Issue issue, User remoteUser, User watcher)
            throws WatchingDisabledException;
}
