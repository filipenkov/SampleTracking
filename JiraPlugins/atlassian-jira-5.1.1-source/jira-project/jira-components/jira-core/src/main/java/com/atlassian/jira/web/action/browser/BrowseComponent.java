package com.atlassian.jira.web.action.browser;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.TabPanelModuleDescriptor;
import com.atlassian.jira.plugin.browsepanel.TabPanel;
import com.atlassian.jira.plugin.componentpanel.BrowseComponentContextImpl;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanel;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorComparator;
import com.atlassian.jira.plugin.webresource.SuperBatchFilteringWriter;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.json.JSONEscaper;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.util.PopularIssueTypesUtil;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import webwork.action.ActionContext;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Version browser that is similar to project browser ({@link BrowseProject}) but also take version into account.
 *
 * @since 3.10
 */
public class BrowseComponent extends JiraWebActionSupport
{
    private final ProjectComponentManager projectComponentManager;
    private final PluginAccessor pluginAccessor;
    private final WebResourceManager webResourceManager;
    private List<ComponentTabPanelModuleDescriptor> componentTabPanels;
    private Long componentId;

    private Collection<IssueType> popularIssueTypes;
    private Collection<IssueType> otherIssueTypes;
    private final PopularIssueTypesUtil popularIssueTypesUtil;
    private final SearchService searchService;
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;

    private boolean noTitle = false;
    private static final String NO_TITLE = "noTitle";

    private boolean contentOnly = false;
    private boolean stateUpdateOnly;
    private ProjectTabPanelModuleDescriptor projectTab;
    private Long projectId;

    public BrowseComponent(final ProjectComponentManager projectComponentManager, final PluginAccessor pluginAccessor,
                           final WebResourceManager webResourceManager, final PopularIssueTypesUtil popularIssueTypesUtil,
                           final SearchService searchService, ProjectManager projectManager,
                           final PermissionManager permissionManager)
    {
        this.projectComponentManager = projectComponentManager;
        this.pluginAccessor = pluginAccessor;
        this.webResourceManager = webResourceManager;
        this.popularIssueTypesUtil = popularIssueTypesUtil;
        this.searchService = searchService;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
    }

    @Override
    protected String doExecute()
    {

        //
        // check that the user has entered in a valid projct they are allowed to see
        // and that they also have entered in valid component for that project
        //
        final Project project = getSelectedProjectObject();
        if (projectId != null)
        {
            final Project newProject = projectManager.getProjectObj(projectId);
            if (newProject!= null && !newProject.equals(project))
            {
                // project set is different to current project.
                // Most probably permission violation
                log.info("Specified project id to browse, but it is diff to current project.  Most prob a permission violation (or it doesn't exist)");
                return PERMISSION_VIOLATION_RESULT;
            }
        }

        if (project == null)
        {
            // redirect to browse all projects.
            return getRedirect("/secure/BrowseProjects.jspa");
        }

        if (componentId != null)
        {
            ProjectComponent component = null;
            try
            {
                component = projectComponentManager.find(componentId);
            }
            catch (final EntityNotFoundException e)
            {
                // this is to be factored out eventually and this check will become redundant but until then...
            }
            if ((component != null) && component.getProjectId().equals(project.getId()))
            {
                if (!contentOnly)
                {
                    webResourceManager.requireResource("jira.webresources:ajaxhistory");
                    webResourceManager.requireResource("jira.webresources:browseproject");
                }
                if (stateUpdateOnly)
                {
                    return "stateupdate";
                }
                if (contentOnly)
                {
                    return "contentonly";
                }
                return SUCCESS;
            }
        }
        //
        // ok they can see the project but some how they have a invalid component id
        // jump them back to browse the individual project
        return getRedirect("/browse/" + project.getKey());

    }

    public boolean hasCreateIssuePermissionForProject()
    {
        return permissionManager.hasPermission(Permissions.CREATE_ISSUE, getProject(), getLoggedInUser());
    }


    public void setComponentId(final Long id)
    {
        componentId = id;
    }

