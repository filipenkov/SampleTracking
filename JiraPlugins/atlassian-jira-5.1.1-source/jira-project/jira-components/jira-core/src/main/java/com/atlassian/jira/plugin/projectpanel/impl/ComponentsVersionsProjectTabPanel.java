package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.jira.web.bean.StatisticMapWrapper;
import com.google.common.collect.Maps;

import java.util.Map;

public class ComponentsVersionsProjectTabPanel extends AbstractProjectTabPanel
{
    private final FieldVisibilityManager fieldVisibilityManager;

    public ComponentsVersionsProjectTabPanel(final JiraAuthenticationContext authenticationContext,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        super(authenticationContext);
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    public String getHtml(BrowseContext ctx)
    {
        try
        {
            final Map<String, Object> startingParams = Maps.newHashMap();
            startingParams.put("project", ctx.getProject());
            startingParams.put("fieldVisibility",fieldVisibilityManager);
            startingParams.put("i18n", authenticationContext.getI18nHelper());

            StatisticAccessorBean statisticAccessorBean = new StatisticAccessorBean(authenticationContext.getLoggedInUser(), ctx.getProject().getId());
            StatisticMapWrapper componentStats = statisticAccessorBean.getWrapper(new FilterStatisticsValuesGenerator().getStatsMapper(FilterStatisticsValuesGenerator.COMPONENTS));
            StatisticMapWrapper versionStats = statisticAccessorBean.getWrapper(new FilterStatisticsValuesGenerator().getStatsMapper(FilterStatisticsValuesGenerator.ALLFIXFOR));
            startingParams.put("componentStats", componentStats);
            startingParams.put("versionStats", versionStats);
            return descriptor.getHtml("view", startingParams);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean showPanel(BrowseContext ctx)
    {
        final Long projectId = ctx.getProject().getId();
        return !(fieldVisibilityManager.isFieldHiddenInAllSchemes(projectId, IssueFieldConstants.FIX_FOR_VERSIONS, null)
                && (fieldVisibilityManager.isFieldHiddenInAllSchemes(projectId, IssueFieldConstants.COMPONENTS, null)));
    }
}
