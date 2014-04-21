package com.atlassian.jira.web.action.browser;

import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.AbstractTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorComparator;
import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.plugin.versionpanel.BrowseVersionContextImpl;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanel;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.webresource.SuperBatchFilteringWriter;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Version browser that is similar to project browser ({@link BrowseProject}) but also take version into account.
 *
 * @since v3.10
 */
public class BrowseVersion extends JiraWebActionSupport
{
    private List<VersionTabPanelModuleDescriptor> versionTabPanels;
    private Long versionId;
    private BrowseVersionContext versionContext;

    private final PluginAccessor pluginAccessor;
    private final WebResourceManager webResourceManager;
    private final ProjectManager projectManager;
    private final PopularIssueTypesUtil popularIssueTypesUtil;
    private final PermissionManager permissionManager;

    private Collection<IssueType> popularIssueTypes;
    private Collection<IssueType> otherIssueTypes;
    private boolean contentOnly = false;
    private boolean noTitle = false;
    private boolean stateUpdateOnly;
    private ProjectTabPanelModuleDescriptor projectTab;
    private Long projectId;

    public BrowseVersion(final PluginAccessor pluginAccessor, final WebResourceManager webResourceManager,
            final PopularIssueTypesUtil popularIssueTypesUtil, final ProjectManager projectManager,
            final PermissionManager permissionManager)
    {
        this.pluginAccessor = pluginAccessor;
        this.webResourceManager = webResourceManager;
        this.popularIssueTypesUtil = popularIssueTypesUtil;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
    }

    protected String doExecute()
    {
        // Check that the user has entered in a valid project they are allowed to see and that they also have entered
        // in a valid version for that project
        Project project = getSelectedProjectObject();
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
            return getRedirect("/secure/BrowseProjects.jspa");
        }


        if (this.versionId != null)
        {
            Version version = getVersionManager().getVersion(this.versionId);
            if (version != null && version.getProjectObject().getId().equals(project.getId()))
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
        // ok they can see the project but some how they have a invalid version id
        // jump them back to browse the individual project
        return getRedirect("/browse/" + project.getKey());

    }

