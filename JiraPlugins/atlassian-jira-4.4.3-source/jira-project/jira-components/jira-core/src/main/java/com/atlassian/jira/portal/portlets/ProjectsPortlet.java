package com.atlassian.jira.portal.portlets;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorComparator;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.browse.BrowseProjectContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.jira.web.bean.StatisticMapWrapper;
import com.atlassian.jira.web.component.webfragment.WebFragmentWebComponent;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.seraph.util.RedirectUtils;
import com.atlassian.util.profiling.UtilTimerStack;
import com.opensymphony.user.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Portlet that displays information about multiple projects.
 *
 * @since v3.13
 */
public class ProjectsPortlet extends PortletImpl
{
    private static final Logger log = Logger.getLogger(ProjectsPortlet.class);
    private final PluginAccessor pluginAccessor;
    private final ProjectManager projectManager;
    private final ConstantsManager constantsManager;

    public ProjectsPortlet(final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager, final ApplicationProperties applicationProperties, final PluginAccessor pluginAccessor, final ProjectManager projectManager, final ConstantsManager constantsManager)
    {
        super(authenticationContext, permissionManager, applicationProperties);
        this.pluginAccessor = pluginAccessor;
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
    }

    protected Map<String, Object> getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        final HttpServletRequest request = ActionContext.getContext().getRequestImpl();

        final User user = authenticationContext.getUser();
        final Long projectCategoryId = getProjectCategoryId(portletConfiguration);

        final Map<String, Object> params = super.getVelocityParams(portletConfiguration);

        params.put("redirectUtils", new RedirectUtils());
        params.put("projectsExist", !projectManager.getProjects().isEmpty());
        params.put("portletConfigurationId", portletConfiguration.getId());

        params.put("isGoodBrowser", ProjectPortletHelper.isGoodBrowser(request));

