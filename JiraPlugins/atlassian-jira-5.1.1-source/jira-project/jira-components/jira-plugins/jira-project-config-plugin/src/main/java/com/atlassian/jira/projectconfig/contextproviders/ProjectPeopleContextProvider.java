package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectAssigneeTypes;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

/**
 * Provides context for the people tab.
 *
 * @since v4.4
 */
public class ProjectPeopleContextProvider implements CacheableContextProvider
{
    static final String CONTEXT_PROJECT_LEAD_EXISTS_KEY = "projectLeadExists";
    static final String CONTEXT_PROJECT_LEAD_KEY = "projectLeadHtml";
    static final String CONTEXT_IS_DEFAULT_ASSIGNEE_ASSIGNABLE_KEY = "defaultAssigneeAssignable";
    static final String CONTEXT_DEFAULT_ASSIGNEE_KEY = "defaultAssignee";
    static final String CONTEXT_DEFAULT_ASSIGNEE_EDITABLE = "defaultAssigneeEditable";
    static final String CONTEXT_IS_USER_AVATARS_ENABLED_KEY = "userAvatarsEnabled";
    static final String CONTEXT_LEAD_USER_AVATAR_URL_KEY = "userAvatarUrl";
    static final String CONTEXT_CURRENT_USER_CAN_BROWSE_USERS = "currentUserCanBrowseUsers";

    private final PermissionManager permissionManager;
    private final UserFormatManager userFormatManager;
    private final UserManager userManager;
    private final AvatarService avatarService;
    private final ContextProviderUtils contextProviderUtils;
    private final JiraAuthenticationContext authenticationContext;

    public ProjectPeopleContextProvider(final PermissionManager permissionManager, final UserFormatManager userFormatManager,
            final UserManager userManager, final AvatarService avatarService, ContextProviderUtils contextProviderUtils, JiraAuthenticationContext authenticationContext)
    {
        this.permissionManager = permissionManager;
        this.userFormatManager = userFormatManager;
        this.userManager = userManager;
        this.avatarService = avatarService;
        this.contextProviderUtils = contextProviderUtils;
        this.authenticationContext = authenticationContext;
    }

    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final List<String> errors = Lists.newArrayList();

        MapBuilder<String, Object> contextMap = MapBuilder.<String, Object>newBuilder().addAll(context);
        final Map<String, Object> defaultContext = contextProviderUtils.getDefaultContext();
        contextMap.addAll(defaultContext);

        final Project project = (Project) defaultContext.get(ContextProviderUtils.CONTEXT_PROJECT_KEY);
        final String leadUserName = project.getLeadUserName();

        final User leadUser = getUser(leadUserName);

        final String leadUserString;
        final String userAvatarUrl;
        if(leadUser != null)
        {
            leadUserString = userFormatManager.formatUser(leadUser.getName(), "profileLink", "projectLead");
            // already built using urlBuilder
            userAvatarUrl = avatarService.getAvatarURL(leadUser, leadUser.getName(), Avatar.Size.SMALL).toString();
        }
        else
        {
            leadUserString = leadUserName;
            userAvatarUrl = null;
        }

        contextMap.add(CONTEXT_PROJECT_LEAD_EXISTS_KEY, leadUser != null);
        contextMap.add(CONTEXT_PROJECT_LEAD_KEY, leadUserString);
        contextMap.add(CONTEXT_IS_DEFAULT_ASSIGNEE_ASSIGNABLE_KEY, isDefaultAssigneeAssignable(project));
        contextMap.add(CONTEXT_DEFAULT_ASSIGNEE_KEY, getPrettyAssigneeTypeString(project.getAssigneeType()));
        contextMap.add(CONTEXT_DEFAULT_ASSIGNEE_EDITABLE, isDefaultAssigneeEditable());
        contextMap.add(CONTEXT_LEAD_USER_AVATAR_URL_KEY, userAvatarUrl);
        contextMap.add(CONTEXT_CURRENT_USER_CAN_BROWSE_USERS, permissionManager.hasPermission(Permissions.USER_PICKER, authenticationContext.getLoggedInUser()));

        return contextMap.toMap();
    }

    private boolean isDefaultAssigneeEditable()
    {
        return ProjectAssigneeTypes.getAssigneeTypes().size() > 1;
    }

    private boolean isDefaultAssigneeAssignable(final Project project)
    {
        final Long assigneeType = project.getAssigneeType();
        if ((assigneeType != null) && (ProjectAssigneeTypes.PROJECT_LEAD == assigneeType))
        {
            final User projectLead = getUser(project.getLeadUserName());
            if(projectLead == null)
            {
                return false;
            }
            return permissionManager.hasPermission(Permissions.ASSIGNABLE_USER, project, projectLead);
        }
        else
        {
            return true;
        }
    }

    private User getUser(final String username)
    {
        return userManager.getUserObject(username);
    }

    String getPrettyAssigneeTypeString(final Long assigneeType)
    {
        return ProjectAssigneeTypes.getPrettyAssigneeType(assigneeType);
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }
}