    public ProjectComponent getComponent()
    {
        try
        {
            return componentId == null ? null : projectComponentManager.find(componentId);
        }
        catch (final EntityNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    public List<ComponentTabPanelModuleDescriptor> getComponentTabPanels()
    {
        if (componentTabPanels == null)
        {
            componentTabPanels = initTabPanels(ComponentTabPanelModuleDescriptor.class);
        }
        return componentTabPanels;
    }

    public String getSelected()
    {
        final Map session = ActionContext.getSession();
        final String currentKey = (String) session.get(SessionKeys.COMPONENT_BROWSER_SELECTED);

        if (canSeeTab(currentKey))
        {
            return currentKey;
        }

        final List<ComponentTabPanelModuleDescriptor> tabPanels = getComponentTabPanels();

        if (!tabPanels.isEmpty())
        {
            final String key = tabPanels.get(0).getCompleteKey();
            session.put(SessionKeys.COMPONENT_BROWSER_SELECTED, key);
            return key;
        }

        return null;

    }

    private boolean canSeeTab(final String tabKey)
    {
        if (tabKey == null)
        {
            return false;
        }
        final List<ComponentTabPanelModuleDescriptor> tabPanels = getComponentTabPanels();
        final StringTokenizer st = new StringTokenizer(tabKey, ":");
        if (st.countTokens() == 2)
        {
            // Get the key of the currently selected tab
            st.nextToken();
            final String tabName = st.nextToken();
            // Iterate over the available project tab panels
            for (final ComponentTabPanelModuleDescriptor descriptor : tabPanels)
            {
                if ((tabName != null) && tabName.equals(descriptor.getKey()))
                {
                    return true;
                }
            }
        }
        return false;

    }

    public void setSelectedTab(final String report)
    {
        ActionContext.getSession().put(SessionKeys.COMPONENT_BROWSER_SELECTED, report);
    }


    public String getTabHtmlForJSON() throws IOException
    {
        final String tabPanelHTML = getTabHtml();

        return JSONEscaper.escape(tabPanelHTML);
    }

    public String getTabHtml() throws IOException
    {
        final String selectedTab = getSelected();
        if (selectedTab == null)
        {
            log.warn("Either unknown tab specified or no tab specfied and no tabs in system");
            return "";
        }

        final ModuleDescriptor tabPanelDescriptor = pluginAccessor.getEnabledPluginModule(selectedTab);

        if (tabPanelDescriptor == null)
        {
            log.warn("Unknown tab panel '" + selectedTab + "' has been specified.");
            return "";
        }
        else if (!(tabPanelDescriptor instanceof ComponentTabPanelModuleDescriptor))
        {
            log.warn("Incorrect plugin module type '" + selectedTab + "' has been specified.");
            return "";
        }
        else
        {
            final ComponentTabPanel panel = (ComponentTabPanel) tabPanelDescriptor.getModule();
            if (panel.showPanel(getContext()))
            {
                final String tabHtml = panel.getHtml(getContext());
                final StringBuilder strBuilder = new StringBuilder();
                if (isContentOnly())
                {
                    final SuperBatchFilteringWriter writer = new SuperBatchFilteringWriter();
                    webResourceManager.includeResources(writer, UrlMode.AUTO);
                    strBuilder.append(writer.toString());
                }

                boolean descriptorNoTitle = tabPanelDescriptor.getParams().containsKey(NO_TITLE) && "true".equalsIgnoreCase((String) tabPanelDescriptor.getParams().get(NO_TITLE));
                if (!(isNoTitle() || descriptorNoTitle))
                {

                    strBuilder.append("<h2>");
                    strBuilder.append(getTabLabel());
                    strBuilder.append("</h2>\n");
                }
                strBuilder.append(tabHtml);
                return strBuilder.toString();

            }
            log.warn("Tab panel should be hidden.");
        }
        return "";
    }

    public String getTabLabel()
    {
        final String selectedTab = getSelected();
        if (selectedTab == null)
        {
            log.warn("Either unknown tab specified or no tab specfied and no tabs in system");
            return "";
        }

        final ComponentTabPanelModuleDescriptor tabPanelDescriptor = (ComponentTabPanelModuleDescriptor) pluginAccessor.getEnabledPluginModule(selectedTab);

        return tabPanelDescriptor.getLabel();

    }

    public Long getId() throws PermissionException
    {
        return projectId;
    }

    public void setId(final Long id)
    {
        projectId = id;
        setSelectedProjectId(id);
    }

    /**
     * Returns full name of the user with given username.
     *
     * @param username username
     * @return full name of the user if found, given username if user not found, empty string if username was null
     */
    public String getFullName(final String username)
    {
        String fullname;
        if (username == null)
        {
            fullname = "";
        }
        else
        {
            User user = UserUtils.getUser(username);
            if (user != null)
            {
                fullname = user.getDisplayName();
            }
            else
            {
                fullname = username;
            }
        }
        return fullname;
    }

    public Project getProject()
    {
        return getSelectedProjectObject();
    }

    public boolean isHasProjectAdminPermission()
    {
        return permissionManager.hasPermission(Permissions.PROJECT_ADMIN, getProject(), getLoggedInUser());
    }

    /**
     * Initializes the tab panels and returns a list of {@link com.atlassian.plugin.ModuleDescriptor} objects.
     *
     * @param tabPanelClass tab panel class
     * @return list of {@link com.atlassian.plugin.ModuleDescriptor} objects, never null
     */
    protected List<ComponentTabPanelModuleDescriptor> initTabPanels(final Class tabPanelClass)
    {
        final List<ComponentTabPanelModuleDescriptor> tabPanels;
        try
        {
            tabPanels = new ArrayList<ComponentTabPanelModuleDescriptor>(pluginAccessor.getEnabledModuleDescriptorsByClass(tabPanelClass));
            for (final Iterator<ComponentTabPanelModuleDescriptor> iterator = tabPanels.iterator(); iterator.hasNext();)
            {
                final ComponentTabPanelModuleDescriptor descriptor = iterator.next();
                if (isTabPanelHidden(descriptor))
                {
                    iterator.remove();
                }
            }
        }
        catch (final PermissionException e)
        {
            throw new RuntimeException(e);
        }
        Collections.sort(tabPanels, ModuleDescriptorComparator.COMPARATOR);
        return tabPanels;
    }

    protected boolean isTabPanelHidden(final TabPanelModuleDescriptor<? extends TabPanel> descriptor) throws PermissionException
    {
        return !descriptor.getModule().showPanel(getContext());
    }

    private BrowseComponentContextImpl getContext()
    {
        return new BrowseComponentContextImpl(searchService, getComponent(), getLoggedInUser());
    }

    public Collection<IssueType> getPopularIssueTypes()
    {
        if (popularIssueTypes == null)
        {
            popularIssueTypes = popularIssueTypesUtil.getPopularIssueTypesForProject(getProject(), getLoggedInUser());
        }

        return popularIssueTypes;
    }

    public Collection<IssueType> getOtherIssueTypes()
    {
        if (otherIssueTypes == null)
        {
            otherIssueTypes = popularIssueTypesUtil.getOtherIssueTypesForProject(getProject(), getLoggedInUser());
        }

        return otherIssueTypes;
    }

    public boolean isContentOnly()
    {
        return contentOnly;
    }

    public void setContentOnly(final boolean contentOnly)
    {
        this.contentOnly = contentOnly;
    }

    public boolean isStateUpdateOnly()
    {
        return stateUpdateOnly;
    }

    public void setStateUpdateOnly(boolean stateUpdateOnly)
    {
        this.stateUpdateOnly = stateUpdateOnly;
    }

    public boolean isNoTitle()
    {
        return noTitle;
    }

    public void setNoTitle(boolean noTitle)
    {
        this.noTitle = noTitle;
    }

    private ProjectTabPanelModuleDescriptor getSelectedProjectTab()
    {
        if (projectTab == null)
        {
            final String currentKey = (String) ActionContext.getSession().get(SessionKeys.PROJECT_BROWSER_CURRENT_TAB);
            if (currentKey != null)
            {
                projectTab = (ProjectTabPanelModuleDescriptor) pluginAccessor.getEnabledPluginModule(currentKey);
            }
        }
        return projectTab;
    }

    public String getBrowseProjectTabLabel()
    {
        final ProjectTabPanelModuleDescriptor browseProjectTab = getSelectedProjectTab();
        if (browseProjectTab == null)
        {
            return getText("common.concepts.summary");
        }
        return browseProjectTab.getLabel();
    }

    public String getBrowseProjectTabKey()
    {
        final ProjectTabPanelModuleDescriptor browseProjectTab = getSelectedProjectTab();
        if (browseProjectTab == null)
        {
            return "com.atlassian.jira.plugin.system.project:summary-panel";
        }
        return browseProjectTab.getCompleteKey();
    }


}
