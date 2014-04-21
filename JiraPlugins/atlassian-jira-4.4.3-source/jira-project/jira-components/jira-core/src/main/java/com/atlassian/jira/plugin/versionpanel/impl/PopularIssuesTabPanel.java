package com.atlassian.jira.plugin.versionpanel.impl;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.plugin.util.TabPanelUtil;
import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.SessionKeys;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;

import java.util.Map;

/**
 * Version tab panel that displayes popular issues sorted by number of votes.
 * <p/>
 * TODO: refactor to somehow use {@link com.atlassian.jira.plugin.projectpanel.impl.PopularIssuesProjectTabPanel}
 *
 * @since v3.10
 */
public class PopularIssuesTabPanel extends GenericTabPanel
{
    private final ApplicationProperties applicationProperties;
    private final ConstantsManager constantsManager;
    private final FieldVisibilityManager fieldVisibilityManager;

    public PopularIssuesTabPanel(final JiraAuthenticationContext authenticationContext,
            final SearchProvider searchProvider, final ApplicationProperties applicationProperties,
            final ConstantsManager constantsManager, final FieldVisibilityManager fieldVisibilityManager)
    {
        super(authenticationContext, searchProvider, fieldVisibilityManager);
        this.applicationProperties = applicationProperties;
        this.constantsManager = constantsManager;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    protected Map<String, Object> createVelocityParams(final BrowseVersionContext context)
    {
        setResolved();

        final TabPanelUtil.PopularIssues popularIssues = new TabPanelUtil.PopularIssues(searchProvider, constantsManager);

        final Map<String, Object> params = super.createVelocityParams(context);
        params.put("popularIssues", popularIssues.getIssues(context, isResolved()));
        params.put("isVersion", Boolean.TRUE);
        params.put("resolved", isResolved());
        params.put("fieldVisibility", fieldVisibilityManager);
        return params;
    }

    private void setResolved()
    {
        //if the request param is set, update the session value.
        final String resolvedParam = getResolvedParam();
        if (StringUtils.isNotEmpty(resolvedParam))
        {
            ActionContext.getSession().put(SessionKeys.VERSION_BROWSER_POPULAR_ISSUES_RESOLVED, Boolean.valueOf(resolvedParam));
        }
    }

    private boolean isResolved()
    {
        Boolean resolved = (Boolean) ActionContext.getSession().get(SessionKeys.VERSION_BROWSER_POPULAR_ISSUES_RESOLVED);
        if (resolved == null)
        {
            final String requestParameter = getResolvedParam();
            if (StringUtils.isNotEmpty(requestParameter))
            {
                resolved = Boolean.valueOf(requestParameter);
                ActionContext.getSession().put(SessionKeys.VERSION_BROWSER_POPULAR_ISSUES_RESOLVED, resolved);
            }
        }
        return resolved != null && resolved.booleanValue();
    }

    private String getResolvedParam()
    {
        VelocityRequestContextFactory vf = new DefaultVelocityRequestContextFactory(applicationProperties);
        VelocityRequestContext context = vf.getJiraVelocityRequestContext();
        return context.getRequestParameter("resolved");
    }

    @Override
    public boolean showPanel(BrowseVersionContext context)
    {
        return super.showPanel(context) && applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING);
    }

}
