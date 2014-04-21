package com.atlassian.jira.web.sitemesh;

import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.web.component.webfragment.AdminTabsWebComponent;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.model.WebPanel;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Class that holds all the logic for the projectconfig decorator.
 *
 * @since v4.4
 */
public class AdminDecoratorHelper
{
    public static final String ACTIVE_SECTION_KEY = "admin.active.section";
    public static final String ACTIVE_TAB_LINK_KEY = "admin.active.tab";


    private final WebInterfaceManager webInterfaceManager;
    private final ProjectService service;
    private final JiraAuthenticationContext authCtx;
    private final AdminTabsWebComponent tabsWebComponent;

    private String projectKey;
    private String currentTab;
    private String currentSection;
    private String selectedMenuSection;

    private ProjectService.GetProjectResult result;
    private List<Header> headers;
    private Pair<String, Integer> tabInfo;

    public AdminDecoratorHelper(WebInterfaceManager webInterfaceManager, ProjectService projectService,
            JiraAuthenticationContext authenticationContext, ComponentFactory componentFactory)
    {
        this.webInterfaceManager = webInterfaceManager;
        this.service = projectService;
        this.authCtx = authenticationContext;
        this.tabsWebComponent = componentFactory.createObject(AdminTabsWebComponent.class);
    }

    public AdminDecoratorHelper setProject(String projectKey)
    {
        clearCache();

        this.projectKey = StringUtils.stripToNull(projectKey);
        return this;
    }

    public AdminDecoratorHelper setCurrentTab(String currentTab)
    {
        clearCache();

        this.currentTab = StringUtils.stripToNull(currentTab);
        return this;
    }

    public AdminDecoratorHelper setCurrentSection(String currentSection)
    {
        clearCache();

        this.currentSection = StringUtils.stripToNull(currentSection);
        if(StringUtils.contains(currentSection, "/"))
        {
            this.selectedMenuSection = currentSection.substring(0, currentSection.indexOf("/"));
        }
        else
        {
            this.selectedMenuSection = currentSection;
        }
        //Project config admin pages need to highlight the project menu!
        if(StringUtils.equals("atl.jira.proj.config", selectedMenuSection))
        {
            this.selectedMenuSection = "admin_project_menu";
        }

        return this;
    }

    public String getSelectedMenuSection()
    {
        return this.selectedMenuSection;
    }

    public boolean hasKey()
    {
        return projectKey != null;
    }

    public List<Header> getHeaders()
    {
        if (this.headers != null)
        {
            return this.headers;
        }

        Project project = getProject();
        Map<String, Object> context;
        String headerPanelSection;
        if (project == null)
        {
            context = MapBuilder.<String, Object>build("admin.active.section", currentSection, "admin.active.tab", currentTab);
            headerPanelSection = "system.admin.decorator.header";
        }
        else
        {
            context = MapBuilder.<String, Object>build("project", project, ACTIVE_SECTION_KEY, currentSection, ACTIVE_TAB_LINK_KEY, currentTab);
            headerPanelSection = "atl.jira.proj.config.header";
        }
        final List<WebPanel> panels = webInterfaceManager.getDisplayableWebPanels(headerPanelSection, Collections.<String, Object>emptyMap());
        final List<Header> headers = new ArrayList<Header>(panels.size());
        for (WebPanel panel : panels)
        {
            headers.add(new Header(panel, context));
        }
        this.headers = headers;
        return this.headers;
    }

    public int getNumberOfTabs()
    {
        return getTabInfo().second();
    }

    public String getTabHtml()
    {
        return getTabInfo().first();
    }

    private Pair<String, Integer> getTabInfo()
    {
        if (tabInfo != null)
        {
            return tabInfo;
        }

        return tabInfo = tabsWebComponent.render(getProject(), currentSection, currentTab);
    }

    private Project getProject()
    {
        ProjectService.GetProjectResult projectResult = getProjectResult();
        if (projectResult != null && projectResult.isValid())
        {
            return projectResult.getProject();
        }
        else
        {
            return null;
        }
    }

    private ProjectService.GetProjectResult getProjectResult()
    {
        if (result == null && hasKey())
        {
            result = service.getProjectByKeyForAction(authCtx.getLoggedInUser(), projectKey, ProjectAction.EDIT_PROJECT_CONFIG);
        }
        return result;
    }

    private void clearCache()
    {
        result = null;
        headers = null;
        tabInfo = null;
    }

    public static class Header
    {
        private final Map<String, Object> contextMap;
        private final WebPanel panel;

        private Header(WebPanel panel, Map<String, Object> contextMap)
        {
            this.contextMap = contextMap;
            this.panel = panel;
        }

        public String getHtml()
        {
            return panel.getHtml(contextMap);
        }
    }
}
