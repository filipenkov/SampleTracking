package com.atlassian.jira.mock.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.util.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @since v4.4
 */
public class MockUserHistoryManager implements UserHistoryManager
{

    private List<UserHistoryItem> userHistoryItems;

    public MockUserHistoryManager()
    {
        this(Collections.<UserHistoryItem>emptyList());
    }
    public MockUserHistoryManager(List<UserHistoryItem> userHistoryItems)
    {
        this.userHistoryItems = userHistoryItems;
    }

    @Override
    public void addItemToHistory(UserHistoryItem.Type type, User user, String entityId)
    {
    }

    @Override
    public void addItemToHistory(UserHistoryItem.Type type, User user, String entityId, String data)
    {
    }
    @Override
    public boolean hasHistory(UserHistoryItem.Type type, User user)
    {
        return false;
    }

    @Override
    public List<UserHistoryItem> getHistory(UserHistoryItem.Type type, User user)
    {
        return userHistoryItems;
    }

    @Override
    public void removeHistoryForUser(@NotNull User user)
    {
    }
}
