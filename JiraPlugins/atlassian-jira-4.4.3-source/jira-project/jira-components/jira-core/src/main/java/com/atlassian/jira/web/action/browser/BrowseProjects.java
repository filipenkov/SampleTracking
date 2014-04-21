package com.atlassian.jira.web.action.browser;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Action for Browse Projects
 */
public class BrowseProjects extends JiraWebActionSupport
{
    private final WebResourceManager webResourceManager;
    private final UserProjectHistoryManager projectHistoryManager;
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;

    private static final String ALL = "all";
    private static final String NONE = "none";
    private static final String RECENT = "recent";

    private List<ProjectCategoryBean> categories;
    private List<ProjectCategoryBean> tabs;
    private String selectedCategory;

    public BrowseProjects(final WebResourceManager webResourceManager,
            final UserProjectHistoryManager projectHistoryManager, ProjectManager projectManager,
            final PermissionManager permissionManager)
    {
        this.webResourceManager = webResourceManager;
        this.projectHistoryManager = projectHistoryManager;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
    }

    /**
     * Get the currently active Category.  Looks for it in the session, if it is "all" return it.
     * Else, check to see if the category exists and we can see projects in it.
     * Else, return the first category.
     *
     * @return the currently active category id, "all", "none" or "recent".
     * @throws GenericEntityException Sorry, yes we are using GVs.
     */
    public String getSelectedCategory() throws GenericEntityException
    {
        if (selectedCategory != null)
        {
            return selectedCategory;
        }

        String sessionCategory = getSession().get(SessionKeys.BROWSE_PROJECTS_CURRENT_TAB);

        if (ALL.equals(sessionCategory))
        {
            return sessionCategory;
        }

        String returnKey = null;
        final List<ProjectCategoryBean> categories = getTabs();
        for (ProjectCategoryBean category : categories)
        {
            if (returnKey == null || category.getId().equals(sessionCategory))
            {
                returnKey = category.getId();
            }
        }

        selectedCategory = returnKey == null ? ALL : returnKey;
        return selectedCategory;
    }

    /**
     * Places the given category in the session.
     *
     * @param selectedCategory the currently active category id, "all", "none" or "recent".
     */
    public void setSelectedCategory(final String selectedCategory)
    {
        getSession().put(SessionKeys.BROWSE_PROJECTS_CURRENT_TAB, selectedCategory);
    }

    private Map<String, String> getSession()
    {
        return ActionContext.getSession();
    }

    /**
     * Whether or not to show tabs.
     *
     * @return true if we would show more than one tab.
     * @throws GenericEntityException Sorry, yes we are using GVs.
     */
    public boolean showTabs() throws GenericEntityException
    {
        return !getTabs().isEmpty();
    }

    public List<ProjectCategoryBean> getTabs() throws GenericEntityException
    {
        if (tabs != null)
        {
            return tabs;
        }

        final List<ProjectCategoryBean> categories = getCategories();
        final List<ProjectCategoryBean> tabs = new ArrayList<ProjectCategoryBean>(categories.size());
        for (ProjectCategoryBean category : categories)
        {
            if (category.isTabVisible())
            {
                tabs.add(category);
            }
        }
        this.tabs = Collections.unmodifiableList(tabs);
        return tabs;
    }

