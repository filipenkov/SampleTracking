package com.atlassian.jira.admin.contextproviders;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.plugin.PluginParseException;
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
public class ProjectPanelContextProvider  extends AdminSummaryPanelContextProvider implements ContextProvider
{
    public static final int MAX_RECENT_PROJECTS_TO_SHOW = 5;

    private static final Logger log = Logger.getLogger(ProjectPanelContextProvider.class);
    private static final String RECENT_PROJECTS_KEY = "recentProjects";
    private static final String PROJECT_COUNT_KEY = "projectCount";
    private static final String DEFAULT_AVATAR_ID = "defaultAvatarUrl";
    private static final String PLUGIN_SECTION = "admin_project_menu";
    private static final String SECTION_NAME = "common.concepts.projects";

    private final UserProjectHistoryManager userProjectHistoryManager;
    private final ProjectService projectService;
    private final SearchProvider searchProvider;
    private final AvatarManager avatarManager;
    private static final String GETTING_STARTED = "gettingStartedUrl";
    private static final String PROJECT_URL = "projectIntroUrl";

    public ProjectPanelContextProvider(UserProjectHistoryManager userProjectHistoryManager, JiraAuthenticationContext authenticationContext, ProjectService projectManager,
            VelocityRequestContextFactory requestContextFactory, SearchProvider searchProvider, SimpleLinkManager linkManager, AvatarManager avatarManager)
    {
        super(requestContextFactory, linkManager, authenticationContext);
        this.userProjectHistoryManager = userProjectHistoryManager;
        this.projectService = projectManager;
        this.searchProvider = searchProvider;
        this.avatarManager = avatarManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        List<SimpleLinkSection> pluginSections = linkManager.getSectionsForLocation(PLUGIN_SECTION, authenticationContext.getLoggedInUser(), jiraHelper);
        // Hack the sections to remove the bits we do specially on the page
        final List<SimpleLinkSection> sections = new ArrayList<SimpleLinkSection>(pluginSections.size());
        for (final SimpleLinkSection section : pluginSections)
        {
            if (!section.getId().equals("recent_project_section") && !section.getId().equals("project_section") && !section.getId().equals("add_project_section"))
            {
                sections.add(section);
            }
        }

        // Get the base context
        MapBuilder<String, Object> contextMap =  getContextMap(PLUGIN_SECTION, context, SECTION_NAME, sections);

        // Add the bits we need to build the recent projects panel
        List<Project> recentProjects = userProjectHistoryManager.getProjectHistoryWithPermissionChecks(ProjectAction.EDIT_PROJECT_CONFIG, authenticationContext.getLoggedInUser());

        recentProjects = recentProjects.subList(0, Math.min(MAX_RECENT_PROJECTS_TO_SHOW, recentProjects.size()));

        List<SimpleProject> simpleProjectList = new ArrayList<SimpleProject>();
        for (Project project : recentProjects)
        {
            simpleProjectList.add(new SimpleProject(project));
        }
        contextMap.add(BASE_URL_KEY, getBaseUrl());
        contextMap.add(RECENT_PROJECTS_KEY, simpleProjectList);
        List<Project> allProjects = projectService.getAllProjectsForAction(authenticationContext.getLoggedInUser(), ProjectAction.EDIT_PROJECT_CONFIG).getReturnedValue();
        contextMap.add(PROJECT_COUNT_KEY, allProjects.size());

        Long avatarId = avatarManager.getDefaultAvatarId(Avatar.Type.PROJECT);
        contextMap.add(DEFAULT_AVATAR_ID, getBaseUrl() + "/secure/projectavatar?avatarId=" + avatarId + "&size=large");
        HelpUtil helpUtil = HelpUtil.getInstance();
        HelpUtil.HelpPath path = helpUtil.getHelpPath("getting_started");
        final String gettingStartedLink = String.format("<a alt=\"%s\" title=\"%s\" href=\"%s\">", path.getAlt(), path.getTitle(), path.getUrl());
        contextMap.add(GETTING_STARTED, gettingStartedLink);
        contextMap.add(PROJECT_URL, helpUtil.getHelpPath("admin_summary_add_project").getUrl());

        return contextMap.toMap();
    }


    public class SimpleProject
    {
        private final String key;
        private final String name;
        private final String url;
        private final String iconUrl;
        private final long issueCount;

        public SimpleProject(Project project)
        {
            this.key = project.getKey();
            this.name = project.getName();
            final Avatar avatar = project.getAvatar();
            this.iconUrl = getBaseUrl() + "/secure/projectavatar?pid=" + project.getId() + "&avatarId=" + avatar.getId() + "&size=large";
            this.url = getBaseUrl() + "/plugins/servlet/project-config/" + project.getKey();
            this.issueCount = getProjectIssueCount(project);
        }

        public String getKey()
        {
            return key;
        }

        public String getName()
        {
            return name;
        }

        public long getIssueCount()
        {
            return issueCount;
        }

        public String getIconUrl()
        {
            return iconUrl;
        }

        public String getUrl()
        {
            return url;
        }

        private long getProjectIssueCount(Project project)
        {
            final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
            builder.where().project(project.getKey());
            try
            {
                return searchProvider.searchCountOverrideSecurity(builder.buildQuery(), authenticationContext.getLoggedInUser());
            }
            catch (SearchException e)
            {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        
    }


}
