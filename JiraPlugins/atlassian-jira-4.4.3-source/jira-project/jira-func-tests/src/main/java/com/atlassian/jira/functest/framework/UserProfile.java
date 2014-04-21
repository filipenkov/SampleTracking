package com.atlassian.jira.functest.framework;

/**
 * Interface for carrying out user profile operations
 *
 * @since v3.13
 */
public interface UserProfile
{
    /**
     * Changes the current user's notification preferences.
     *
     * @param useHtml Set to True for HTML notifications, False for plain text notifications
     */
    void changeUserNotificationType(boolean useHtml);

    /**
     * Changes the user's default sharing settings.
     *
     * @param global true if default sharing should be public, false otherwise.
     */
    void changeUserSharingType(boolean global);

    /**
     * Changes the default sharing settings for users that have not configured any.
     *
     * @param global true if default sharing should be public, false otherwise.
     */
    void changeDefaultSharingType(boolean global);

    /**
     * Changes the current user's language preferences.
     *
     * @param lang the full language text to change to e.g. "German (Germany)"
     */
    void changeUserLanguage(String lang);

    /**
     * Goto the current user's profile page.
     */
    void gotoCurrentUserProfile();

    /**
     * Go to the user's profile page.
     * @param userName The user name of the other user.
     */
    void gotoUserProfile(String userName);

    /**
     * Go to the user's roadmap tab in their profile
     * @param userName the user whose roadmap you want to go to
     */
    void gotoRoadmap(String userName);

    /**
     * Changes the current user's time zone. A null time zone means that this user will use the default JIRA user time
     * zone.
     *
     *
     * @param timeZoneID@return this
     */
    UserProfile changeUserTimeZone(String timeZoneID);
}
