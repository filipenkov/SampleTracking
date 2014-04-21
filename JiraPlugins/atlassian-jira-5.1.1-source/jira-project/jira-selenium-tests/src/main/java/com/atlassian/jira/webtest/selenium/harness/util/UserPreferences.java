package com.atlassian.jira.webtest.selenium.harness.util;

/**
 * There must be a user already logged in. You can then change various preferences for that user.
 *
 * @since v4.2
 */
public interface UserPreferences
{
    /**
     * Enable and disable the keyboard shortcuts (aka commands) for the logged in user
     *
     *
     * @param enabled - if the keyboard shortcuts should be enabled or not.
     */
    void setKeyboardShortcutsEnabled(boolean enabled);

    /**
     * Set the locale preference for the current user
     * @param locale the locale e.g. "de_DE". Use "-1" for the default language.
     */
    void setLanguage(final String locale);
}
