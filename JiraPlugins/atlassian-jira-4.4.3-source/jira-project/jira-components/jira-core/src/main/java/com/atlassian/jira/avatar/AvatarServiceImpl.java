package com.atlassian.jira.avatar;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.opensymphony.module.propertyset.PropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public boolean isUserAvatarsEnabled()
    {
        return avatarManager.isUserAvatarsEnabled();
    }

    @Override
    public Avatar getAvatar(User remoteUser, String avatarUserId) throws AvatarsDisabledException
    {
        return getAvatarImpl(remoteUser, false, avatarUserId);
    }

    private Avatar getAvatarImpl(User remoteUser, boolean skipPermissionCheck, String avatarUserId)
    {
        if (!isUserAvatarsEnabled())
        {
            throw new AvatarsDisabledException();
        }

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
            Long defaultAvatarId = avatarManager.getDefaultAvatarId(Avatar.Type.USER);
            log.debug("Avatar not configured for user '{}', using default id {}", avatarUserId, defaultAvatarId);

            return defaultAvatarId != null ? avatarManager.getById(defaultAvatarId) : null;
        }

        Long anonAvatarId = avatarManager.getAnonymousAvatarId();
        log.debug("User '{}' does not exist, using anonymous avatar id {}", avatarUserId, anonAvatarId);

        return anonAvatarId != null ? avatarManager.getById(anonAvatarId) : null;
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

    private URI getAvatarURLImpl(User remoteUser, boolean skipPermissionCheck, String avatarUserId, Avatar.Size size)
    {
        Avatar avatar = getAvatarImpl(remoteUser, skipPermissionCheck, avatarUserId);
        String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();

        UrlBuilder urlBuilder = new UrlBuilder(baseUrl + "/secure/useravatar", applicationProperties.getEncoding(), false);


        if (size != null && !size.isDefault)
        {
            urlBuilder.addParameter("size", size.param);
        }

        String ownerId = avatar != null ? avatar.getOwner() : null;
        if (ownerId != null)
        {
            urlBuilder.addParameter("ownerId", ownerId);
        }

        // optional avatarId
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
}
