package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.opensymphony.user.User;

/**
 * Condition to check if keyboard shortcuts are enabled.
 * <p/>
 *
 * @since v4.2
 */
public class IsKeyboardShortcutsEnabledCondition extends AbstractJiraCondition
{
    private final UserPreferencesManager userPreferencesManager;


    public IsKeyboardShortcutsEnabledCondition(final UserPreferencesManager userPreferencesManager)
    {
        this.userPreferencesManager = userPreferencesManager;
    }

    @Override
    public boolean shouldDisplay(final User user, final JiraHelper jiraHelper)
    {
        //shortcuts are enabled for anonymous users by default.
        if(user == null)
        {
            return true;
        }
        final Preferences preferences = userPreferencesManager.getPreferences(user);
        return !preferences.getBoolean(PreferenceKeys.USER_KEYBOARD_SHORTCUTS_DISABLED);
    }
}