package com.atlassian.jira.user.preferences;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import java.util.concurrent.ExecutionException;

/**
 * A simple implementation to cache user preferences objects.
 */
@EventComponent
public class DefaultUserPreferencesManager implements UserPreferencesManager
{
    private final Cache<User, JiraUserPreferences> cache = CacheBuilder.newBuilder()
        .concurrencyLevel(4)
        .maximumSize(1000)
        .build(
            new CacheLoader<User, JiraUserPreferences>()
            {
                public JiraUserPreferences load(User user)
                {
                    return new JiraUserPreferences(user);
                }
            }
        );

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        clearCache();
    }

    @Override
    public Preferences getPreferences(User user)
    {
        if (user == null)
            return new JiraUserPreferences((User) null);

        try
        {
            return cache.get(user);
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void clearCache(String username)
    {
        if (username != null)
        {
            cache.invalidate(username);
        }
    }

    public void clearCache()
    {
        cache.invalidateAll();
    }
}
