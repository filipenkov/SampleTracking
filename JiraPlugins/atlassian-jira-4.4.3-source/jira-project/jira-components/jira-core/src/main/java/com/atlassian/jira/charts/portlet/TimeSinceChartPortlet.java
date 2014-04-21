package com.atlassian.jira.charts.portlet;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Map;

import static com.atlassian.jira.charts.ChartFactory.PORTLET_IMAGE_HEIGHT;
import static com.atlassian.jira.charts.ChartFactory.PORTLET_IMAGE_WIDTH;
import static com.atlassian.jira.charts.ChartFactory.PeriodName;

/**
 * Displays a bar graph showing the average time issues have been open for for a particular time period.
 *
 * @since v4.0
 */
public class TimeSinceChartPortlet extends AbstractChartPortlet implements SearchRequestAwareChartPortlet
{
    private static final Logger log = Logger.getLogger(TimeSinceChartPortlet.class);
    private final ChartFactory chartFactory;


    public TimeSinceChartPortlet(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager,
            ApplicationProperties applicationProperties, VelocityRequestContextFactory velocityRequestContextFactory,
            ChartUtils chartUtils, ChartFactory chartFactory)
    {
        super(authenticationContext, permissionManager, applicationProperties, velocityRequestContextFactory, chartUtils);
        this.chartFactory = chartFactory;
    }

    protected Map<String, Object> getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        final Map<String, Object> params = getVelocityParams(portletConfiguration, null);
        params.put("showReportLink", Boolean.TRUE);
        return params;
    }

    public String getSearchRequestViewHtml(PortletConfiguration portletConfiguration, SearchRequest searchRequest)
    {
        final Map<String, Object> params = getVelocityParams(portletConfiguration, searchRequest);
        params.put("showReportLink", Boolean.FALSE);
        return getDescriptor().getHtml("view", params);
    }

    private Map<String, Object> getVelocityParams(PortletConfiguration portletConfiguration, SearchRequest searchRequest)
    {
        final Map<String, Object> params = generateCommonParameters(portletConfiguration);
        try
        {
            params.put("loggedin", (authenticationContext.getUser() != null) ? Boolean.TRUE : Boolean.FALSE);
            params.put("indexing", applicationProperties.getOption(APKeys.JIRA_OPTION_INDEXING));

            // Retrieve the portlet parameters
            final String projectOrFilterId = portletConfiguration.getProperty("projectOrFilterId");
            final PeriodName periodName = PeriodName.valueOf(portletConfiguration.getProperty("periodName"));

            int days = getDaysPrevious(portletConfiguration);

            final String cumulativeString = portletConfiguration.getProperty("cumulative");
            final boolean cumulative = cumulativeString != null && cumulativeString.equalsIgnoreCase("true");

            final String dateFieldId = portletConfiguration.getProperty("dateField");

            SearchRequest request = searchRequest;
            if (StringUtils.isNotEmpty(projectOrFilterId) && request == null)
            {
                params.put("projectOrFilterId", projectOrFilterId);
                request = chartUtils.retrieveOrMakeSearchRequest(projectOrFilterId, params);
            }

            if (request != null)
            {
                params.put("searchRequest", request);
                final ChartFactory.ChartContext context =
                        new ChartFactory.ChartContext(authenticationContext.getUser(), request, PORTLET_IMAGE_WIDTH, PORTLET_IMAGE_HEIGHT);
                final Chart chart = chartFactory.generateTimeSinceChart(context, days, periodName, cumulative, dateFieldId);
                params.putAll(chart.getParameters());
            }
        }
        catch (ObjectConfigurationException e)
        {
            log.error("Could not create velocity parameters.", e);
        }
        catch(RuntimeException e)
        {
            log.error("Could not create velocity parameters. Unknown error.", e);
        }

        return params;
    }
}