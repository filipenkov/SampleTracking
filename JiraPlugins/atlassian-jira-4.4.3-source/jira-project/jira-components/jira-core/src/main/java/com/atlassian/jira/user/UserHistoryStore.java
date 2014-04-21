package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.NotNull;
import com.atlassian.util.concurrent.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Store interface for {@link com.atlassian.jira.user.UserHistoryItem} objects.
 *
 * @since v4.0
 */
public interface UserHistoryStore
{
    /**
     * Add a history item to the database.  This removes the currently refered to entity (user, type, id) from the
     * list and then adds it.  If adding it causes the history items stored for that user/type to exceed the
     * max (jira.max.history.items) items allowed, it should remove the oldest items.
     *
     * @param user        The user to store the history item against
     * @param historyItem the item to store. Containing a timestamp and referenced entity
     */
    void addHistoryItem(@Nullable User user, @NotNull UserHistoryItem historyItem);

    /**
     * Retreive the history for a given user/type.
     *
     * @param type The type of entity to retreive history for.
     * @param user The user to retreive history for.
     * @return a list containing all stored history items for the passed in user/type.
     */
    @NotNull
    List<UserHistoryItem> getHistory(@NotNull UserHistoryItem.Type type, @NotNull User user);

    /**
     * Remove all history items for a given user.
     *
     * @param user The user to remove all history of.
     * @return The set of history types that were removed;
     */
    Set<UserHistoryItem.Type> removeHistoryForUser(@NotNull User user);
}
