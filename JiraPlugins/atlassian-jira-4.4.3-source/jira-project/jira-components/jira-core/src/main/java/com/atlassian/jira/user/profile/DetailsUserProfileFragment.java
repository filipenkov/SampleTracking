package com.atlassian.jira.user.profile;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.GroupPermissionChecker;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.module.propertyset.PropertySet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    public DetailsUserProfileFragment(ApplicationProperties applicationProperties, JiraAuthenticationContext jiraAuthenticationContext,
            VelocityManager velocityManager, EmailFormatter emailFormatter,
            GroupPermissionChecker groupPermissionChecker, PermissionManager permissionManager,
            CrowdService crowdService, UserPropertyManager userPropertyManager,
            WebResourceManager webResourceManager, AvatarManager avatarManager, UserManager userManager)
    {
        super(applicationProperties, jiraAuthenticationContext, velocityManager);
        this.emailFormatter = emailFormatter;
        this.groupPermissionChecker = groupPermissionChecker;
        this.permissionManager = permissionManager;
        this.crowdService = crowdService;
        this.userPropertyManager = userPropertyManager;
        this.webResourceManager = webResourceManager;
        this.avatarManager = avatarManager;
        this.userManager = userManager;
    }

    @Override
    protected Map<String, Object> createVelocityParams(User profileUser, User currentUser)
    {
        webResourceManager.requireResource("jira.webresources:avatarpicker");
        
        final Map<String, Object> params = super.createVelocityParams(profileUser, currentUser);

        params.put("currentUserIsProfileUser", currentUser.equals(profileUser));
        params.put("user", profileUser);
        params.put("userAvatarEnabled", avatarManager.isUserAvatarsEnabled());
        params.put("canEditAvatar", avatarManager.hasPermissionToEdit(currentUser, Avatar.Type.USER, profileUser.getName()));
        final PropertySet propertySet = userPropertyManager.getPropertySet(profileUser);
        if (propertySet.exists(AvatarManager.USER_AVATAR_ID_KEY))
        {
            params.put("avatarId", propertySet.getLong(AvatarManager.USER_AVATAR_ID_KEY));
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

    public String getId()
    {
        return "details-profile-fragment";
    }

    private boolean displayEdit(User profileUser, User currentUser)
    {
        if (userManager.canUpdateUser(profileUser))
        {
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

        Iterable<String> groups = crowdService.search(membershipQuery);
        return groups;
    }

}
