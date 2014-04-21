package com.atlassian.jira.plugin.userformat;

import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.plugin.webfragment.conditions.UserIsTheLoggedInUserCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.GroupPermissionChecker;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.component.webfragment.ContextLayoutBean;
import com.atlassian.jira.web.component.webfragment.ViewUserProfileContextLayoutBean;
import com.atlassian.jira.web.component.webfragment.WebFragmentWebComponent;
import com.google.common.collect.Maps;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Provides the full user's profile that is used to display the column on the left in the View Profile page.
 *
 * @since v3.13
 */
public class FullProfileUserFormat implements UserFormat
{
    private final EmailFormatter emailFormatter;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final GroupPermissionChecker groupPermissionChecker;
    private final WebFragmentWebComponent webFragmentWebComponent;
    private final UserUtil userUtil;
    private final UserFormatModuleDescriptor moduleDescriptor;
    private final UserPropertyManager userPropertyManager;

    public FullProfileUserFormat(final EmailFormatter emailFormatter, final PermissionManager permissionManager,
            final JiraAuthenticationContext authenticationContext, final GroupPermissionChecker groupPermissionChecker,
            final WebFragmentWebComponent webFragmentWebComponent, final UserUtil userUtil,
            final UserFormatModuleDescriptor moduleDescriptor, final UserPropertyManager userPropertyManager)
    {
        this.emailFormatter = emailFormatter;
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
        this.groupPermissionChecker = groupPermissionChecker;
        this.webFragmentWebComponent = webFragmentWebComponent;
        this.userUtil = userUtil;
        this.moduleDescriptor = moduleDescriptor;
        this.userPropertyManager = userPropertyManager;
    }

    public String format(final String username, final String id)
    {
        final Map<String, Object> params = MapBuilder.<String, Object>newBuilder()
                .add("username", username)
                .add("user", userUtil.getUser(username))
                .add("action", this)
                .add("navWebFragment", webFragmentWebComponent)
                .add("id", id)
                .toMap();

        return moduleDescriptor.getHtml(UserFormat.VIEW_TEMPLATE, params);
    }

    public String format(final String username, final String id, final Map params)
    {
        return format(username, id);
    }

    public boolean isUserLoggedinUser(final User user)
    {
        return user.equals(authenticationContext.getUser());
    }

    public boolean isEmailVisible(final User user)
    {
        return emailFormatter.emailVisible(user);
    }

    public String getDisplayEmail(final User user)
    {
        return emailFormatter.formatEmailAsLink(user.getEmailAddress(), user);
    }

    public boolean hasViewGroupPermission(final String group)
    {
        return groupPermissionChecker.hasViewGroupPermission(group, authenticationContext.getUser());
    }

    public String getUserLinks(final User profileUser, final String template)
    {
        final HttpServletRequest servletRequest = ActionContext.getRequest();
        final ContextLayoutBean userNavLayout = new ViewUserProfileContextLayoutBean(profileUser, (String) ActionContext.getValueStack().findValue(
            "/actionName"));
        // Set the context user as a request attribute so we can fish it out in the condition through the JiraHelper
        servletRequest.setAttribute(UserIsTheLoggedInUserCondition.PROFILE_USER, profileUser);
                final Map<String, Object> params = MapBuilder.<String, Object>build(UserIsTheLoggedInUserCondition.PROFILE_USER, profileUser);

        final JiraHelper helper = new JiraHelper(servletRequest, null, params);

        return webFragmentWebComponent.getHtml(template, "system.user.profile.links", helper, userNavLayout);
    }

    /**
     * These user properties are currently only visible to JIRA Administrators
     *
     * @param user The user to get properties for.
     * @return java.util.Map of user properties
     */
    public Map getUserProperties(final User user)
    {
        final Map<String, String> userProperties = Maps.newHashMap();
        if ((user != null) && isHasPermission(Permissions.ADMINISTER))
        {
            final PropertySet userPropertySet = userPropertyManager.getPropertySet(user);

            for (Object userPropertyKeyAsObject : userPropertySet.getKeys(PropertySet.STRING))
            {
                final String userPropertyKeyAsString = (String) userPropertyKeyAsObject;
                if (userPropertyKeyAsString.startsWith(UserUtil.META_PROPERTY_PREFIX))
                {
                    userProperties.put
                            (
                                    userPropertyKeyAsString.substring(UserUtil.META_PROPERTY_PREFIX.length()),
                                    userPropertySet.getString(userPropertyKeyAsString)
                            );
                }
            }
        }
        return userProperties;
    }

    public boolean isHasPermission(final int permissionsId)
    {
        return permissionManager.hasPermission(permissionsId, authenticationContext.getLoggedInUser());
    }
}
