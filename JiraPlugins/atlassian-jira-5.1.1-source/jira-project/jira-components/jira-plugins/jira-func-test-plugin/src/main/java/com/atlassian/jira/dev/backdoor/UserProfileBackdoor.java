package com.atlassian.jira.dev.backdoor;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Use this backdoor to manipulate User Profiles as part of setup for tests.
 *
 * This class should only be called by the {@link com.atlassian.jira.functest.framework.backdoor.UserProfileControl}.
 *
 * @since v5.0
 */
@Path ("userProfile")
public class UserProfileBackdoor
{
    private UserPreferencesManager userPreferencesManager;
    private UserUtil userUtil;

    public UserProfileBackdoor(UserPreferencesManager userPreferencesManager, UserUtil userUtil)
    {
        this.userPreferencesManager = userPreferencesManager;
        this.userUtil = userUtil;
    }

    @GET
    @AnonymousAllowed
    @Path("notificationType/set")
    public Response addGlobalPermission(@QueryParam ("username") String username,
            @QueryParam ("format") String format)
    {
        User user = userUtil.getUser(username);
        Preferences preferences = userPreferencesManager.getPreferences(user);

        try
        {
            preferences.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, format);
        }
        catch (AtlassianCoreException e)
        {
            throw new RuntimeException(e);
        }

        // Clear any caches, to ensure they are refreshed (defensive code - see UpdateUserPreferences)
        userPreferencesManager.clearCache(username);

        return Response.ok(null).build();
    }
}
