package com.atlassian.jira.avatar;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;

import java.net.URI;

/**
 * Service for manipulating {@link Avatar}'s.
 *
 * @since v4.3
 */
@PublicApi
public interface AvatarService
{
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
     *
     * @since v4.3
     */
    Avatar getAvatar(User remoteUser, String avatarUserId) throws AvatarsDisabledException;

    /**
     * Returns the URL for the avatar of the user having the given username for displaying on a page that should be
     * shown for the passed in remoteUser. This method returns a URL for an avatar with the default size.
     * <p/>
     * If the user does not have a custom avatar, or if the calling user does not have permission to view the Avatar,
     * this method returns the URL of the default avatar. If the user does not exist, this method returns the URL of the
     * anonymous avatar.
     *
     * @param remoteUser a User object for the currently logged in user
     * @param avatarUserId a String containing a username (may have been deleted)
     * @return a URL that can be used to display the avatar
     * @throws AvatarsDisabledException if avatars are disabled
     *
     * @see #getAvatar(com.atlassian.crowd.embedded.api.User, String)
     *
     * @since v5.0.3
     */
    URI getAvatarURL(User remoteUser, String avatarUserId) throws AvatarsDisabledException;

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
     * @since v4.3
     */
    URI getAvatarURL(User remoteUser, String avatarUserId, Avatar.Size size) throws AvatarsDisabledException;

    /**
     * This is the same as {@link #getAvatarURL(com.atlassian.crowd.embedded.api.User, String,
     * com.atlassian.jira.avatar.Avatar.Size)} but does no permission checking.
     *
     * @param avatarUserId a String containing a username (may have been deleted)
     * @param size the size of the avatar to be displayed (if null, the default size is used)
     * @return a URL that can be used to display the avatar
     * @throws AvatarsDisabledException if avatars are disabled
     *
     * @since v5.0
     */
    URI getAvatarUrlNoPermCheck(String avatarUserId, Avatar.Size size) throws AvatarsDisabledException;

    /**
     * This is the same as
     * {@link #getAvatarURL(com.atlassian.crowd.embedded.api.User, String, com.atlassian.jira.avatar.Avatar.Size)}
     * but returns an absolute URL.
     */
    URI getAvatarAbsoluteURL(User remoteUser, String avatarUserId, Avatar.Size size) throws AvatarsDisabledException;

    /**
     * Returns the URL for the avatar of the given project.
     * <p/>
     * If running in the context of a web request, this will return a URL relative to the server root (ie "/jira/...").
     * Otherwise, it will return an absolute URL (eg. "http://example.com/jira/...").
     *
     * @param project the Project of which to get the avatar URL
     * @param size the size of the avatar to be displayed (if null, the default size is used)
     * @return a URL that can be used to display the avatar
     */
    URI getProjectAvatarURL(Project project, Avatar.Size size);

    /**
     * Returns the URL for the avatar of the given project.
     * <p/>
     * This will always return an absolute URL (eg. "http://example.com/jira/...").
     *
     * @param project the Project of which to get the avatar URL
     * @param size the size of the avatar to be displayed (if null, the default size is used)
     * @return a URL that can be used to display the avatar
     */
    URI getProjectAvatarAbsoluteURL(Project project, Avatar.Size size);

    /*
     * Returns true if the user has configured a custom avatar, false otherwise.
     *
     * @param remoteUser a User object for the currently logged in user
     * @param username the username of the user whose avatar we will check
     * @return a boolean indicating whether the given user has configued a custom avatar
     *
     * @since v5.0.3
     */
    @ExperimentalApi
    boolean hasCustomUserAvatar(User remoteUser, String username);

    /**
     * Returns true if Gravatar support is enabled.
     *
     * @return a boolean indicating whether Gravatar support is on
     */
    @ExperimentalApi
    public boolean isGravatarEnabled();

    /**
     * Sets a custom avatar for a given user.
     *
     * @param remoteUser a User object for the currently logged in user
     * @param username the username of the user whose avatar we will configure
     * @param avatarId the id of the avatar to configure for the user
     * @throws AvatarsDisabledException if avatars are disabled
     * @throws NoPermissionException if the remote user does not have permission to update the given user's avatar
     *
     * @see #canSetCustomUserAvatar(com.atlassian.crowd.embedded.api.User, String)
     *
     * @since v5.0.3
     */
    @ExperimentalApi
    void setCustomUserAvatar(User remoteUser, String username, Long avatarId)
            throws AvatarsDisabledException, NoPermissionException;

    /**
     * Returns a boolean indicating whether the calling user can edit the custom user avatar for the user with the given
     * username.
     *
     * @param remoteUser a User object for the currently logged in user
     * @param username the username of the user whose avatar we will configure
     * @return a indicating whether the calling user can edit the custom user avatar for another user
     *
     * @since v5.0.3
     */
    @ExperimentalApi
    boolean canSetCustomUserAvatar(User remoteUser, String username);
}
