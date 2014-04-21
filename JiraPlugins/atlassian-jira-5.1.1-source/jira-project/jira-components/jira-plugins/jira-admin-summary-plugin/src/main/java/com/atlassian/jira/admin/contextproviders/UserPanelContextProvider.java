package com.atlassian.jira.admin.contextproviders;

import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.web.ContextProvider;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides the Project Panel on the Admin Summary Screen
 *
 * @since v4.4
 */
public class UserPanelContextProvider extends AdminSummaryPanelContextProvider implements ContextProvider
{
    private static final Logger log = Logger.getLogger(UserPanelContextProvider.class);
    private static final String PLUGIN_SECTION = "admin_users_menu";
    private static final String SECTION_NAME = "admin.common.words.users";
    private static final String USER_COUNT_KEY = "userCount";
    private static final String GROUP_COUNT_KEY = "groupCount";
    private static final String ROLE_COUNT_KEY = "roleCount";

    private final GroupManager groupManager;
    private final UserUtil userUtil;
    private final ProjectRoleManager projectRoleManager;


    public UserPanelContextProvider(JiraAuthenticationContext authenticationContext, VelocityRequestContextFactory requestContextFactory, SimpleLinkManager linkManager, GroupManager groupManager, UserUtil userUtil, ProjectRoleManager projectRoleManager)
    {
        super(requestContextFactory, linkManager, authenticationContext);
        this.groupManager = groupManager;
        this.userUtil = userUtil;
        this.projectRoleManager = projectRoleManager;
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        List<SimpleLinkSection> pluginSections = linkManager.getSectionsForLocation(PLUGIN_SECTION, authenticationContext.getLoggedInUser(), jiraHelper);
        // Hack the sections to remove the bits we do specially on the page
        final List<SimpleLinkSection> sections = new ArrayList<SimpleLinkSection>(pluginSections.size());
        for (final SimpleLinkSection section : pluginSections)
        {
            if (!section.getId().equals("users_groups_section"))
            {
                sections.add(section);
            }
        }

        // Get the base context
        MapBuilder<String, Object> contextMap =  getContextMap(PLUGIN_SECTION, context, SECTION_NAME, sections);
        contextMap.add(BASE_URL_KEY, getBaseUrl());
        // Add User, Group and Role counts
        contextMap.add(USER_COUNT_KEY, userUtil.getTotalUserCount());
        contextMap.add(GROUP_COUNT_KEY, groupManager.getAllGroups().size());
        contextMap.add(ROLE_COUNT_KEY, projectRoleManager.getProjectRoles().size());

        return contextMap.toMap();
    }

}