    public boolean hasCreateIssuePermissionForProject()
    {
        return permissionManager.hasPermission(Permissions.CREATE_ISSUE, getProject(), getRemoteUser());
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
        else if (!(tabPanelDescriptor instanceof VersionTabPanelModuleDescriptor))
        {
            log.warn("Incorrect plugin module type '" + selectedTab + "' has been specified.");
            return "";
        }
        else
        {
            final VersionTabPanel panel = (VersionTabPanel) tabPanelDescriptor.getModule();
            if (panel.showPanel(getVersionContext()))
            {
                final String tabHtml = panel.getHtml(getVersionContext());
                final StringBuilder strBuilder = new StringBuilder();
                if (isContentOnly())
                {
                    final SuperBatchFilteringWriter writer = new SuperBatchFilteringWriter();
                    webResourceManager.includeResources(writer, UrlMode.AUTO);
                    strBuilder.append(writer.toString());

                }
                if (!isNoTitle())
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

        final VersionTabPanelModuleDescriptor tabPanelDescriptor = (VersionTabPanelModuleDescriptor) pluginAccessor.getEnabledPluginModule(selectedTab);

        return tabPanelDescriptor.getLabel();

    }

    public Long getId()
    {
        return projectId;
    }

    public void setId(Long id)
    {
        projectId = id;
        setSelectedProjectId(id);
    }

    public void setVersionId(Long id)
    {
        this.versionId = id;
        this.versionContext = null;
    }

    public Project getProject()
    {
        return getSelectedProjectObject();
    }

    /**
     * Constructs a version context for the fix for version.  Override this method
     * if you'd like to search other version types.
     */
    public BrowseVersionContext getVersionContext()
    {
        if (versionContext == null)
        {
            versionContext = new BrowseVersionContextImpl(getVersion(), getRemoteUser());
        }
        return versionContext;
    }

    public List<VersionTabPanelModuleDescriptor> getVersionTabPanels()
    {
        if (versionTabPanels == null)
        {
            versionTabPanels = initTabPanels(VersionTabPanelModuleDescriptor.class);
        }
        return versionTabPanels;
    }

    public String getSelected()
    {
        final String currentKey = (String) ActionContext.getSession().get(SessionKeys.VERSION_BROWSER_SELECTED);
        if (canSeeTab(currentKey))
        {
            return currentKey;
        }

        List<VersionTabPanelModuleDescriptor> tabPanels = getVersionTabPanels();
        if (!tabPanels.isEmpty())
        {
            final String key = (tabPanels.get(0)).getCompleteKey();
            ActionContext.getSession().put(SessionKeys.VERSION_BROWSER_SELECTED, key);
            return key;
        }

        return null;

    }

    private boolean canSeeTab(String tabKey)
    {
        if (tabKey == null)
        {
            return false;
        }

        final List<VersionTabPanelModuleDescriptor> tabPanels = getVersionTabPanels();
        StringTokenizer st = new StringTokenizer(tabKey, ":");
        if (st.countTokens() == 2)
        {
            // Get the key of the currently selected tab
            st.nextToken();
            String tabName = st.nextToken();
            // Iterate over the available project tab panels
            for (VersionTabPanelModuleDescriptor descriptor : tabPanels)
            {
                if (tabName != null && tabName.equals(descriptor.getKey()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public void setSelectedTab(String report)
    {
        ActionContext.getSession().put(SessionKeys.VERSION_BROWSER_SELECTED, report);
    }

    protected boolean isTabPanelHidden(AbstractTabPanelModuleDescriptor descriptor) throws PermissionException
    {
        return !((VersionTabPanel) descriptor.getModule()).showPanel(getVersionContext());
    }

    /**
     * Initializes the tab panels and returns a list of {@link com.atlassian.plugin.ModuleDescriptor} objects.
     *
     * @param tabPanelClass tab panel class
     * @return list of {@link com.atlassian.plugin.ModuleDescriptor} objects, never null
     */
    protected List<VersionTabPanelModuleDescriptor> initTabPanels(Class tabPanelClass)
    {
        List<VersionTabPanelModuleDescriptor> tabPanels;
        try
        {
            tabPanels = new ArrayList<VersionTabPanelModuleDescriptor>(pluginAccessor.getEnabledModuleDescriptorsByClass(tabPanelClass));
            for (Iterator iterator = tabPanels.iterator(); iterator.hasNext();)
            {
                AbstractTabPanelModuleDescriptor descriptor = (AbstractTabPanelModuleDescriptor) iterator.next();
                if (isTabPanelHidden(descriptor))
                {
                    iterator.remove();
                }
            }
        }
        catch (PermissionException e)
        {
            throw new RuntimeException(e);
        }
        Collections.sort(tabPanels, ModuleDescriptorComparator.COMPARATOR);
        return tabPanels;
    }

    public Version getVersion()
    {
        return versionId == null ? null : getVersionManager().getVersion(versionId);
    }

    public NextPreviousVersion getNextAndPreviousVersions()
    {
        final Version currentVersion = getVersion();

        if (currentVersion != null)
        {
            final Collection<Version> versions = getVersionManager().getVersionsUnarchived(currentVersion.getProjectObject().getId());
            Version previous = null;
            Version next = null;
            for (Iterator<Version> versionIterator = versions.iterator(); versionIterator.hasNext();)
            {
                Version version = versionIterator.next();

                if (version.equals(currentVersion))
                {
                    if (versionIterator.hasNext())
                    {
                        next = versionIterator.next();
                    }
                    return new NextPreviousVersion(previous, next);
                }
                previous = version;
            }
        }

        return new NextPreviousVersion(null, null);
    }

    public static class NextPreviousVersion
    {
        private final Version next;
        private final Version previous;


        public NextPreviousVersion(Version previous, Version next)
        {
            this.next = next;
            this.previous = previous;
        }

        public Version getNext()
        {
            return next;
        }

        public Version getPrevious()
        {
            return previous;
        }
    }

    public boolean isHasProjectAdminPermission()
    {
        return permissionManager.hasPermission(Permissions.PROJECT_ADMIN, getProject(), getRemoteUser());
    }

    public Collection<IssueType> getPopularIssueTypes()
    {
        if (popularIssueTypes == null)
        {
            popularIssueTypes = popularIssueTypesUtil.getPopularIssueTypesForProject(getProject(), getRemoteUser());
        }

        return popularIssueTypes;
    }

    public Collection<IssueType> getOtherIssueTypes()
    {
        if (otherIssueTypes == null)
        {
            otherIssueTypes = popularIssueTypesUtil.getOtherIssueTypesForProject(getProject(), getRemoteUser());
        }

        return otherIssueTypes;
    }

    public boolean isContentOnly()
    {
        return contentOnly;
    }

    public void setContentOnly(boolean contentOnly)
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
