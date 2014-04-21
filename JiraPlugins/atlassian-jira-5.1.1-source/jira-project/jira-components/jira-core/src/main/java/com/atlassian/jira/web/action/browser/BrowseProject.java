package com.atlassian.jira.web.action.browser;

import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.TabPanelModuleDescriptor;
import com.atlassian.jira.plugin.browsepanel.TabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorComparator;
import com.atlassian.jira.plugin.webresource.SuperBatchFilteringWriter;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.project.browse.BrowseProjectContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.json.JSONEscaper;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.action.util.PopularIssueTypesUtil;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import webwork.action.ActionContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class BrowseProject extends ProjectActionSupport
{

    private static final String COKE_TAB_PANEL_NAME = "coke";

    protected final PluginAccessor pluginAccessor;
    private final WebResourceManager webResourceManager;
    private final PopularIssueTypesUtil popularIssueTypesUtil;
    private final UserProjectHistoryManager projectHistoryManager;

    private List<ProjectTabPanelModuleDescriptor> projectTabPanels;
    private boolean contentOnly = false;

    private Long projectId;

    private boolean noTitle = false;
    private static final String NO_TITLE = "noTitle";

    private PopularIssueTypesUtil.PopularIssueTypesHolder issueTypesHolder;
    private boolean stateUpdateOnly = false;
    private static final String STATEUPDATE = "stateupdate";
    private static final String CONTENTONLY = "contentonly";

    public BrowseProject(final ProjectManager projectManager, final PermissionManager permissionManager,
                         final PluginAccessor pluginAccessor, final WebResourceManager webResourceManager,
                         final PopularIssueTypesUtil popularIssueTypesUtil, final UserProjectHistoryManager projectHistoryManager)
    {
        super(projectManager, permissionManager);
        this.pluginAccessor = pluginAccessor;
        this.webResourceManager = webResourceManager;
        this.popularIssueTypesUtil = popularIssueTypesUtil;
        this.projectHistoryManager = projectHistoryManager;
    }

    public Long getId()
    {
        return projectId;
    }

    public void setId(final Long id)
    {
        projectId = id;
        setSelectedProjectId(id);
    }

    public String getSelected()
    {
        final String currentKey = (String) getSession().get(SessionKeys.PROJECT_BROWSER_CURRENT_TAB);
        if (canSeeTab(currentKey))
        {
            return currentKey;
        }

        final List<ProjectTabPanelModuleDescriptor> projectTabPanels = getProjectTabPanels();
        if (!projectTabPanels.isEmpty())
        {
            final String key = (projectTabPanels.get(0)).getCompleteKey();
            getSession().put(SessionKeys.PROJECT_BROWSER_CURRENT_TAB, key);
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
        final StringTokenizer st = new StringTokenizer(tabKey, ":");
        if (st.countTokens() == 2)
        {
            // Get the key of the currently selected tab
            st.nextToken();
            final String tabName = st.nextToken();
            // Iterate over the available project tab panels
            for (final ProjectTabPanelModuleDescriptor projectTabPanelModuleDescriptor : getProjectTabPanels())
            {
                if ((tabName != null) && tabName.equals(projectTabPanelModuleDescriptor.getKey()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    protected Map<String, Object> getSession()
    {
        return ActionContext.getSession();
    }

    public void setSelectedTab(final String report)
    {
        getSession().put(SessionKeys.PROJECT_BROWSER_CURRENT_TAB, report);
    }

    public void setStateUpdateOnly(boolean stateUpdateOnly)
    {

        this.stateUpdateOnly = stateUpdateOnly;
    }

    public boolean isStateUpdateOnly()
    {
        return stateUpdateOnly;
    }

    /**
     * This method returns currently selected project
     *
     * @return currently selected project
     * @since 3.10
     */
    public Project getProject()
    {
        return projectHistoryManager.getCurrentProject(Permissions.BROWSE, getLoggedInUser());
    }

    public boolean isHasProjectAdminPermission()
    {
        return getPermissionManager().hasPermission(Permissions.PROJECT_ADMIN, getProject(), getLoggedInUser());
    }


    /**
     * Browse single project
     *
     * @return string representing the view
     * @throws Exception if cannot get the project
     */
    public String doExecute() throws Exception
    {
        try
        {
            final Project project = getProject();
            if (projectId != null)
            {
                final Project newProject = projectManager.getProjectObj(projectId);
                if (newProject != null && !newProject.equals(project))
                {
                    // project set is different to current project.
                    // Most probably permission violation
                    log.info("Specified project id to browse, but it is diff to current project.  Most prob a permission violation (or it doesn't exist)");
                    return PERMISSION_VIOLATION_RESULT;

                }

            }

            if (project == null)
            {
                // they haven't selected a project, if there is only one - select it for them
                final Collection<Project> projects = getBrowsableProjects();
                if (projects.size() == 1)
                {
                    final Project onlyProject = projects.iterator().next();
                    setSelectedProject(onlyProject);
                }
            }
            if (getProject() == null)
            {
                return getRedirect("/secure/BrowseProjects.jspa");
            }

            if (!contentOnly)
            {
                webResourceManager.requireResource("jira.webresources:ajaxhistory");
                webResourceManager.requireResource("jira.webresources:browseproject");
            }

            if (stateUpdateOnly)
            {
                return STATEUPDATE;
            }
            if (contentOnly)
            {
                return CONTENTONLY;
            }
        }
        catch (final IllegalStateException e)
        {
            log.info("Permissions Exception whilst browsing project", e);
            return PERMISSION_VIOLATION_RESULT;
        }

        return SUCCESS;
    }

    public boolean hasCreateIssuePermissionForProject()
    {
        return getPermissionManager().hasPermission(Permissions.CREATE_ISSUE, getProject(), getLoggedInUser());
    }

    public List<ProjectTabPanelModuleDescriptor> getProjectTabPanels()
    {
        if (projectTabPanels == null)
        {
            projectTabPanels = initTabPanels(ProjectTabPanelModuleDescriptor.class);
        }
        return projectTabPanels;
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
        else if (!(tabPanelDescriptor instanceof ProjectTabPanelModuleDescriptor))
        {
            log.warn("Incorrect plugin module type '" + selectedTab + "' has been specified.");
            return "";
        }
        else
        {
            final ProjectTabPanel panel = (ProjectTabPanel) tabPanelDescriptor.getModule();
            if (panel.showPanel(getBrowseContext()))
            {
                final String tabHtml = panel.getHtml(getBrowseContext());
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

        final ProjectTabPanelModuleDescriptor tabPanelDescriptor = (ProjectTabPanelModuleDescriptor) pluginAccessor.getEnabledPluginModule(selectedTab);

        return tabPanelDescriptor.getLabel();

    }

    public void setContentOnly(final boolean contentOnly)
    {
        this.contentOnly = contentOnly;
    }

    public boolean isContentOnly()
    {
        return contentOnly;
    }

    public boolean isNoTitle()
    {
        return noTitle;
    }

    public void setNoTitle(final boolean noTitle)
    {
        this.noTitle = noTitle;
    }


    private PopularIssueTypesUtil.PopularIssueTypesHolder getIssueTypesHolder()
    {
        if (issueTypesHolder == null)
        {
            issueTypesHolder = popularIssueTypesUtil.getPopularAndOtherIssueTypesForProject(getProject(), getLoggedInUser());
        }

        return issueTypesHolder;
    }

    public Collection<IssueType> getPopularIssueTypes()
    {
        return getIssueTypesHolder().getPopularIssueTypes();
    }

    public Collection<IssueType> getOtherIssueTypes()
    {
        return getIssueTypesHolder().getOtherIssueTypes();
    }

    /**
     * Initializes the tab panels and returns a list of {@link com.atlassian.plugin.ModuleDescriptor} objects.
     *
     * @param tabPanelClass tab panel class
     * @return list of {@link com.atlassian.plugin.ModuleDescriptor} objects, never null
     * @since v3.10
     */
    protected List<ProjectTabPanelModuleDescriptor> initTabPanels(final Class tabPanelClass)
    {
        final List<ProjectTabPanelModuleDescriptor> tabPanels;
        try
        {
            tabPanels = new ArrayList<ProjectTabPanelModuleDescriptor>(pluginAccessor.getEnabledModuleDescriptorsByClass(tabPanelClass));
            for (final Iterator<ProjectTabPanelModuleDescriptor> iterator = tabPanels.iterator(); iterator.hasNext();)
            {
                final ProjectTabPanelModuleDescriptor descriptor = iterator.next();
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

    /**
     * Returns true if the tab panel of the given descriptor should be hidden from the current view
     *
     * @param descriptor module descriptor
     * @return true if hidden, false otherwise
     * @throws PermissionException if project is invalid or not visible to the current user
     * @since v3.10
     */
    protected boolean isTabPanelHidden(final TabPanelModuleDescriptor<? extends TabPanel> descriptor) throws PermissionException
    {
        return !descriptor.getModule().showPanel(getBrowseContext());
    }

    private BrowseContext getBrowseContext()
    {
        return new BrowseProjectContext(getLoggedInUser(), getProject());
    }
}
