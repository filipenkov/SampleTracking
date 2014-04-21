package com.atlassian.jira.user.preferences;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;

/**
 * A simple manager for retrieving, caching and updating user preferences objects
 */
public interface UserPreferencesManager
{
    /**
     * @return The user preferences for a user, or null if the user is null
     */
    Preferences getPreferences(com.opensymphony.user.User user);

    /**
     * @return The user preferences for a user, or null if the user is null
     */
    Preferences getPreferences(User user);

    /**
     * Clear any cached preferences for a given username.
     */
    void clearCache(String username);

    /**
     * Clear all cached preferences.
     */
    void clearCache();
}
