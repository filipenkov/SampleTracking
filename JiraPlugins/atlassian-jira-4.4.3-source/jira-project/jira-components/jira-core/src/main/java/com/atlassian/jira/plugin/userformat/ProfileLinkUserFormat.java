package com.atlassian.jira.plugin.userformat;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.collect.MapBuilder;

import java.util.Map;

/**
 * Very simple implementation that only renders the users full name with a link to the user's profile page. If the
 * username is null, it will display 'Anonymous'.  If no user matching the username can be found, ony the username will
 * be printed.
 *
 * @since v3.13
 */
public class ProfileLinkUserFormat implements UserFormat
{
    public static final String TYPE = "profileLink";

    private final UserFormatModuleDescriptor moduleDescriptor;
    private final UserUtil userUtil;
    private final AvatarService avatarService;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public ProfileLinkUserFormat(final UserFormatModuleDescriptor moduleDescriptor, final UserUtil userUtil,
            final AvatarService avatarService, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.moduleDescriptor = moduleDescriptor;
        this.userUtil = userUtil;
        this.avatarService = avatarService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public String format(final String username, final String id)
    {
        final Map<String, Object> params = getInitialParams(username, id);
        return moduleDescriptor.getHtml(VIEW_TEMPLATE, params);
    }

    public String format(final String username, final String id, final Map<String, Object> params)
    {
        final Map<String, Object> velocityParams = getInitialParams(username, id);
        velocityParams.putAll(params);

        return moduleDescriptor.getHtml(VIEW_TEMPLATE, velocityParams);
    }

    private Map<String, Object> getInitialParams(final String username, final String id)
    {
        final User user = userUtil.getUserObject(username);
        final String fullName = user == null ? username : userUtil.getDisplayableNameSafely(user);
        final boolean userAvatarEnabled = avatarService.isUserAvatarsEnabled();
        final String avatarURL = avatarService.getAvatarURL(jiraAuthenticationContext.getLoggedInUser(), username, Avatar.Size.SMALL).toString();

        return MapBuilder.<String, Object>newBuilder().
                add("username", username).
                add("user", user).
                add("fullname", fullName).
                add("id", id).
                add("userAvatarEnabled", userAvatarEnabled).
                add("avatarURL", avatarURL).
                toMutableMap();
    }
}
