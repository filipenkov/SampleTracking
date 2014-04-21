package com.atlassian.jira.charts.portlet;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

/**
 * A chart that shows created vs resolved issues over time.
 *
 * @since v4.0
 */
public class ResolutionTimeChartPortlet extends AbstractTimeChartPortlet
{
    public ResolutionTimeChartPortlet(JiraAuthenticationContext authenticationContext,
            PermissionManager permissionManager, ApplicationProperties applicationProperties,
            VelocityRequestContextFactory velocityRequestContextFactory, ChartUtils chartUtils,
            ChartFactory chartFactory)
    {
        super(authenticationContext, permissionManager, applicationProperties, velocityRequestContextFactory, chartUtils,
                chartFactory);
    }

    protected long getYAxisTimePeriod()
    {
        return DateUtils.DAY_MILLIS;
    }

    protected String getReportKey()
    {
        return "com.atlassian.jira.plugin.system.reports%3Aresolutiontime-report";
    }

    protected String getI18nPrefix()
    {
        return "portlet.resolutiontime";
    }

    protected String getDateFieldId()
    {
        return DocumentConstants.ISSUE_RESOLUTION_DATE;
    }

    protected String getLabelSuffixKey()
    {
        return "datacollector.daystoresolve";
    }
}
