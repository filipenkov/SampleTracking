package com.atlassian.jira.charts.report;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.jfreechart.TimePeriodUtils;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.web.action.ProjectActionSupport;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.charts.ChartFactory.ChartContext;
import static com.atlassian.jira.charts.ChartFactory.PeriodName;
import static com.atlassian.jira.charts.ChartFactory.REPORT_IMAGE_HEIGHT;
import static com.atlassian.jira.charts.ChartFactory.REPORT_IMAGE_WIDTH;

/**
 * A report showing resolution time for a given project or search request.
 *
 * @since v4.0
 */
public class ResolutionTimeReport extends AbstractChartReport
{
    private final ChartFactory chartFactory;
    private final TimeZoneManager timeZoneManager;

    public ResolutionTimeReport(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties,
            ProjectManager projectManager, SearchRequestService searchRequestService, ChartUtils chartUtils,
            ChartFactory chartFactory, TimeZoneManager timeZoneManager)
    {
        super(authenticationContext, applicationProperties, projectManager, searchRequestService, chartUtils);
        this.chartFactory = chartFactory;
        this.timeZoneManager = timeZoneManager;
    }

    public String generateReportHtml(ProjectActionSupport action, Map reqParams) throws Exception
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("report", this);
        params.put("action", action);
        params.put("indexing", (applicationProperties.getOption(APKeys.JIRA_OPTION_INDEXING)) ? Boolean.TRUE : Boolean.FALSE);
        params.put("user", authenticationContext.getLoggedInUser());
        params.put("timePeriods", new TimePeriodUtils(timeZoneManager));

        // Retrieve the portlet parameters
        final String projectOrFilterId = (String) reqParams.get("projectOrFilterId");
        final PeriodName periodName = PeriodName.valueOf((String) reqParams.get("periodName"));

        int days = 30;

        if (reqParams.containsKey("daysprevious"))
        {
            days = Integer.parseInt((String) reqParams.get("daysprevious"));
        }

        final SearchRequest request = chartUtils.retrieveOrMakeSearchRequest(projectOrFilterId, params);
        params.put("projectOrFilterId", projectOrFilterId);

        final ChartContext context = new ChartContext(authenticationContext.getLoggedInUser(), request, REPORT_IMAGE_WIDTH, REPORT_IMAGE_HEIGHT);
        final Chart chart = chartFactory.generateDateRangeTimeChart(context, days, periodName, DateUtils.DAY_MILLIS,
                "datacollector.daystoresolve", DocumentConstants.ISSUE_RESOLUTION_DATE);
        params.putAll(chart.getParameters());

        return descriptor.getHtml("view", params);
    }
}