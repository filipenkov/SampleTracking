package com.atlassian.jira.charts.portlet;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.portal.Portlet;
import com.atlassian.jira.portal.PortletConfiguration;

/**
 * Marker interface to identify any charting portlets.  This is used to mark any charts to displayed in the charting
 * SearchRequest view ({@see com.atlassian.jira.web.action.charts.SavePortletSearchViewConfiguration}).
 *
 * @since v4.0
 */
public interface SearchRequestAwareChartPortlet extends Portlet
{
    /**
     * Renders the SearchRequest View HTML for a given portlet configuration and searchrequest.
     *
     * @param portletConfiguration The properties to use for the chart portlet to render
     * @param searchRequest The search request, defining the data to chart
     * @return The HTML of the chart portlet
     */
    String getSearchRequestViewHtml(PortletConfiguration portletConfiguration, SearchRequest searchRequest);
}