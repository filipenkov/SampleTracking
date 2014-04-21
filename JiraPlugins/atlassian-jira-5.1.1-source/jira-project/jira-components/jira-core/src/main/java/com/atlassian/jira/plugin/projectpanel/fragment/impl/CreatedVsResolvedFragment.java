package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;

import java.util.Map;

import static com.atlassian.jira.charts.ChartFactory.ChartContext;
import static com.atlassian.jira.charts.ChartFactory.FRAGMENT_IMAGE_HEIGHT;
import static com.atlassian.jira.charts.ChartFactory.FRAGMENT_IMAGE_WIDTH;
import static com.atlassian.jira.charts.ChartFactory.VersionLabel;

/**
 * Displays created vs resolved chart fragment for a project.
 *
 * @since v4.0
 */
public class CreatedVsResolvedFragment extends AbstractFragment
{
    protected static final String TEMPLATE_DIRECTORY_PATH = "templates/plugins/jira/projectpanels/fragments/summary/";
    private final ChartFactory chartFactory;

    public CreatedVsResolvedFragment(final VelocityTemplatingEngine templatingEngine,
            final JiraAuthenticationContext jiraAuthenticationContext, final ChartFactory chartFactory)
    {
        super(templatingEngine, jiraAuthenticationContext);
        this.chartFactory = chartFactory;
    }

    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        final Map<String, Object> params = super.createVelocityParams(ctx);

        final SearchRequest sr = new SearchRequest(ctx.createQuery());

        final ChartContext context = new ChartContext(ctx.getUser(), sr, FRAGMENT_IMAGE_WIDTH, FRAGMENT_IMAGE_HEIGHT);
        final Chart chart = chartFactory.generateCreatedVsResolvedChart(context, 30, ChartFactory.PeriodName.daily, VersionLabel.major, true, false);
        params.putAll(chart.getParameters());
        return params;
    }

    protected String getTemplateDirectoryPath()
    {
        return TEMPLATE_DIRECTORY_PATH;
    }

    // NOTE: We need to ensure that all IDs are unique (as required by HTML specification).
    // When implementing ProjectTabPanelFragment ensure you add it to TestProjectTabPanelFragment test!
    public String getId()
    {
        return "createdvsresolved";
    }

    public boolean showFragment(final BrowseContext ctx)
    {
        return true;
    }
}
