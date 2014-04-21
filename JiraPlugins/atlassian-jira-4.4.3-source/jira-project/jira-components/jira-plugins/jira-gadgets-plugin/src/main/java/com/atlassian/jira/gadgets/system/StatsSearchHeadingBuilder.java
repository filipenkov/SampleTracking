package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.TwoDimensionalStatsMap;
import com.atlassian.jira.issue.statistics.ProjectStatisticsMapper;
import com.atlassian.jira.issue.statistics.IssueTypeStatisticsMapper;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.security.JiraAuthenticationContext;

/**
 * Extracted to an interface to allow this behavior to be easily stubbed out.
 */
interface StatsSearchUrlBuilder
{
    String getSearchUrlForCell(Object xAxisObject, Object yAxisObject, TwoDimensionalStatsMap statsMap, SearchRequest searchRequest);

    String getSearchUrlForHeaderCell(Object axisObject, StatisticsMapper axisMapper, SearchRequest searchRequest);
}

