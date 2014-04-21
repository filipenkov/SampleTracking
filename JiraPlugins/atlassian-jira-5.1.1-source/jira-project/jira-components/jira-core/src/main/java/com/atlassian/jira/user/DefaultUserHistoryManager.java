package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.NotNull;
import com.atlassian.util.concurrent.Nullable;
import net.jcip.annotations.ThreadSafe;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of the UserHistoryManager.
 *
 * @since v4.0
 */
@ThreadSafe
public class DefaultUserHistoryManager implements UserHistoryManager
{
    private final UserHistoryStore store;

    public DefaultUserHistoryManager(@NotNull final UserHistoryStore store)
    {
        this.store = notNull("store", store);
    }

    public void addItemToHistory(@NotNull final UserHistoryItem.Type type, @NotNull final User user, @NotNull final String entityId)
    {
        addItemToHistory(type, user, entityId, null);
    }

    public void addItemToHistory(@NotNull final UserHistoryItem.Type type, @Nullable final User user, @NotNull final String entityId, @Nullable final String data)
    {
        notNull("type", type);
        notNull("entityId", entityId);
        store.addHistoryItem(ApplicationUsers.from(user), new UserHistoryItem(type, entityId, data));
    }

    public boolean hasHistory(@NotNull final UserHistoryItem.Type type, @NotNull final User user)
    {
        notNull("type", type);
        notNull("user", user);
        final List<UserHistoryItem> history = store.getHistory(type, ApplicationUsers.from(user));
        return (history != null) && !history.isEmpty();
    }

    @NotNull
    public List<UserHistoryItem> getHistory(@NotNull final UserHistoryItem.Type type, @Nullable final User user)
    {
        notNull("type", type);
        return store.getHistory(type, ApplicationUsers.from(user));
    }

    public void removeHistoryForUser(@NotNull final User user)
    {
        notNull("user", user);
        store.removeHistoryForUser(ApplicationUsers.from(user));
    }

}
