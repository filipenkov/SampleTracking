package com.atlassian.jira.portal.portlets;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
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
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Portlet that displays information about a single project.
 *
 * @since v3.13
 */
public class ProjectPortlet extends PortletImpl
{
    private static final Logger log = Logger.getLogger(ProjectPortlet.class);

    private final ProjectManager projectManager;
    private final PluginAccessor pluginAccessor;
    private final ConstantsManager constantsManager;

    public ProjectPortlet(final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager, final ApplicationProperties applicationProperties, final ProjectManager projectManager, final PluginAccessor pluginAccessor, final ConstantsManager constantsManager)
    {
        super(authenticationContext, permissionManager, applicationProperties);
        this.projectManager = projectManager;
        this.pluginAccessor = pluginAccessor;
        this.constantsManager = constantsManager;
    }

    protected Map<String, Object> getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        final HttpServletRequest request = ActionContext.getContext().getRequestImpl();
        final Project project = getProject(getProperty(portletConfiguration, "projectid"));

        final Map<String, Object> params = super.getVelocityParams(portletConfiguration);

        params.put("redirectUtils", new RedirectUtils());
        params.put("projectsExist", !projectManager.getProjects().isEmpty());
        params.put("portletConfigurationId", portletConfiguration.getId());

        params.put("isGoodBrowser", ProjectPortletHelper.isGoodBrowser(request));

        params.put("showToggle", Boolean.FALSE);
        if (project != null)
        {
            params.put("project", project);
            params.put("hasBrowsePermission", hasBrowsePermission(project));
            params.put("tabPanels", getProjectTabPanels(project));
            final Long projectId = project.getId();
            params.put("isPriorityVisible", isPriorityFieldVisible(projectId));
            params.put("priorityStats", getPriorityStats(projectId));
        }

        renderFilters(params, project, request);

        return params;
    }

    private void renderFilters(final Map<String, Object> params, final Project project, final HttpServletRequest request)
    {
        final String STACK_KEY = "Rendering Preset Filters on the dashboard";

        UtilTimerStack.push(STACK_KEY);
        final WebFragmentWebComponent webFragment = (WebFragmentWebComponent) ComponentManager.getComponentInstanceOfType(WebFragmentWebComponent.class);
        final JiraHelper jiraHelper = new JiraHelper(request, project);
        final boolean hasDisplayableItems = webFragment.hasDisplayableItems("system.preset.filters", jiraHelper);
        final String html;
        if (hasDisplayableItems)
        {
            html = webFragment.getHtml("templates/plugins/webfragments/system-preset-filters.vm", "system.preset.filters", jiraHelper);
        }
        else
        {
            html = "";
        }
        UtilTimerStack.pop(STACK_KEY);

        params.put("hasDisplayableItems", hasDisplayableItems);
        params.put("displayableItemsHtml", html);
    }

    public Map<String, ProjectPortlet.PriorityStat> getPriorityStats(final Long projectId)
    {
        final PriorityStatFactory factory = new PriorityStatFactory(constantsManager);

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

    private Boolean isPriorityFieldVisible(final Long projectId)
    {
        return !new FieldVisibilityBean().isFieldHidden(projectId, IssueFieldConstants.PRIORITY, FieldVisibilityManager.ALL_ISSUE_TYPES);
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

    /**
     * Finds and returns project by given id
     *
     * @param projectId project id
     * @return found project or null if not found
     */
    private Project getProject(final String projectId)
    {
        try
        {
            return projectManager.getProjectObj(new Long(projectId));
        }
        catch (final NumberFormatException e)
        {
            if (log.isInfoEnabled())
            {
                log.info("Misconfigured project portlet, project id expected to be a Long but is '" + projectId + "'");
            }
        }
        return null;
    }

    private String getProperty(final PortletConfiguration portletConfiguration, final String s)
    {
        try
        {
            return portletConfiguration.getProperty(s);
        }
        catch (final ObjectConfigurationException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Boolean hasBrowsePermission(final Project project)
    {
        return (project != null) && permissionManager.hasPermission(Permissions.BROWSE, project, authenticationContext.getUser());
    }

    static class PriorityStatFactory
    {
        private static final String DEFAULT_COLOR = "#cccccc";
        private final ConstantsManager constantsManager;

        public PriorityStatFactory(final ConstantsManager constantsManager)
        {
            this.constantsManager = constantsManager;
        }

        PriorityStat create(final StatisticMapWrapper stats, final GenericValue priorityGV)
        {
            final String priorityId = getPriorityId(priorityGV);
            return new PriorityStat(priorityId, (Integer) stats.get(priorityGV), constantsManager.getPriorityName(getPriorityId(priorityGV)),
                    stats.getPercentage(priorityGV), getColor(priorityGV));
        }

        String getPriorityId(final GenericValue priorityGV)
        {
            return priorityGV == null ? "-1" : priorityGV.getString("id");
        }

        private String getColor(final GenericValue priorityGV)
        {
            return (getPriorityId(priorityGV) == null) || (priorityGV == null) ? DEFAULT_COLOR : priorityGV.getString("statusColor");
        }

    }

    public static class PriorityStat
    {
        private final String id;
        private final Object count;
        private final String name;
        private final long percentage;
        private final String color;

        public PriorityStat(final String id, final Integer count, final String name, final long percentage, final String color)
        {
            this.id = id;
            this.count = count;
            this.name = name;
            this.percentage = percentage;
            this.color = color;
        }

        public String getId()
        {
            return id;
        }

        public String getColor()
        {
            return color;
        }

        public String getName()
        {
            return name;
        }

        public long getPercentage()
        {
            return percentage;
        }

        public Object getCount()
        {
            return count;
        }
    }

}
