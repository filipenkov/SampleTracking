package com.atlassian.jira.issue.watchers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Allows watching of issues. I.e.: Users watching an issue will receive
 * notifications for every update of the issue.
 */
public interface WatcherManager
{
    boolean isWatchingEnabled();

    /**
     * Determine whether the current user is already watching the issue or not
     *
     * @param user  user
     * @param issue issue being watched
     * @return True if a user is watching the issue specified.
     */
    boolean isWatching(com.opensymphony.user.User user, Issue issue);

    /**
     * Determine whether the current user is already watching the issue or not
     *
     * @param user  user
     * @param issue issue being watched
     * @return True if a user is watching the issue specified.
     */
    boolean isWatching(User user, Issue issue);

    /**
     * Determine whether the current user is already watching the issue or not
     *
     * @param user  user
     * @param issue issue being watched
     * @return True if a user is watching the issue specified.
     */
    boolean isWatching(com.opensymphony.user.User user, GenericValue issue);

    /**
     * Determine whether the current user is already watching the issue or not
     *
     * @param user  user
     * @param issue issue being watched
     * @return True if a user is watching the issue specified.
     */
    boolean isWatching(User user, GenericValue issue);

    /**
     * Retrieve collection of users that are currently watching this issue (including the current user)
     *
     * @param userLocale the locale of the user making this call, this is used for sorting the list values.
     * @param issue issue being watched
     * @return A collection of {@link User}s
     * @since v4.3
     */
    Collection<User> getCurrentWatchList(Issue issue, Locale userLocale);

    /**
     * Retrieve collection of users that are currently watching this issue (including the current user)
     *
     * @param userLocale the locale of the user making this call, this is used for sorting the list values.
     * @param issue issue being watched
     * @return A collection of {@link com.opensymphony.user.User}s
     *
     * @deprecated Use {@link #getCurrentWatchList(com.atlassian.jira.issue.Issue, java.util.Locale)} instead. Since v4.3.
     */
    Collection<com.opensymphony.user.User> getCurrentWatchList(Locale userLocale, GenericValue issue);

    /**
     * Retrieve the list of usernames of users watching the given issue
     *
     * @param issue issue being watched
     * @return the list of usernames of users watching the given issue
     * @throws DataAccessException if cannot retrieve watchers
     */
    List<String> getCurrentWatcherUsernames(Issue issue) throws DataAccessException;

    /**
     * Retrieve the list of usernames of users watching the given issue
     *
     * @param issue issue being watched
     * @return the list of usernames of users watching the given issue
     * @throws DataAccessException if cannot retrieve watchers
     */
    List<String> getCurrentWatcherUsernames(GenericValue issue) throws DataAccessException;

    /**
     * Enable watching of a particular issue for the user supplied. This means the user
     * will retrieve updates for any modifications to the issue.  Note, that this will not
     * check if a user has the BROWSE_ISSUE permission.  Notifications will however only be
     * sent to users who have the appropriate permissions.  Adding a permission check here
     * would complicate updating permission schemes a lot, as it would have to update issue's
     * watchers lists.
     *
     * @param user  user that starts watching the given issue
     * @param issue issue being watched
     */
    void startWatching(com.opensymphony.user.User user, GenericValue issue);

    /**
     * Enable watching of a particular issue for the user supplied. This means the user
     * will retrieve updates for any modifications to the issue.  Note, that this will not
     * check if a user has the BROWSE_ISSUE permission.  Notifications will however only be
     * sent to users who have the appropriate permissions.  Adding a permission check here
     * would complicate updating permission schemes a lot, as it would have to update issue's
     * watchers lists.
     *
     * @param user  user that starts watching the given issue
     * @param issue issue being watched
     */
    void startWatching(User user, GenericValue issue);

    /**
     * Disable watching of a particular issue for the user supplied.
     *
     * @param user  user that stops watching the given issue
     * @param issue issue being watched
     */
    void stopWatching(com.opensymphony.user.User user, Issue issue);

    /**
     * Disable watching of a particular issue for the user supplied.
     *
     * @param user  user that stops watching the given issue
     * @param issue issue being watched
     */
    void stopWatching(User user, Issue issue);

    /**
     * Disable watching of a particular issue for the user supplied.
     *
     * @param user  user that stops watching the given issue
     * @param issue issue being watched
     */
    void stopWatching(com.opensymphony.user.User user, GenericValue issue);

    /**
     * Disable watching of a particular issue for the user supplied.
     *
     * @param user  user that stops watching the given issue
     * @param issue issue being watched
     */
    void stopWatching(User user, GenericValue issue);

    /**
     * Disable watching of a particular issue for the user supplied.
     * <p/>
     * Note: Use this method in case when user no longer exists in JIRA, e.g.
     * JIRA uses external user management and user was removed externally.
     *
     * @param username username of the user that stops watching the given issue
     * @param issue    issue being watched
     */
    void stopWatching(String username, GenericValue issue);

    /**
     * Remove all watches for a given user
     *
     * @param user The user that has most probably been  deleted
     * @since v3.13
     */
    void removeAllWatchesForUser(com.opensymphony.user.User user);

    /**
     * Remove all watches for a given user
     *
     * @param user The user that has most probably been  deleted
     * @since v3.13
     */
    void removeAllWatchesForUser(User user);
}
