package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.NotNull;

import java.util.List;

/**
 * A wrapper of the {@link com.atlassian.jira.user.UserHistoryManager} that allows you to deal directly with Issue objects
 *
 * @since v4.0
 */
public interface UserIssueHistoryManager
{
    public static int DEFAULT_ISSUE_HISTORY_DROPDOWN_ITEMS = 6;

    /**
     * Add an {@link Issue} to the user hsitory list.
     * A null users history should still be stored, even if only for duration of session
     *
     * @param user  The user to add the history item to
     * @param issue The issue to add to the history list
     */
    void addIssueToHistory(User user, Issue issue);

    /**
     * Determines whether the user has a current issue history.
     * This method also performs permission checks against issue to ensure that user can see atleast 1 issue.
     *
     * @param user The user to check for.
     * @return true if the user has at least 1 issue in their issue history queue that they can see, false otherwise
     */
    boolean hasIssueHistory(User user);

    /**
     * Retreive the user's issue history queue.
     * The list is returned ordered by DESC lastViewed date (i.e. newest is first).
     * This method performs no permission checks.
     *
     * @param user The user to get the history issue items for.
     * @return a list of history issue items sort by desc lastViewed date.
     */
    @NotNull
    List<UserHistoryItem> getFullIssueHistoryWithoutPermissionChecks(User user);

    /**
     * Retreive the user's issue history queue.
     * The list is returned ordered by DESC lastViewed date (i.e. newest is first).
     * This method performs permission checks.
     *
     * @param user The user to get the history issue items for.
     * @return a list of history issue items sort by desc lastViewed date.
     */
    @NotNull
    List<UserHistoryItem> getFullIssueHistoryWithPermissionChecks(User user);

    /**
     * Retreive the first X (jira.max.issue.history.dropdown.items)  Issues from the user's issue history queue.
     * This purpose of this method is to return a small list for easy display.  The filtering could be
     * done in the view code, but for performance reasons, is done here.
     * The list is returned ordered by DESC lastViewed date (i.e. newest is first).
     * This method performs permission checks.
     *
     * @param user The user to get the history issue items for.
     * @return a list of issue items sorted by desc lastViewed date.
     */
    @NotNull
    List<Issue> getShortIssueHistory(User user);
}
