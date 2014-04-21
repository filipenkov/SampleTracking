package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.I18nBean;

import java.util.Map;

/**
 * A generic implementation of a {@link com.atlassian.jira.plugin.projectpanel.ProjectTabPanel}. Delegates to the
 * view resource for rendering.
 */
public class GenericProjectTabPanel extends AbstractProjectTabPanel
{
    private final FieldVisibilityManager fieldVisibilityManager;

    public GenericProjectTabPanel(final JiraAuthenticationContext jiraAuthenticationContext,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        super(jiraAuthenticationContext);
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    /**
     * @deprecated Please use {@link #GenericProjectTabPanel(com.atlassian.jira.security.JiraAuthenticationContext, com.atlassian.jira.web.FieldVisibilityManager)}
     * instead.
     */
    @SuppressWarnings ( { "JavaDoc" })
    @Deprecated
    public GenericProjectTabPanel(final JiraAuthenticationContext jiraAuthenticationContext)
    {
        super(jiraAuthenticationContext);
        this.fieldVisibilityManager = ComponentManager.getComponentInstanceOfType(FieldVisibilityManager.class);
    }

    public String getHtml(BrowseContext ctx)
    {
        final Map<String, Object> startingParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
        startingParams.put("i18n", new I18nBean(ctx.getUser()));
        startingParams.put("project", ctx.getProject());
        startingParams.put("fieldVisibility", fieldVisibilityManager);
        return descriptor.getHtml("view", startingParams);
    }

    public boolean showPanel(BrowseContext ctx)
    {
        return !(fieldVisibilityManager.isFieldHiddenInAllSchemes(ctx.getProject().getId(), IssueFieldConstants.FIX_FOR_VERSIONS, null) &&
                fieldVisibilityManager.isFieldHiddenInAllSchemes(ctx.getProject().getId(), IssueFieldConstants.COMPONENTS, null));
    }
}
