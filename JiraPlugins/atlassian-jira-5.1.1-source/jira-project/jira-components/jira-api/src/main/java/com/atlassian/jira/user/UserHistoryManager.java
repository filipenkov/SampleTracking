package com.atlassian.jira.user;

import com.atlassian.jira.util.NotNull;
import com.atlassian.crowd.embedded.api.User;

import java.util.List;

/**
 * The manager responsible for storing and retreiving {@link com.atlassian.jira.user.UserHistoryItem} objects.
 * Although it is possible to store a reference to any {@link com.atlassian.jira.user.UserHistoryItem.Type} it has
 * special methods for Issue history as that is the only use internal to JIRA.
 *
 * @since v4.0
 */
public interface UserHistoryManager
{
    /**
     * Create and add an {@link com.atlassian.jira.user.UserHistoryItem} to the Users history list.
     * A null users history should still be stored, even if only for duration of session.
     *
     * @param type     The type queue to add the history item to
     * @param user     The user to add the history item to
     * @param entityId The entity id of the entity to add to the history queue.
     */
    void addItemToHistory(UserHistoryItem.Type type, User user, String entityId);

    /*
     * Create and add an {@link com.atlassian.jira.user.UserHistoryItem} to the Users history list.
     * Allows to store data related to the user history item.
     *
     * @param type      The type queue to add the history item to
     * @param user      The user to add the history item to
     * @param entityId  The entity id of the entity to add to the history queue
     * @param data      Data related to the history item. Can be null.
     */
    void addItemToHistory(UserHistoryItem.Type type, User user, String entityId, String data);

    /**
     * Determines whether a user has any items in their history for a given {@link com.atlassian.jira.user.UserHistoryItem.Type}
     * This method performs no permission checks.
     *
     * @param type The type to check for
     * @param user The user to check for.
     * @return true if the user has any entities in their queue of the give type, false otherwise
     */
    boolean hasHistory(UserHistoryItem.Type type, User user);

    /**
     * Retreive the user's history queue for the given {@link com.atlassian.jira.user.UserHistoryItem.Type}.
     * The list is returned ordered by DESC lastViewed date (i.e. newest is first).
     * This method performs no permission checks.
     *
     * @param type The type of entity to get the history for
     * @param user The user to get the history items for.
     * @return a list of history items sort by desc lastViewed date.
     */
    @NotNull
    List<UserHistoryItem> getHistory(UserHistoryItem.Type type, User user);

    /**
     * Remove the user's history.
     *
     * @param user The User to remove the history for.
     */
    void removeHistoryForUser(@NotNull User user);

}
