package com.atlassian.jira.user.preferences;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import org.apache.commons.collections.map.LRUMap;

import java.util.Collections;
import java.util.Map;

/**
 * A simple implementation to cache user preferences objects.
 */
public class DefaultUserPreferencesManager implements UserPreferencesManager, Startable
{
    private Map cache;
    private final EventPublisher eventPublisher;

    public DefaultUserPreferencesManager(final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
        cache = Collections.synchronizedMap(new LRUMap(250));
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        clearCache();
    }

    @Override
    public Preferences getPreferences(com.opensymphony.user.User user)
    {
        return getPreferences((User) user);
    }

    public Preferences getPreferences(User user)
    {
        if (user == null)
            return new JiraUserPreferences((User) null);

        Preferences prefs = (Preferences) cache.get(user.getName());

        if (prefs == null)
        {
            prefs = new JiraUserPreferences(user);
            cache.put(user.getName(), prefs);
        }

        return prefs;
    }

    public void clearCache(String username)
    {
        cache.remove(username);
    }

    public void clearCache()
    {
        cache.clear();
    }
}
