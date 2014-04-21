package com.atlassian.jira.charts.util;

import com.atlassian.jira.issue.search.SearchRequest;

import java.util.Map;

/**
 * Utility class for charting
 *
 * @since v4.0
 */
public interface ChartUtils
{
    /**
     * Make a search request out of the searchQuery parameter and populate the report/portlet param map
     *
     * @param searchQuery a jql string, a project id or a filter id
     * @param params            the map of parameters to modify
     * @return The SearchRequest for the project/filter id
     */
    SearchRequest retrieveOrMakeSearchRequest(String searchQuery, Map<String, Object> params);
}
