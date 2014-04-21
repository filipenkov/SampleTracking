package com.atlassian.jira.avatar;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.util.concurrent.LazyReference;
import com.opensymphony.module.propertyset.PropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.net.URI;

/**
 * Implementation of the AvatarService.
 *
 * @since v4.3
 */
public class AvatarServiceImpl implements AvatarService
{
    private final Logger log = LoggerFactory.getLogger(AvatarServiceImpl.class);
    private final UserManager userManager;
    private final AvatarManager avatarManager;
    private final UserPropertyManager userPropertyManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ApplicationProperties applicationProperties;
    private final LazyReference<Avatar.Size> defaultAvatarSize = new LazyReference<Avatar.Size>()
    {
        @Override
        protected Avatar.Size create() throws Exception
        {
            for (Avatar.Size size : Avatar.Size.values())
            {
                if (size.isDefault)
                {
                    return size;
                }
            }

            return Avatar.Size.LARGE;
        }
    };

    public AvatarServiceImpl(UserManager userManager, AvatarManager avatarManager, UserPropertyManager userPropertyManager,
            VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties)
    {
        this.userManager = userManager;
        this.avatarManager = avatarManager;
        this.userPropertyManager = userPropertyManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Avatar getAvatar(User remoteUser, String avatarUserId) throws AvatarsDisabledException
    {
        return getAvatarImpl(remoteUser, false, avatarUserId);
    }

    private Avatar getAvatarImpl(User remoteUser, boolean skipPermissionCheck, String avatarUserId)
    {
        User user = userManager.getUserObject(avatarUserId);
        if (user != null)
        {
            // try to use the configured avatar
            Long customAvatarId = configuredAvatarIdFor(user);
            if (customAvatarId != null)
            {
                Avatar avatar = avatarManager.getById(customAvatarId);
                if (avatar != null && (skipPermissionCheck || canViewAvatar(remoteUser, avatar)))
                {
                    return avatar;
                }
            }

            // fall back to the default user avatar
            Avatar defaultAvatar = getDefaultAvatar();
            log.debug("Avatar not configured for user '{}', using default id {}", avatarUserId, defaultAvatar != null ? defaultAvatar.getId() : null);

            return defaultAvatar;
        }

        Avatar anonymousAvatar = getAnonymousAvatar();
        log.debug("User '{}' does not exist, using anonymous avatar id {}", avatarUserId, anonymousAvatar != null ? anonymousAvatar.getId() : null);

        return anonymousAvatar;
    }

    @Override
    public URI getAvatarURL(User remoteUser, String avatarUserId) throws AvatarsDisabledException
    {
        return getAvatarURLImpl(remoteUser, false, avatarUserId, defaultAvatarSize.get());
    }

    @Override
    public URI getAvatarURL(User remoteUser, String avatarUserId, Avatar.Size size) throws AvatarsDisabledException
    {
        return getAvatarURLImpl(remoteUser, false, avatarUserId, size);
    }

    @Override
    public URI getAvatarUrlNoPermCheck(String avatarUserId, Avatar.Size size) throws AvatarsDisabledException
    {
        return getAvatarURLImpl(null, true, avatarUserId, size);
    }

    @Override
    public URI getAvatarAbsoluteURL(User remoteUser, String avatarUserId, Avatar.Size size)
            throws AvatarsDisabledException
    {
        return getAvatarURLImpl(remoteUser, false, avatarUserId, size, true);
    }

    private URI getAvatarURLImpl(User remoteUser, boolean skipPermissionCheck, String avatarUserId, Avatar.Size size)
    {
        return getAvatarURLImpl(remoteUser, skipPermissionCheck, avatarUserId, size, false);
    }

    private URI getAvatarURLImpl(User remoteUser, boolean skipPermissionCheck, String avatarUserId, Avatar.Size size, boolean buildAbsoluteURL)
    {
        boolean useGravatars = isGravatarEnabled();

        UrlStrategy urlStrategy = useGravatars ? new GravatarUrlStrategy() : new JiraUrlStrategy(skipPermissionCheck, buildAbsoluteURL);

        return urlStrategy.get(remoteUser, avatarUserId, size != null ? size : defaultAvatarSize.get());
    }

    @Override
    public boolean hasCustomUserAvatar(User remoteUser, String username)
    {
        User user = userManager.getUserObject(username);

        return user != null && configuredAvatarIdFor(user) != null;
    }

    @Override
    public void setCustomUserAvatar(User remoteUser, String username, Long avatarId)
            throws AvatarsDisabledException, NoPermissionException
    {
        User user = userManager.getUserObject(username);
        if (user == null)
        {
            throw new IllegalArgumentException(String.format("User '%s' does not exist", username));
        }

        if (!canSetCustomUserAvatar(remoteUser, username))
        {
            throw new NoPermissionException();
        }

        setConfiguredAvatarIdFor(user, avatarId);
    }

    @Override
    public boolean canSetCustomUserAvatar(User remoteUser, String username)
    {
        return !isGravatarEnabled() && avatarManager.hasPermissionToEdit(remoteUser, Avatar.Type.USER, username);
    }

    @Override
    public URI getProjectAvatarURL(final Project project, final Avatar.Size size)
    {
        final String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
        return getProjectAvatarURLImpl(project, size, baseUrl);
    }

    @Override
    public URI getProjectAvatarAbsoluteURL(final Project project, final Avatar.Size size)
    {
        final String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
        return getProjectAvatarURLImpl(project, size, baseUrl);
    }

    private URI getProjectAvatarURLImpl(final Project project, final Avatar.Size size, final String baseUrl)
    {
        UrlBuilder urlBuilder = new UrlBuilder(baseUrl + "/secure/projectavatar", applicationProperties.getEncoding(), false);

        if (size != null && !size.isDefault)
        {
            urlBuilder.addParameter("size", size.param);
        }

        urlBuilder.addParameter("pid", project.getId());

        // optional avatarId
        final Avatar avatar = project.getAvatar();
        Long avatarId = avatar != null ? avatar.getId() : null;
        if (avatarId != null)
        {
            urlBuilder.addParameter("avatarId", avatarId.toString());
        }

        return urlBuilder.asURI();
    }

    /**
     * Returns the avatar id that is configured for the given User. If the user has not configured an avatar, this
     * method returns null.
     *
     * @param user the user whose avatar we want
     * @return an avatar id, or null
     * @see AvatarManager#getDefaultAvatarId(com.atlassian.jira.avatar.Avatar.Type)
     * @see com.atlassian.jira.avatar.AvatarManager#getAnonymousAvatarId()
     */
    protected Long configuredAvatarIdFor(User user)
    {
        PropertySet userProperties = userPropertyManager.getPropertySet(user);
        if (userProperties.exists(AvatarManager.USER_AVATAR_ID_KEY))
        {
            long avatarId = userProperties.getLong(AvatarManager.USER_AVATAR_ID_KEY);
            log.debug("Avatar configured for user '{}' is {}", user.getName(), avatarId);

            return avatarId;
        }

        return null;
    }

    /**
     * Returns true if Gravatar support is enabled.
     *
     * @return a boolean indicating whether Gravatar support is on
     */
    @Override
    public boolean isGravatarEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_USER_AVATAR_FROM_GRAVATAR);
    }

    /**
     * Sets the given avatar id as the configured avatar id for a user.
     *
     * @param user the User whose avatar is being configured
     * @param avatarId the avatar id to configure
     */
    protected void setConfiguredAvatarIdFor(User user, Long avatarId)
    {
        PropertySet userProperties = userPropertyManager.getPropertySet(user);
        userProperties.setLong(AvatarManager.USER_AVATAR_ID_KEY, avatarId);
        log.debug("Set configured avatar id for user '{}' to {}", user.getName(), avatarId);
    }

    /**
     * Returns true if the passed in user has permission to view the passed in avatar. By definition, any user can view
     * the system avatars (e.g. avatars with no owner).
     *
     * @param user a User
     * @param avatar an Avatar
     * @return a boolean indicating whether the passed in user has permission to view the passed in avatar
     */
    protected boolean canViewAvatar(User user, Avatar avatar)
    {
        boolean hasPermission = avatarManager.hasPermissionToView(user, avatar.getAvatarType(), avatar.getOwner());
        if (!hasPermission)
        {
            log.debug("User '{}' is not allowed to view avatar {}", user, avatar.getId());
        }

        return hasPermission;
    }

    /**
     * Returns the default avatar, if configured. Otherwise returns null.
     *
     * @return the default Avatar, or null
     */
    protected Avatar getDefaultAvatar()
    {
        Long defaultAvatarId = avatarManager.getDefaultAvatarId(Avatar.Type.USER);

        return defaultAvatarId != null ? avatarManager.getById(defaultAvatarId) : null;
    }

    /**
     * Returns the anonymous avatar, if configured. Otherwise returns null.
     *
     * @return the anonymous avatar, or null
     */
    protected Avatar getAnonymousAvatar()
    {
        Long anonAvatarId = avatarManager.getAnonymousAvatarId();

        return anonAvatarId != null ? avatarManager.getById(anonAvatarId) : null;
    }

    /**
     * Builds a URI for a given JIRA avatar, with the requested size.
     *
     * @param avatar the Avatar whose URI we want
     * @param size the size in which the avatar should be displayed
     * @param absoluteUrl a boolean indicating whether to biuld an absolute URL
     * @return a URI that can be used to display the avatar
     */
    private URI buildUriForAvatar(Avatar avatar, @Nonnull Avatar.Size size, boolean absoluteUrl)
    {
        VelocityRequestContext jiraVelocityRequestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();

        String baseUrl = absoluteUrl ? jiraVelocityRequestContext.getCanonicalBaseUrl() : jiraVelocityRequestContext.getBaseUrl();
        UrlBuilder builder = new UrlBuilder(baseUrl + "/secure/useravatar", applicationProperties.getEncoding(), false);

        if (!size.isDefault)
        {
            builder.addParameter("size", size.param);
        }

        String ownerId = avatar != null ? avatar.getOwner() : null;
        if (ownerId != null)
        {
            builder.addParameter("ownerId", ownerId);
        }

        // optional avatarId
        Long avatarId = avatar != null ? avatar.getId() : null;
        if (avatarId != null)
        {
            builder.addParameter("avatarId", avatarId.toString());
        }

        return builder.asURI();
    }

    /**
     * Interface for avatar URL building strategy.
     */
    private interface UrlStrategy
    {
        URI get(User remoteUser, String username, @Nonnull Avatar.Size size);
    }

    /**
     * Build avatar URLs that point to JIRA avatars.
     */
    private class JiraUrlStrategy implements UrlStrategy
    {
        private final boolean skipPermissionCheck;
        private final boolean buildAbsoluteURL;

        public JiraUrlStrategy(boolean skipPermissionCheck, boolean buildAbsoluteURL)
        {
            this.skipPermissionCheck = skipPermissionCheck;
            this.buildAbsoluteURL = buildAbsoluteURL;
        }

        @Override
        public URI get(User remoteUser, String username, @Nonnull Avatar.Size size)
        {
            Avatar avatar = getAvatarImpl(remoteUser, skipPermissionCheck, username);

            return buildUriForAvatar(avatar, size, buildAbsoluteURL);
        }
    }

    /**
     * Build avatar URLs that point to Gravatar avatars. If the user does not exist, we serve the anonymous avatar
     * directly. If the user has not configured gravatar, we get Gravatar to redirect to the default JIRA avatar.
     */
    private class GravatarUrlStrategy implements UrlStrategy
    {
        @Override
        public URI get(User remoteUser, String username, @Nonnull Avatar.Size size)
        {
            User user = userManager.getUserObject(username);
            // JRADEV-12195: email should not be null, but we have seen it in the wild (EAC/J connected to Crowd).
            if (user != null && user.getEmailAddress() != null)
            {
                String email = user.getEmailAddress();
                String hash = MD5Util.md5Hex(email);

                URI jiraBaseUrl = URI.create(velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl());
                String gravatarBaseUrl;
                if ("https".equalsIgnoreCase(jiraBaseUrl.getScheme()))
                {
                    gravatarBaseUrl = String.format("https://secure.gravatar.com/avatar/%s", hash);
                }
                else
                {
                    gravatarBaseUrl = String.format("http://www.gravatar.com/avatar/%s", hash);
                }

                return new UrlBuilder(gravatarBaseUrl)
                        .addParameter("d", buildUriForAvatar(getDefaultAvatar(), size, true).toString())
                        .addParameter("s", size.pixels.toString())
                        .asURI();
            }

            return buildUriForAvatar(getAnonymousAvatar(), size, true);
        }
    }
}
