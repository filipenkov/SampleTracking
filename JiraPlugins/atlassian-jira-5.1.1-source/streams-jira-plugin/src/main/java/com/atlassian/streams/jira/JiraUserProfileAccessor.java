package com.atlassian.streams.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.streams.api.UserProfile;
import com.atlassian.streams.spi.StreamsI18nResolver;
import com.atlassian.streams.spi.UserProfileAccessor;

import java.net.URI;

import static com.atlassian.streams.api.common.Option.*;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * JIRA implementation of building Atom author constructs
 */
public class JiraUserProfileAccessor implements UserProfileAccessor
{
    private final ApplicationProperties applicationProperties;
    private final EmailFormatter emailFormatter;
    private final JiraAuthenticationContext authenticationContext;
    private final UserUtil userUtil;
    private final UserManager userManager;
    private final AvatarService avatarService;
    private final AvatarManager avatarManager;
    private final StreamsI18nResolver i18nResolver;

    public JiraUserProfileAccessor(UserUtil userUtil,
                                   ApplicationProperties applicationProperties,
                                   EmailFormatter emailFormatter,
                                   JiraAuthenticationContext authenticationContext,
                                   UserManager userManager,
                                   AvatarService avatarService,
                                   AvatarManager avatarManager,
                                   StreamsI18nResolver i18nResolver)
    {
        this.userUtil = checkNotNull(userUtil, "userUtil");
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.emailFormatter = checkNotNull(emailFormatter, "emailFormatter");
        this.authenticationContext = checkNotNull(authenticationContext, "authenticationContext");
        this.userManager = checkNotNull(userManager, "userManager");
        this.avatarService = checkNotNull(avatarService, "avatarService");
        this.avatarManager = checkNotNull(avatarManager, "avatarManager");
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
    }

    private URI getUserProfileUri(String username)
    {
        return URI.create(applicationProperties.getBaseUrl() + userManager.getUserProfile(username).getProfilePageUri().toASCIIString());
    }

    private URI getProfilePictureUri(String username)
    {
        return avatarService.getAvatarAbsoluteURL(authenticationContext.getLoggedInUser(), username, Avatar.Size.LARGE);
    }

    public UserProfile getAnonymousUserProfile()
    {
        return new UserProfile.Builder(i18nResolver.getText("streams.jira.authors.unknown.username"))
                .fullName(i18nResolver.getText("streams.jira.authors.unknown.fullname"))
                .email(none(String.class))
                .profilePageUri(none(URI.class))
                .profilePictureUri(some(getAnonymousProfilePictureUri()))
                .build();
    }

    private URI getAnonymousProfilePictureUri()
    {
        return URI.create(applicationProperties.getBaseUrl() + "/secure/useravatar?avatarId=" + avatarManager.getAnonymousAvatarId());
    }

    public UserProfile getUserProfile(String username)
    {
        if (username == null)
        {
            return getAnonymousUserProfile();
        }

        User user = userUtil.getUserObject(username);
        String email;
        if (user != null)
        {
            if(emailFormatter.emailVisible(authenticationContext.getLoggedInUser()))
            {
                email = user.getEmailAddress();
            }
            else
            {
                email = null;
            }

            return new UserProfile.Builder(username)
                    .fullName(user.getDisplayName())
                    .email(option(email))
                    .profilePageUri(some(getUserProfileUri(username)))
                    .profilePictureUri(some(getProfilePictureUri(username)))
                    .build();
        }
        else
        {
            return new UserProfile.Builder(username).profilePictureUri(some(getAnonymousProfilePictureUri())).build();
        }
    }
}
