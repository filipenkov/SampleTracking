package com.atlassian.jira.quickedit.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.quickedit.rest.api.UserPreferences;

/**
 * Store responsible for retrieving and saving which fields a particular user has chosen to use for quick create or
 * quick edit.
 *
 * @since v5.0
 */
public interface UserPreferencesStore
{
    static final String QUICK_EDIT_FIELDS = "jira.quick.edit.fields";
    static final String QUICK_CREATE_FIELDS = "jira.quick.create.fields";
    static final String SHOW_WELCOME_SCREEN_KEY = "jira.quick.edit.show.welcome.screen";
    static final String USE_QUICK_EDIT_KEY = "jira.quick.edit.use";
    static final String USE_QUICK_CREATE_KEY = "jira.quick.create.use";

    /**
     * Given a user this method returns {@link UserPreferences} used to display this user's quick edit screen
     *
     * @param loggedInUser User for whom to retrieve list of fields
     * @return A list of field ids to display. Empty if none have been set yet.
     */
    UserPreferences getEditUserPreferences(final User loggedInUser);

    /**
     * Given a user this method stores {@link UserPreferences} used to display this user's quick edit screen
     *
     * @param loggedInUser User for whom to store list of fields
     * @param prefs User preferences for quick edit/create to save
     */
    void storeEditUserPreferences(final User loggedInUser, final UserPreferences prefs);

    /**
     * Given a user this method returns {@link UserPreferences} used to display this user's quick create screen
     *
     * @param loggedInUser User for whom to retrieve list of fields
     * @return A list of field ids to display. Empty if none have been set yet.
     */
    UserPreferences getCreateUserPreferences(final User loggedInUser);

    /**
     * Given a user this method stores {@link UserPreferences} used to display this user's quick create screen
     *
     * @param loggedInUser User for whom to store list of fields
     * @param prefs User preferences for quick edit/create to save
     */
    void storeCreateUserPreferences(final User loggedInUser, final UserPreferences prefs);

}
