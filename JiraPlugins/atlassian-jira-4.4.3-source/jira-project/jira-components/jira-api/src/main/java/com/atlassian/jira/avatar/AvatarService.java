package com.atlassian.jira.avatar;

import com.atlassian.crowd.embedded.api.User;

import java.net.URI;

/**
 * Service for manipulating {@link Avatar}'s.
 *
 * @since v4.3
 */
public interface AvatarService
{
    /**
     * Returns a boolean indicating whether user Avatars are enabled.
     *
     * @return true if user avatars are enabled, false otherwise
     */
    boolean isUserAvatarsEnabled();

    /**
     * Returns the Avatar for the given user, if configured. If the user does not have a custom avatar, or if the
     * calling user does not have permission to view the Avatar, this method returns the default avatar. If the user
     * does not exist, this method returns the anonymous avatar.
     * <p/>
     * If this method would return the default user avatar but none is configured, or if this method would return the
     * anonymous avatar but none is configured, this method returns null.
     *
     * @param remoteUser the User that wants to view an Avatar
     * @param avatarUserId a String containing a username (may have been deleted)
     * @return an Avatar, or null
     * @throws AvatarsDisabledException if avatars are disabled
     */
    Avatar getAvatar(User remoteUser, String avatarUserId) throws AvatarsDisabledException;

    /**
     * Returns the URL for the avatar of the user having the given username for displaying on a page that should be
     * shown for the passed in remoteUser.
     * <p/>
     * If the user does not have a custom avatar, or if the calling user does not have permission to view the Avatar,
     * this method returns the URL of the default avatar. If the user does not exist, this method returns the URL of the
     * anonymous avatar.
     *
     * @param remoteUser a User object for the currently logged in user
     * @param avatarUserId a String containing a username (may have been deleted)
     * @param size the size of the avatar to be displayed (if null, the default size is used)
     * @return a URL that can be used to display the avatar
     * @throws AvatarsDisabledException if avatars are disabled
     * @see #getAvatar(com.atlassian.crowd.embedded.api.User, String)
     */
    URI getAvatarURL(User remoteUser, String avatarUserId, Avatar.Size size) throws AvatarsDisabledException;

    /**
     * This is the same as
     * {@link #getAvatarURL(com.atlassian.crowd.embedded.api.User, String, com.atlassian.jira.avatar.Avatar.Size)}
     * but does no permission checking.
     */
    URI getAvatarUrlNoPermCheck(String avatarUserId, Avatar.Size size) throws AvatarsDisabledException;
}
