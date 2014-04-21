package com.atlassian.jira.portal.portlets;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import org.apache.log4j.Logger;

import java.util.Map;

public class StatsPortlet extends PortletImpl
{
    private static final Logger log = Logger.getLogger(StatsPortlet.class);
    private final SearchRequestService searchRequestService;
    private final CustomFieldManager customFieldManager;
    private final ProjectManager projectManager;

    public StatsPortlet(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager, ApplicationProperties applicationProperties, SearchRequestService searchRequestService, CustomFieldManager customFieldManager, ProjectManager projectManager)
    {
        super(authenticationContext, permissionManager, applicationProperties);
        this.searchRequestService = searchRequestService;
        this.customFieldManager = customFieldManager;
        this.projectManager = projectManager;
    }

    protected Map getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        Map params = super.getVelocityParams(portletConfiguration);
        try
        {
            //params particular to the stats filter
            final String filterId = portletConfiguration.getProperty("filterid");
            final String sortOrder = portletConfiguration.getProperty("sortOrder");
            final String sortDirection = portletConfiguration.getProperty("sortDirection");
            final JiraServiceContext ctx = new JiraServiceContextImpl(authenticationContext.getUser());
            final SearchRequest request = searchRequestService.getFilter(ctx, new Long(filterId));
            
            params.put("searchRequest", request);
            params.put("statsBean", new StatisticAccessorBean(authenticationContext.getUser(), request));
            params.put("customFieldManager", customFieldManager);
            params.put("user", authenticationContext.getUser());
            params.put("portlet", this);
            params.put("projectManager", projectManager);
            params.put("sortOrder", StatisticAccessorBean.OrderBy.get(sortOrder));
            params.put("sortDirection", StatisticAccessorBean.Direction.get(sortDirection));
        }
        catch (Exception e)
        {
            log.error("Could not create velocity parameters " + e.getMessage(), e);
        }
        return params;
    }
}
