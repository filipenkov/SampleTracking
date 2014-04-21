package com.atlassian.jira.portal.portlets;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public class ProjectStatsPortlet extends PortletImpl
{
    private static final Logger log = Logger.getLogger(ProjectStatsPortlet.class);

    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final CustomFieldManager customFieldManager;

    public ProjectStatsPortlet(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties, ProjectManager projectManager, PermissionManager permissionManager, CustomFieldManager customFieldManager)
    {
        super(authenticationContext, permissionManager, applicationProperties);
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.customFieldManager = customFieldManager;
    }

    protected Map getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        Map params = super.getVelocityParams(portletConfiguration);
        try
        {
            String projectId = portletConfiguration.getProperty("projectid");
            Boolean showClosedIssueStatistics = Boolean.valueOf(portletConfiguration.getProperty("showclosed"));
            final String sortOrder = portletConfiguration.getProperty("sortOrder");
            final String sortDirection = portletConfiguration.getProperty("sortDirection");
            GenericValue project = projectManager.getProject(new Long(projectId));

            if (project == null)
            {
                // Portlet associated with invalid project - display to user (if logged in)
                params.put("invalidProjectReference", projectId);
                params.put("user", authenticationContext.getUser());
            }
            else if (permissionManager.hasPermission(Permissions.BROWSE, project, authenticationContext.getUser()))
            {
                params.put("project", project);
                params.put("statsBean", new StatisticAccessorBean(authenticationContext.getUser(), project.getLong("id"), !showClosedIssueStatistics.booleanValue()));
                params.put("customFieldManager", customFieldManager);
                params.put("portlet", this);
                params.put("sortOrder", StatisticAccessorBean.OrderBy.get(sortOrder));
                params.put("sortDirection", StatisticAccessorBean.Direction.get(sortDirection));
            }
        }
        catch (ObjectConfigurationException e)
        {
            log.error(e, e);
        }
        return params;
    }

}