    /**
     * Get the list of available categories as {@link com.atlassian.jira.web.action.browser.BrowseProjects.ProjectCategoryBean} objects.
     * These also contain the projects for that category.
     * <p/>
     * We filter out categories with no visible projects.
     * <p/>
     * We add a pseudo category for "none" No Category.
     * We add a pseudo category for "Recent Projects" if there are 2 or more categories.
     *
     * @return the list of available categories
     * @throws GenericEntityException Sorry, yes we are using GVs.
     */
    public List<ProjectCategoryBean> getCategories() throws GenericEntityException
    {
        if (categories == null)
        {
            List<ProjectCategoryBean> categories = new ArrayList<ProjectCategoryBean>();

            final Collection<GenericValue> categoryGVs = projectManager.getProjectCategories();
            for (GenericValue categoryGV : categoryGVs)
            {
                final Collection<GenericValue> projects = permissionManager.getProjects(Permissions.BROWSE, getRemoteUser(), categoryGV);
                if (projects != null && !projects.isEmpty())
                {
                    categories.add(new ProjectCategoryBean(categoryGV, projects));
                }
            }

            final Collection<GenericValue> noCategoryProjects = permissionManager.getProjects(Permissions.BROWSE, getRemoteUser(), null);
            final List<GenericValue> recentProjects = getRecentGVs();
            if (!noCategoryProjects.isEmpty())
            {
                if (categories.isEmpty())
                {
                    if (!recentProjects.isEmpty())
                    {
                        //We only want to show the "All Projects" and "Recent Projects" tabs. The "NONE" panel is the only thing to
                        //display in the all, however, we want to call it "All Projects" rather than no category in this case.
                        categories.add(new ProjectCategoryBean(getText("browse.projects.all"), "", NONE, noCategoryProjects, false, true));
                    }
                    else
                    {
                        //We only have "All Projects" to display. In this case just display a list of all the projects without tabs or title.
                        categories.add(new ProjectCategoryBean("", "", NONE, noCategoryProjects, false, true));
                    }
                }
                else
                {
                    categories.add(new ProjectCategoryBean(getText("browse.projects.none"), getText("browse.projects.none.desc"), NONE, noCategoryProjects));
                }
            }

            if (!recentProjects.isEmpty())
            {
                categories.add(0, new ProjectCategoryBean(getText("browse.projects.recent"), getText("browse.projects.recent.desc"), RECENT, recentProjects, true, false));
            }

            this.categories = Collections.unmodifiableList(categories);
        }

        return categories;
    }

    private List<GenericValue> getRecentGVs()
    {
        final List<Project> recentProjects = projectHistoryManager.getProjectHistoryWithPermissionChecks(Permissions.BROWSE, getRemoteUser());
        final List<GenericValue> recentGVs = new ArrayList<GenericValue>(recentProjects.size());
        for (Project recentProject : recentProjects)
        {
            recentGVs.add(recentProject.getGenericValue());
        }
        return recentGVs;
    }


    @Override
    protected String doExecute() throws Exception
    {
        final List<ProjectCategoryBean> categories = getCategories();
        int projectCount = 0;
        for (ProjectCategoryBean category : categories)
        {
            projectCount += category.getProjects().size();
        }

        // If there is only 1 project, lets browse it.
        if (projectCount == 1)
        {
            return getRedirect("/secure/BrowseProject.jspa");
        }

        webResourceManager.requireResource("jira.webresources:browseprojects");

        return super.doExecute();
    }


    /**
     * Simple bean that contains Project category information and its containing projects as GVS.
     */
    public static class ProjectCategoryBean
    {
        private final Collection<GenericValue> projects;

        private final String name;
        private final String description;
        private final String id;
        private final boolean tabVisible;
        private final boolean includeInAll;

        public ProjectCategoryBean(GenericValue category, Collection<GenericValue> projects)
        {
            this.projects = projects;
            this.name = category.getString("name");
            final String desc = category.getString("description");
            if (StringUtils.isBlank(desc))
            {
                this.description = this.name;
            }
            else
            {
                this.description = this.name + " - " + desc;
            }
            this.id = category.getString("id");
            this.tabVisible = true;
            this.includeInAll = true;
        }

        public ProjectCategoryBean(String name, String description, String id, Collection<GenericValue> projects)
        {
            this (name, description, id, projects, true, true);
        }

        public ProjectCategoryBean(String name, String description, String id, Collection<GenericValue> projects,
                boolean tabVisible, boolean inAll)
        {
            this.projects = projects;
            this.name = name;
            this.description = description;
            this.id = id;
            this.tabVisible = tabVisible;
            this.includeInAll = inAll;
        }

        public boolean hasProject()
        {
            return projects != null && !projects.isEmpty();
        }

        public Collection<GenericValue> getProjects()
        {
            return projects;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public String getId()
        {
            return id;
        }

        public boolean isTabVisible()
        {
            return tabVisible;
        }

        public boolean isAll()
        {
            return includeInAll;
        }
    }
}