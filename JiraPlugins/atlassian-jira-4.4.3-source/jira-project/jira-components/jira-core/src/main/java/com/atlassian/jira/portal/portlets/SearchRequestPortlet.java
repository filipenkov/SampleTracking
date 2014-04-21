package com.atlassian.jira.portal.portlets;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.web.component.TableLayoutFactory;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class SearchRequestPortlet extends AbstractSearchResultsPortlet
{
    private static final Logger log = Logger.getLogger(SearchRequestPortlet.class);

    private final SearchRequestService searchRequestService;

    public SearchRequestPortlet(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager,
            ConstantsManager constantsManager, SearchProvider searchProvider,
            ApplicationProperties applicationProperties, SearchRequestService searchRequestService, TableLayoutFactory tableLayoutFactory)
    {
        super(authenticationContext, permissionManager, constantsManager, searchProvider, applicationProperties, tableLayoutFactory);
        this.searchRequestService = searchRequestService;
    }

    protected SearchRequest getSearchRequest(PortletConfiguration portletConfiguration)
    {
        try
        {
            Long filterId = portletConfiguration.getLongProperty("filterid");
            return getSearchRequest(filterId);
        }
        catch (ObjectConfigurationException e)
        {
            log.error(e, e);
        }
        return null;
    }

    private SearchRequest getSearchRequest(Long filterId)
    {
        try
        {
            final User remoteUser = authenticationContext.getUser();
            return searchRequestService.getFilter(new JiraServiceContextImpl(remoteUser), filterId);
        }
        catch (DataAccessException e)
        {
            log.error(e, e);
        }
        return null;
    }

    protected String getLinkToSearch(SearchRequest searchRequest, PortletConfiguration portletConfiguration)
    {
        return "secure/IssueNavigator.jspa?requestId=" + searchRequest.getId() + "&mode=hide";
    }

    protected String getSearchName(SearchRequest searchRequest)
    {
        return searchRequest.getName();
    }

    protected String getSearchTypeName()
    {
        return authenticationContext.getI18nHelper().getText("portlet.savedfilter.issues");
    }

    protected String getNoIssuesText()
    {
        return authenticationContext.getI18nHelper().getText("portlet.savedfilter.noissues");
    }

    protected Map getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        Map params = new HashMap();
        params.putAll(super.getVelocityParams(portletConfiguration));
        params.put("description", getDescription(portletConfiguration));
        return params;
    }

    protected String getDescription(PortletConfiguration portletConfiguration)
    {
        String description = null;
        try
        {
            if (Boolean.valueOf(portletConfiguration.getProperty("showdescription")).booleanValue())
            {
                Long filterId = portletConfiguration.getLongProperty("filterid");
                SearchRequest sr = getSearchRequest(filterId);
                if (sr != null)
                {
                    description = sr.getDescription();
                }
            }
        }
        catch (ObjectConfigurationException e)
        {
            log.error(e, e);
        }

        return description;
    }


}
