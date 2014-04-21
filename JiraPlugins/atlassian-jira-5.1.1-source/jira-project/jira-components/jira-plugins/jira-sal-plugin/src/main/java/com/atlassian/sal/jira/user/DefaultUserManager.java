package com.atlassian.sal.jira.user;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.sal.api.user.UserResolutionException;
import org.apache.commons.lang.StringUtils;
import webwork.util.URLCodec;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;

/**
 * User operations
 */
public class DefaultUserManager implements UserManager
{
    private final GlobalPermissionManager globalPermissionManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final CrowdService crowdService;
    private final AvatarService avatarService;

    public DefaultUserManager(final GlobalPermissionManager globalPermissionManager,
            final JiraAuthenticationContext jiraAuthenticationContext, final CrowdService crowdService,
            AvatarService avatarService)
    {
        this.globalPermissionManager = globalPermissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.crowdService = crowdService;
        this.avatarService = avatarService;
    }

    public String getRemoteUsername()
    {
        final User user = jiraAuthenticationContext.getLoggedInUser();
        if (user != null)
        {
            return user.getName();
        }
        return null;
    }

    public String getRemoteUsername(final HttpServletRequest request)
    {
        return getRemoteUsername();
    }


    public boolean isSystemAdmin(final String username)
    {
        if (StringUtils.isNotEmpty(username))
        {
            final User user = crowdService.getUser(username);
            return user != null && globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
        }
        return false;
    }

    public boolean isAdmin(final String username)
    {
        if (StringUtils.isNotEmpty(username))
        {
            final User user = crowdService.getUser(username);
            return user != null && globalPermissionManager.hasPermission(Permissions.ADMINISTER, user);
        }
        return false;
    }

    public boolean authenticate(final String username, final String password)
    {
        try
        {
            return crowdService.authenticate(username, password) != null;
        }
        catch (FailedAuthenticationException e)
        {
            return false;
        }
    }

    public Principal resolve(final String username) throws UserResolutionException
    {
        return crowdService.getUser(username);
    }

    /**
     * Returns whether the user is in the specify group
     *
     * @param username The username to check
     * @param groupName The group to check
     * @return True if the user is in the specified group
     */
    public boolean isUserInGroup(final String username, final String groupName)
    {
        final User user = crowdService.getUser(username);
        final Group group = crowdService.getGroup(groupName);


        return user != null && group != null && crowdService.isUserMemberOfGroup(user, group);
    }


    @Override
    public UserProfile getUserProfile(String username)
    {
        final User user = crowdService.getUser(username);
        if (user != null)
        {
            return new JiraUserProfile(user);
        }
        else
        {
            return null;
        }
    }

    class JiraUserProfile implements UserProfile
    {
        private final User user;

        JiraUserProfile(final User user)
        {
            this.user = user;
        }

        @Override
        public String getUsername()
        {
            return user.getName();
        }

        @Override
        public String getFullName()
        {
            return user.getDisplayName();
        }

        @Override
        public String getEmail()
        {
            return user.getEmailAddress();
        }

        @Override
        public URI getProfilePictureUri(int width, int height)
        {
            if (width > AvatarManager.ImageSize.LARGE.getPixels()
                    || height > AvatarManager.ImageSize.LARGE.getPixels())
            {
                return null;
            }
            else
            {
                return getProfilePictureUri();
            }
        }

        @Override
        public URI getProfilePictureUri()
        {
            User remoteUser = jiraAuthenticationContext.getLoggedInUser();
            return avatarService.getAvatarURL(remoteUser, user.getName(), Avatar.Size.LARGE);
        }

        @Override
        public URI getProfilePageUri()
        {
            final String username = getUsername();
            if (username == null)
            {
                return null;
            }

            try
            {
                return new URI(String.format("/secure/ViewProfile.jspa?name=%s", URLCodec.encode(username, "UTF-8")));
            }
            catch (URISyntaxException e)
            {
                return null;
            }
            catch (UnsupportedEncodingException e)
            {
                return null;
            }
        }
    }
}
