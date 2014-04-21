package com.atlassian.jira.portal.portlets;

import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.portal.LazyLoadingPortlet;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.web.portlet.bean.PortletRenderer;
import org.apache.log4j.Logger;

import java.util.Map;

public class SavedFiltersPortlet extends AbstractRequiresUserPortlet implements LazyLoadingPortlet
{
    private static final Logger log = Logger.getLogger(SavedFiltersPortlet.class);
    private final SearchProvider searchProvider;
    private final SearchRequestService searchRequestService;

    public SavedFiltersPortlet(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager,
            ApplicationProperties applicationProperties, SearchRequestService searchRequestService, SearchProvider searchProvider)
    {
        super(authenticationContext, permissionManager, applicationProperties);
        this.searchRequestService = searchRequestService;
        this.searchProvider = searchProvider;
    }

    protected Map getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        Map params = super.getVelocityParams(portletConfiguration);
        try
        {
            //params particular to the stats filter
            params.put("savedFilters", searchRequestService.getFavouriteFilters(authenticationContext.getUser()));
            params.put("portlet", this);
        }
        catch (Exception e)
        {
            log.error("Could not create velocity parameters " + e.getMessage(), e);
        }
        return params;
    }

    public long getCountsForFilter(SearchRequest filter) throws SearchException
    {
        if (filter == null)
        {
            return 0;
        }
        return searchProvider.searchCount(filter.getQuery(), authenticationContext.getUser());
    }

    public String getLoadingHtml(PortletConfiguration portletConfiguration)
    {
        if (authenticationContext.getUser() == null)
        {
            return PortletRenderer.RENDER_NO_OUTPUT_AND_NO_AJAX_CALLHOME;
        }

        Map params = super.getVelocityParams(portletConfiguration);
        try
        {
            //params particular to the stats filter
            params.put("savedFilters", searchRequestService.getFavouriteFilters(authenticationContext.getUser()));
            params.put("portlet", this);
            params.put("indexing", Boolean.FALSE);
            params.put("loading", Boolean.TRUE);
        }
        catch (Exception e)
        {
            log.error("Could not create velocity parameters " + e.getMessage(), e);
        }

        return getDescriptor().getHtml("loading", params);
    }

    public String getStaticHtml(PortletConfiguration portletConfiguration)
    {
        return "";
    }
}
