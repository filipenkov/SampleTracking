package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectAssigneeTypes;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;

import java.util.Map;

/**
 * Provides context for the people summary panel, in particular the project lead username,
 * default assignee and other booleans indicating if the project lead user exists, etc.
 *
 * @since v4.4
 */
public class PeopleSummaryPanelContextProvider implements CacheableContextProvider
{
    static final String CONTEXT_PROJECT_LEAD_EXISTS_KEY = "projectLeadExists";
    static final String CONTEXT_PROJECT_LEAD_KEY = "projectLeadHtml";
    static final String CONTEXT_IS_DEFAULT_ASSIGNEE_ASSIGNABLE_KEY = "defaultAssigneeAssignable";
    static final String CONTEXT_DEFAULT_ASSIGNEE_KEY = "defaultAssignee";
    static final String CONTEXT_DEFAULT_ASSIGNEE_EDITABLE = "defaultAssigneeEditable";
    static final String CONTEXT_IS_USER_AVATARS_ENABLED_KEY = "userAvatarsEnabled";
    static final String CONTEXT_LEAD_USER_AVATAR_URL_KEY = "userAvatarUrl";

    private final PermissionManager permissionManager;
    private final UserFormatManager userFormatManager;
    private final UserManager userManager;
    private final AvatarService avatarService;

    public PeopleSummaryPanelContextProvider(final PermissionManager permissionManager, final UserFormatManager userFormatManager,
            final UserManager userManager, final AvatarService avatarService)
    {
        this.permissionManager = permissionManager;
        this.userFormatManager = userFormatManager;
        this.userManager = userManager;
        this.avatarService = avatarService;
    }

    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final Project project = (Project) context.get(ContextProviderUtils.CONTEXT_PROJECT_KEY);
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

        final Map<String, Object> peopleContext = MapBuilder.<String, Object>newBuilder()
                .addAll(context)
                .add(CONTEXT_PROJECT_LEAD_EXISTS_KEY, leadUser != null)
                .add(CONTEXT_PROJECT_LEAD_KEY, leadUserString)
                .add(CONTEXT_IS_DEFAULT_ASSIGNEE_ASSIGNABLE_KEY, isDefaultAssigneeAssignable(project))
                .add(CONTEXT_DEFAULT_ASSIGNEE_KEY, getPrettyAssigneeTypeString(project.getAssigneeType()))
                .add(CONTEXT_DEFAULT_ASSIGNEE_EDITABLE, isDefaultAssigneeEditable())
                .add(CONTEXT_LEAD_USER_AVATAR_URL_KEY, userAvatarUrl)
                .toMap();

        return peopleContext;
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
