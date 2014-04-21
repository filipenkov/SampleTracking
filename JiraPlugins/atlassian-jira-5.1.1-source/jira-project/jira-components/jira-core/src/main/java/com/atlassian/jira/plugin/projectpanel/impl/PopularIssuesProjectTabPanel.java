package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.plugin.util.TabPanelUtil;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.List;
import java.util.Map;

/**
 * Displays popular issues for a project.
 */
public class PopularIssuesProjectTabPanel extends AbstractProjectTabPanel
{
    private final ApplicationProperties applicationProperties;
    private final SearchProvider searchProvider;
    private final ConstantsManager constantsManager;
    private final FieldVisibilityManager fieldVisibilityManager;

    public PopularIssuesProjectTabPanel(final ApplicationProperties applicationProperties, SearchProvider searchProvider,
            final ConstantsManager constantsManager, final JiraAuthenticationContext authenticationContext,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        super(authenticationContext);
        this.applicationProperties = applicationProperties;
        this.searchProvider = searchProvider;
        this.constantsManager = constantsManager;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        final Map<String, Object> params = super.createVelocityParams(ctx);
        params.put("popularIssues", getPopularIssues(ctx));
        params.put("fieldVisibility", fieldVisibilityManager);
        return params;
    }

    public boolean showPanel(BrowseContext ctx)
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING);
    }

    private List getPopularIssues(final BrowseContext ctx)
    {
        final TabPanelUtil.PopularIssues popularIssues = new TabPanelUtil.PopularIssues(searchProvider, constantsManager);
        return popularIssues.getIssues(ctx, false);
    }

}