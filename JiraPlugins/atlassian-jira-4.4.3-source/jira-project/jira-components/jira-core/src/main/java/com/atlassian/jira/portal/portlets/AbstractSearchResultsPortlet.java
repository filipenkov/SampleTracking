package com.atlassian.jira.portal.portlets;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.portal.ColumnNamesValuesGenerator;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.bean.PermissionCheckBean;
import com.atlassian.jira.web.component.IssueTableLayoutBean;
import com.atlassian.jira.web.component.IssueTableWebComponent;
import com.atlassian.jira.web.component.TableLayoutFactory;
import com.atlassian.jira.web.util.IssueTableBean;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

public abstract class AbstractSearchResultsPortlet extends AbstractRequiresUserPortlet
{
    private static final Logger log = Logger.getLogger(AbstractSearchResultsPortlet.class);

    protected final ConstantsManager constantsManager;
    protected final SearchProvider searchProvider;
    protected final TableLayoutFactory tableLayoutFactory;

    private static final class Property
    {
        public static final String NUM_OF_ENTRIES = "numofentries";
        public static final String COLUMNS = "columns";
        public static final String SHOW_HEADER = "showHeader";
    }


    protected AbstractSearchResultsPortlet(JiraAuthenticationContext authenticationContext,
                                           PermissionManager permissionManager, ConstantsManager constantsManager,
                                           SearchProvider searchProvider, ApplicationProperties applicationProperties,
                                           TableLayoutFactory tableLayoutFactory)
    {
        super(authenticationContext, permissionManager, applicationProperties);
        this.constantsManager = constantsManager;
        this.searchProvider = searchProvider;
        this.tableLayoutFactory = tableLayoutFactory;
    }

    /**
     * this constructor is for backwards compatibility for old portlets
     * @deprecated Use {@link #AbstractSearchResultsPortlet(com.atlassian.jira.security.JiraAuthenticationContext, com.atlassian.jira.security.PermissionManager, com.atlassian.jira.config.ConstantsManager, com.atlassian.jira.issue.search.SearchProvider, com.atlassian.jira.config.properties.ApplicationProperties, com.atlassian.jira.web.component.TableLayoutFactory)} instead
     */
    protected AbstractSearchResultsPortlet(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager, ConstantsManager constantsManager, SearchProvider searchProvider, ApplicationProperties applicationProperties)
    {
        this(authenticationContext, permissionManager, constantsManager, searchProvider, applicationProperties, ComponentManager.getComponent(TableLayoutFactory.class));
    }

    protected Map<String, Object> getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        Map<String, Object> params = super.getVelocityParams(portletConfiguration);
        try
        {
            params.put("searchTypeName", getSearchTypeName());

            int maxEntryCount = getMaxNumberOfIssues(portletConfiguration);
            boolean showHeader = Boolean.parseBoolean(portletConfiguration.getProperty(Property.SHOW_HEADER));
            SearchRequest searchRequest = getSearchRequest(portletConfiguration);
            SearchResults searchResults = getSearchResults(maxEntryCount, searchRequest);
            params.put("searchRequest", searchRequest);
            params.put("portlet", this);
            if (searchRequest != null)
            {
                final int totalCount;
                final int totalNumIssues;
                if (searchResults == null)
                {
                    totalCount = 0;
                    totalNumIssues = 0;
                }
                else
                {
                    totalCount = searchResults.getEnd();
                    totalNumIssues = searchResults.getTotal();

                    List issues = searchResults.getIssues();
                    if (issues != null && !issues.isEmpty())
                    {
                        params.put("issues", issues);
                        IssueTableLayoutBean dashboardLayout = getLayout(portletConfiguration);
                        dashboardLayout.setDisplayHeader(showHeader);
                        params.put("table", new IssueTableWebComponent().getHtml(dashboardLayout, issues, null));
                    }
                }
                params.put("displayedIssueCount", totalCount);
                params.put("totalNumIssues", totalNumIssues);
                params.put("constantsManager", constantsManager);
                params.put("issueBean", new IssueTableBean());
                params.put("fieldVisibility", new FieldVisibilityBean());
                params.put("linkToSearch", getLinkToSearch(searchRequest, portletConfiguration));
                params.put("searchName", getSearchName(searchRequest));
                params.put("noIssuesText", getNoIssuesText());
                params.put("permissionCheck", new PermissionCheckBean(authenticationContext, permissionManager));
            }
            else
            {
                params.put("user", authenticationContext.getUser());
            }
        }
        catch (Exception e)
        {
            log.error("Could not create velocity parameters " + e.getMessage(), e);
        }
        return params;
    }

    /**
     * Override to provide your own column layouts
     */
    protected IssueTableLayoutBean getLayout(PortletConfiguration portletConfiguration) throws FieldException
    {
        try
        {
            final String columnNamesFromUI = portletConfiguration.getProperty(Property.COLUMNS);
            List<String> columnNames = getListFromMultiSelectValue(columnNamesFromUI);
            if (onlyColumnSelectedIsDefaultColumns(columnNames))
                columnNames = null;
            return tableLayoutFactory.getDashboardLayout(authenticationContext.getUser(), columnNames);
        }
        catch (ObjectConfigurationException e)
        {
            log.error(e, e);
        }
        return tableLayoutFactory.getDashboardLayout(authenticationContext.getUser(), null);

    }

    private boolean onlyColumnSelectedIsDefaultColumns(List columnNames)
    {
        return columnNames == null || columnNames.size() == 1 && columnNames.contains(ColumnNamesValuesGenerator.Property.DEFAULT_COLUMNS);
    }

    protected int getMaxNumberOfIssues(PortletConfiguration portletConfiguration)
    {
        int maxEntryCount = 0;
        try
        {
            final Long longProp = portletConfiguration.getLongProperty(Property.NUM_OF_ENTRIES);
            maxEntryCount = longProp == null ? 10 : longProp.intValue();
        }
        catch (ObjectConfigurationException e)
        {
            log.error(e, e);
        }
        return maxEntryCount;
    }

    /**
     * Returns search results if the user is logged in and serach request passed in is not null
     *
     * @param displayIssueCount number of issues to display
     * @param searchRequest     search request
     * @return search results
     */
    private SearchResults getSearchResults(int displayIssueCount, SearchRequest searchRequest)
    {
        com.atlassian.crowd.embedded.api.User user = authenticationContext.getLoggedInUser();
        if (user == null || searchRequest == null)
        {
            return null;
        }

        try
        {
            PagerFilter pagerFilter = new PagerFilter(displayIssueCount);
            return searchProvider.search(searchRequest.getQuery(), user, pagerFilter);
        }
        catch (SearchException e)
        {
            log.error("Could not get issues", e);
            return null;
        }
    }

    protected abstract SearchRequest getSearchRequest(PortletConfiguration portletConfiguration);

    protected abstract String getLinkToSearch(SearchRequest searchRequest, PortletConfiguration portletConfiguration);

    protected abstract String getSearchName(SearchRequest searchRequest);

    protected abstract String getSearchTypeName();

    protected abstract String getNoIssuesText();
}