        params.put("user", user);
        params.put("isCategorySet", isCategorySet(projectCategoryId));
        if (projectCategoryId == null)
        {
            params.put("browseableProjects", permissionManager.getProjects(Permissions.BROWSE, user));
            params.put("projectCategories", projectManager.getProjectCategories());
            params.put("browseableProjectsWithNoCategory", getBrowseableProjectsWithNoCategory());
        }
        else
        {
            params.put("projectCategory", projectManager.getProjectCategory(projectCategoryId));
        }
        return params;
    }

    private Long getProjectCategoryId(final PortletConfiguration portletConfiguration)
    {
        try
        {
            final String categoryId = portletConfiguration.getProperty("projectcategoryid");
            // To handle backward compatibility, we cannot just test for null. We need to check for non-empty string.
            if ((categoryId != null) && (categoryId.length() > 0))
            {
                try
                {
                    return new Long(categoryId);
                }
                catch (final NumberFormatException e)
                {
                    log.warn("Project category ID is not a number: '" + categoryId + "' in projects portlet: '" + portletConfiguration.getId() + "'");
                    return null;
                }
            }
        }
        catch (final ObjectConfigurationException e)
        {
            log.warn("Could not get project category ID for projects portlet: '" + portletConfiguration.getId() + "'");
        }
        return null;
    }

    private Boolean isCategorySet(final Long projectCategoryId)
    {
        return (projectCategoryId != null);
    }

    /**
     * Retrieves a list of projects belonging to the specified category that the user has permission to see
     *
     * @param category specify the category or null to retrieve a list of browseable projects that are not associated with any category
     * @return collection of project generic values
     */
    public Collection getBrowseableProjectsInCategory(final GenericValue category)
    {
        try
        {
            return genericValueToProject(permissionManager.getProjects(Permissions.BROWSE, authenticationContext.getUser(), category));
        }
        catch (final DataAccessException e)
        {
            log.error("Failed retrieving projects with category: '" + category + "'", e);
            return Collections.EMPTY_LIST;
        }
    }

    private Collection getBrowseableProjectsWithNoCategory()
    {
        try
        {
            return genericValueToProject(permissionManager.getProjects(Permissions.BROWSE, authenticationContext.getUser(), null));
        }
        catch (final DataAccessException e)
        {
            log.error("Failed retrieving projects with no category", e);
            return Collections.EMPTY_LIST;
        }
    }

    private Collection genericValueToProject(final Collection genericValues)
    {
        final Collection projects = new ArrayList(genericValues);
        final Transformer transformer = new Transformer()
        {
            public Object transform(final Object object)
            {
                return projectManager.getProjectObj(((GenericValue) object).getLong("id"));
            }
        };
        CollectionUtils.transform(projects, transformer);
        return projects;
    }

    public Boolean hasBrowsePermission(final Project project)
    {
        return (project != null) && permissionManager.hasPermission(Permissions.BROWSE, project, authenticationContext.getUser());
    }

    public List getProjectTabPanels(final Project project)
    {
        final List projectTabPanels = new ArrayList(pluginAccessor.getEnabledModuleDescriptorsByClass(ProjectTabPanelModuleDescriptor.class));
        final BrowseProjectContext ctx = new BrowseProjectContext(authenticationContext.getUser(), project);

        for (final Iterator iterator = projectTabPanels.iterator(); iterator.hasNext();)
        {
            final ProjectTabPanelModuleDescriptor descriptor = (ProjectTabPanelModuleDescriptor) iterator.next();
            if (!((ProjectTabPanel) descriptor.getModule()).showPanel(ctx))
            {
                iterator.remove();
            }
        }
        Collections.sort(projectTabPanels, ModuleDescriptorComparator.COMPARATOR);
        return projectTabPanels;
    }

    /* This method sucks! */
    private GenericValue getProjectGV(final Project project)
    {
        return project.getGenericValue();
    }

    public Boolean isPriorityFieldVisible(final Long projectId)
    {
        return !new FieldVisibilityBean().isFieldHidden(projectId, IssueFieldConstants.PRIORITY, FieldVisibilityManager.ALL_ISSUE_TYPES);
    }

    public Map /* <String, Object[]> */getPriorityStats(final Long projectId)
    {
        final ProjectPortlet.PriorityStatFactory factory = new ProjectPortlet.PriorityStatFactory(constantsManager);

        final StatisticAccessorBean sab = new StatisticAccessorBean(authenticationContext.getUser(), projectId);
        try
        {
            final StatisticMapWrapper stats = sab.getAllFilterBy("priorities");
            final Map data = new ListOrderedMap();
            for (final Iterator iterator = stats.keySet().iterator(); iterator.hasNext();)
            {
                final GenericValue priorityGV = (GenericValue) iterator.next();
                data.put(factory.getPriorityId(priorityGV), factory.create(stats, priorityGV));
            }
            return data;
        }
        catch (final SearchException e)
        {
            log.error("Failed to retrieve stats for project id '" + projectId + "'", e);
            return null;
        }
    }

    public boolean hasDisplayableItems(final Project project)
    {
        final WebFragmentWebComponent webFragment = (WebFragmentWebComponent) ComponentManager.getComponentInstanceOfType(WebFragmentWebComponent.class);
        final JiraHelper jiraHelper = new JiraHelper(ActionContext.getRequest(), project);
        return webFragment.hasDisplayableItems("system.preset.filters", jiraHelper);
    }

    public String displayableItemsHtml(final Project project)
    {
        final String STACK_KEY = "Rendering Preset Filters on the dashboard";
        UtilTimerStack.push(STACK_KEY);
        final String html;
        if (hasDisplayableItems(project))
        {
            final WebFragmentWebComponent webFragment = (WebFragmentWebComponent) ComponentManager.getComponentInstanceOfType(WebFragmentWebComponent.class);
            final JiraHelper jiraHelper = new JiraHelper(ActionContext.getRequest(), project);
            html = webFragment.getHtml("templates/plugins/webfragments/system-preset-filters.vm", "system.preset.filters", jiraHelper);
        }
        else
        {
            html = "";
        }
        UtilTimerStack.pop(STACK_KEY);
        return html;
    }
}
