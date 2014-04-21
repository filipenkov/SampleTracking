package com.atlassian.jira.plugin.issuenav;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * A condition that checks if keyboard shortcuts are enabled for the authenticated user.
 *
 * The existing condition in JIRA core doesn't work for web resources, so we have this.
 */
public class KeyboardShortcutsEnabledCondition implements Condition
{
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final UserPreferencesManager userPreferencesManager;

    public KeyboardShortcutsEnabledCondition(JiraAuthenticationContext jiraAuthenticationContext, UserPreferencesManager userPreferencesManager)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.userPreferencesManager = userPreferencesManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        User user = jiraAuthenticationContext.getLoggedInUser();
        Preferences preferences = userPreferencesManager.getPreferences(user);
        return !preferences.getBoolean(PreferenceKeys.USER_KEYBOARD_SHORTCUTS_DISABLED);
    }
}