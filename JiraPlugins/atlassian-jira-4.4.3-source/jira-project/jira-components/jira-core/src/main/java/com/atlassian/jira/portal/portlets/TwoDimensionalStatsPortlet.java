package com.atlassian.jira.portal.portlets;


import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.search.ReaderCache;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.issue.statistics.IssueTypeStatisticsMapper;
import com.atlassian.jira.issue.statistics.ProjectStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.TwoDimensionalStatsMap;
import com.atlassian.jira.issue.statistics.util.TwoDimensionalTermHitCollector;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.searchers.ThreadLocalSearcherCache;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.QueryImpl;
import org.apache.log4j.Logger;
import org.apache.lucene.search.HitCollector;

import java.util.Map;

public class TwoDimensionalStatsPortlet extends PortletImpl
{
    private static final Logger log = Logger.getLogger(StatsPortlet.class);
    private final SearchRequestService searchRequestService;
    private final CustomFieldManager customFieldManager;
    private final SearchProvider searchProvider;
    private final ProjectManager projectManager;
    private final SearchService searchService;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final ReaderCache readerCache;

    public TwoDimensionalStatsPortlet(JiraAuthenticationContext authCtx, PermissionManager permissionManager,
                                      ApplicationProperties appProps, SearchRequestService searchRequestService,
                                      CustomFieldManager customFieldManager, SearchProvider searchProvider, ProjectManager projectManager,
                                      SearchService searchService, FieldVisibilityManager fieldVisibilityManager,
                                      ReaderCache readerCache)
    {
        super(authCtx, permissionManager, appProps);
        this.searchRequestService = searchRequestService;
        this.customFieldManager = customFieldManager;
        this.searchProvider = searchProvider;
        this.projectManager = projectManager;
        this.searchService = searchService;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.readerCache = readerCache;
    }

    protected Map getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        Map params = super.getVelocityParams(portletConfiguration);
        try
        {
            //params particular to the stats filter
            final String filterId = portletConfiguration.getProperty("filterid");
            final String xAxisType = portletConfiguration.getProperty("xAxis");
            final String yAxisOrder = portletConfiguration.getProperty("yAxisOrder");
            final String yAxisDirection = portletConfiguration.getProperty("yAxisDirection");
            final String yAxisType = portletConfiguration.getProperty("yAxis");
            final StatisticsMapper xAxisMapper = new FilterStatisticsValuesGenerator().getStatsMapper(xAxisType);
            final StatisticsMapper yAxisMapper = new FilterStatisticsValuesGenerator().getStatsMapper(yAxisType);
            final Boolean showTotals = Boolean.valueOf(portletConfiguration.getProperty("showTotals"));
            final String numberToShowString = portletConfiguration.getProperty("numberToShow");
            final Integer numberToShow =
                    (numberToShowString == null || numberToShowString.length() == 0)
                            ? new Integer(Integer.MAX_VALUE)    // if not specified set max intereger = unlimited
                            : new Integer(numberToShowString);  // otherwise set to specified value

            final JiraServiceContext ctx = new JiraServiceContextImpl(authenticationContext.getUser());
            final SearchRequest request = searchRequestService.getFilter(ctx, new Long(filterId));

            params.put("searchRequest", request);

            if (request == null)
            {
                params.put("user", authenticationContext.getUser());
            }
            else
            {
                TwoDimensionalStatsMap groupedCounts = searchCountMap(request, xAxisMapper, yAxisMapper);
                params.put("twoDStatsMap", groupedCounts);
                params.put("xAxisType", xAxisType);
                params.put("yAxisType", yAxisType);
                params.put("customFieldManager", customFieldManager);
                params.put("showTotals", showTotals);
                params.put("yAxisOrder", yAxisOrder);
                params.put("yAxisDirection", yAxisDirection);
                params.put("numberToShow", numberToShow);
                params.put("portlet", this);
                params.put("portlet", this);
                params.put("projectManager", projectManager);
            }
        }
        catch (Exception e)
        {
            log.error("Could not create velocity parameters " + e.getMessage(), e);
        }
        return params;
    }

    protected TwoDimensionalStatsMap searchCountMap(SearchRequest request, StatisticsMapper xAxisStatsMapper, StatisticsMapper yAxisStatsMapper)
            throws SearchException
    {
        TwoDimensionalStatsMap statsMap = new TwoDimensionalStatsMap(xAxisStatsMapper, yAxisStatsMapper);
        HitCollector hitCollector = new TwoDimensionalTermHitCollector(statsMap, ThreadLocalSearcherCache.getReader(), fieldVisibilityManager, readerCache);

        searchProvider.search((request != null) ? request.getQuery() : null, authenticationContext.getUser(), hitCollector);

        return statsMap;
    }


    // ---------------------------------------------------------------------------------------------- Known view helpers
    public String getSearchUrlForHeaderCell(Object xAxisObject, StatisticsMapper xAxisMapper, SearchRequest searchRequest)
    {
        SearchRequest searchUrlSuffix = xAxisMapper.getSearchUrlSuffix(xAxisObject, searchRequest);
        return searchUrlSuffix != null ? searchService.getQueryString(authenticationContext.getUser(), (searchUrlSuffix == null) ? new QueryImpl() : searchUrlSuffix.getQuery()) : "";
    }

    public String getSearchUrlForCell(Object xAxisObject, Object yAxisObject, TwoDimensionalStatsMap statsMap, SearchRequest searchRequest)
    {
        StatisticsMapper xAxisMapper = statsMap.getxAxisMapper();
        StatisticsMapper yAxisMapper = statsMap.getyAxisMapper();

        SearchRequest srAfterSecond;
        if (isFirst(yAxisMapper, xAxisMapper))
        {
            SearchRequest srAfterFirst = yAxisMapper.getSearchUrlSuffix(yAxisObject, searchRequest);
            srAfterSecond = xAxisMapper.getSearchUrlSuffix(xAxisObject, srAfterFirst);
        }
        else
        {
            SearchRequest srAfterFirst = xAxisMapper.getSearchUrlSuffix(xAxisObject, searchRequest);
            srAfterSecond = yAxisMapper.getSearchUrlSuffix(yAxisObject, srAfterFirst);
        }

        return srAfterSecond != null ? searchService.getQueryString(authenticationContext.getUser(), srAfterSecond.getQuery()) : "";
    }

    public int getYAxisUniqueTotal(TwoDimensionalStatsMap statsMap)
    {
        return statsMap.getYAxisUniqueTotal(FilterStatisticsValuesGenerator.IRRELEVANT);
    }

    public int getXAxisUniqueTotal(TwoDimensionalStatsMap statsMap)
    {
        return statsMap.getXAxisUniqueTotal(FilterStatisticsValuesGenerator.IRRELEVANT);
    }

    // -------------------------------------------------------------------------------------------------- Private helper
    private boolean isFirst(StatisticsMapper a, StatisticsMapper b)
    {
        if (a instanceof ProjectStatisticsMapper)
        {
            return true;
        }
        else if (b instanceof ProjectStatisticsMapper)
        {
            return false;
        }
        else if (a instanceof IssueTypeStatisticsMapper)
        {
            return true;
        }
        else if (b instanceof IssueTypeStatisticsMapper)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}