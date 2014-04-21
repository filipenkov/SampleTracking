package com.atlassian.jira.user.profile;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.avatar.MD5Util;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.GroupPermissionChecker;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.opensymphony.module.propertyset.PropertySet;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.opensymphony.util.TextUtils.htmlEncode;

/**
 * User Profile Fragment that displays the users core details
 *
 * @since v4.1
 */
public class DetailsUserProfileFragment extends AbstractUserProfileFragment
{
    private final EmailFormatter emailFormatter;
    private final GroupPermissionChecker groupPermissionChecker;
    private final PermissionManager permissionManager;
    private final CrowdService crowdService;
    private final UserPropertyManager userPropertyManager;
    private final WebResourceManager webResourceManager;
    private final AvatarManager avatarManager;
    private final UserManager userManager;
    private final AvatarService avatarService;
    private final VelocityRequestContextFactory velocityRequestContextFactory;


    public DetailsUserProfileFragment(JiraAuthenticationContext jiraAuthenticationContext,
            VelocityTemplatingEngine templatingEngine, VelocityParamFactory velocityParamFactory, EmailFormatter emailFormatter,
            GroupPermissionChecker groupPermissionChecker, PermissionManager permissionManager,
            CrowdService crowdService, UserPropertyManager userPropertyManager,
            WebResourceManager webResourceManager, AvatarManager avatarManager, UserManager userManager, VelocityRequestContextFactory velocityRequestContextFactory,
            AvatarService avatarService)
    {
        super(jiraAuthenticationContext, templatingEngine, velocityParamFactory);
        this.emailFormatter = emailFormatter;
        this.groupPermissionChecker = groupPermissionChecker;
        this.permissionManager = permissionManager;
        this.crowdService = crowdService;
        this.userPropertyManager = userPropertyManager;
        this.webResourceManager = webResourceManager;
        this.avatarManager = avatarManager;
        this.userManager = userManager;
        this.avatarService = avatarService;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    @Override
    protected Map<String, Object> createVelocityParams(User profileUser, User currentUser)
    {
        webResourceManager.requireResource("jira.webresources:avatar-picker");
        
        final Map<String, Object> params = super.createVelocityParams(profileUser, currentUser);
        params.put("context", new Context(profileUser, currentUser));

        boolean canEditAvatar = avatarService.canSetCustomUserAvatar(currentUser, profileUser.getName());
        boolean currentUserIsProfileUser = currentUser.equals(profileUser);
        params.put("user", profileUser);


        params.put("defaultAvatarId", avatarManager.getDefaultAvatarId(Avatar.Type.USER));

        final PropertySet propertySet = userPropertyManager.getPropertySet(profileUser);
        if (propertySet.exists(AvatarManager.USER_AVATAR_ID_KEY))
        {
            params.put("avatarId", propertySet.getLong(AvatarManager.USER_AVATAR_ID_KEY));
        }
        else
        {
            params.put("avatarId", avatarManager.getDefaultAvatarId(Avatar.Type.USER));
        }

        boolean hasCustomUserAvatar = avatarService.hasCustomUserAvatar(currentUser, profileUser.getName());

        if (canEditAvatar && currentUserIsProfileUser && !hasCustomUserAvatar)
        {
            // show the "add avatar" icon instead.
            params.put("avatarSrc", String.format("%s/images/icons/ico_add_avatar.png", velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl()));
        }
        else
        {
            params.put("avatarSrc", avatarService.getAvatarURL(currentUser, profileUser.getName(), Avatar.Size.LARGE));
        }

        params.put("displayEdit", displayEdit(profileUser, currentUser));
        final boolean isAdmin = isAdmin(currentUser);
        params.put("isAdmin", isAdmin);
        params.put("displayChangePassword", displayChangePassword(profileUser, currentUser));
        params.put("displayRememberMe", displayRememberMe(profileUser, currentUser));


        if (emailFormatter.emailVisible(currentUser))
        {
            params.put("email", emailFormatter.formatEmailAsLink(profileUser.getEmailAddress(), currentUser));
        }

        final List<String> groups = getGroups(profileUser, currentUser);
        if (!groups.isEmpty())
        {
            params.put("groups", groups);
        }

        params.put("userProperties", getUserProperties(profileUser, isAdmin));


        return params;
    }

    public String getBaseUrl()
    {
        return velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
    }

    public String getId()
    {
        return "details-profile-fragment";
    }

    private boolean displayEdit(User profileUser, User currentUser)
    {
        // Check if the user is read-only or editable
        if (userManager.canUpdateUser(profileUser))
        {
            // You can only update your own details from User Profile
            return profileUser.equals(currentUser);
        }
        return false;
    }

    private boolean displayChangePassword(User profileUser, User currentUser)
    {
        if (userManager.canUpdateUserPassword(profileUser))
        {
            return profileUser.equals(currentUser);
        }
        return false;
    }

    private Object displayRememberMe(final User profileUser, final User currentUser)
    {
        return profileUser.equals(currentUser);
    }

    private List<String> getGroups(final User profileUser, User currentUser)
    {
        final List<String> groups = new ArrayList<String>();

        for (String group : getGroupsForUser(profileUser.getName()))
        {
            if (groupPermissionChecker.hasViewGroupPermission(group, currentUser))
            {
                groups.add(group);
            }
        }

        return groups;
    }

    private boolean isAdmin(final User currentUser)
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, currentUser);
    }

    private Map<String, String> getUserProperties(User profileUser, boolean isAdmin)
    {
        final Map<String, String> userProperties = new HashMap<String, String>();
        if (profileUser != null && isAdmin)
        {
            final PropertySet userPropertySet = userPropertyManager.getPropertySet(profileUser);

            for (String key : (Collection<String>) userPropertySet.getKeys(PropertySet.STRING))
            {
                if (key.startsWith(UserUtil.META_PROPERTY_PREFIX))
                {
                    userProperties.put(key.substring(UserUtil.META_PROPERTY_PREFIX.length()), userPropertySet.getString(key));
                }
            }
        }
        return userProperties;
    }

    private Iterable<String> getGroupsForUser(final String userName)
    {
        final com.atlassian.crowd.search.query.membership.MembershipQuery<String> membershipQuery =
                QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(userName).returningAtMost(EntityQuery.ALL_RESULTS);

        return crowdService.search(membershipQuery);
    }

    /**
     * Helper object for use in the Velocity template.
     */
    public class Context
    {
        @Nonnull
        private final User profileUser;
        private final User currentUser;

        public Context(User profileUser, User currentUser)
        {
            this.profileUser = checkNotNull(profileUser);
            this.currentUser = currentUser;
        }

        /**
         * @return a boolean indicating whether Gravatars are enabled for this JIRA instance
         */
        public boolean isGravatarEnabled()
        {
            return avatarService.isGravatarEnabled();
        }

        /**
         * @return a boolean indicating whether to serve the Gravatar over SSL
         */
        public boolean isUseSSL()
        {
            VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();

            return requestContext != null && requestContext.getCanonicalBaseUrl().startsWith("https://");
        }

        /**
         * @return whether the current user can edit the avatar of the profile user
         */
        public boolean isCanEditAvatar()
        {
            return avatarService.canSetCustomUserAvatar(currentUser, profileUser.getName());
        }

        /**
         * @return a boolean indicating whether to display the Gravatar help text
         */
        public boolean isDisplayGravatarHelpText()
        {
            return isGravatarEnabled() && profileUser.equals(currentUser);
        }

        /**
         * @return the currently logged in user's email
         */
        private String getUserEmail()
        {
            return profileUser.equals(currentUser) ? currentUser.getEmailAddress() : "";
        }

        /**
         * @return an HTML-encoded version of the currently logged in user's email
         */
        public String getUserEmailHtml()
        {
            return htmlEncode(getUserEmail());
        }

        /**
         * @return a URL-encoded version of the currently logged in user's email
         */
        public String getUserEmailAsURL()
        {
            try
            {
                return URLEncoder.encode(getUserEmail(), "utf-8");
            }
            catch (UnsupportedEncodingException e)
            {
                // can't happen for utf-8
                return "";
            }
        }

        /**
         * @return the MD5 hash of the currently logged in user's email
         */
        public String getUserEmailMD5()
        {
            return MD5Util.md5Hex(getUserEmail());
        }
    }
}
