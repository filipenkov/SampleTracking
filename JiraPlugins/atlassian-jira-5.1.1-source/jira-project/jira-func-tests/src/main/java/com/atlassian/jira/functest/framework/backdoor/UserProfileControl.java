package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

/**
 * Use this class from func/selenium/page-object tests that need to manipulate User Profiles.
 *
 * See UserProfileBackdoor for the code this plugs into at the back-end.
 *
 * @since v5.0
 */
public class UserProfileControl extends BackdoorControl<UserProfileControl>
{
    public UserProfileControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * Changes the given user's email format to the one supplied.
     *
     * @param username the user to change the email format for
     * @param format either "html" or "text"
     */
    public void changeUserNotificationType(String username, String format)
    {
        get(createResource().path("userProfile/notificationType/set")
                .queryParam("username", username)
                .queryParam("format", format));
    }
}
