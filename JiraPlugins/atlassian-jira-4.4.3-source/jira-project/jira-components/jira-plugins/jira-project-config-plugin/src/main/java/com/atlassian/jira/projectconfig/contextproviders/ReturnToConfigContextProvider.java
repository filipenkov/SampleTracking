package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Context Provider for the "Back to Project Config link"
 *
 * @since v4.4
 */
public class ReturnToConfigContextProvider implements ContextProvider
{
    private final VelocityRequestContextFactory contextFactory;
    private final JiraAuthenticationContext authenticationContext;
    private final ProjectService projectService;

    public ReturnToConfigContextProvider(VelocityRequestContextFactory contextFactory, JiraAuthenticationContext authenticationContext, ProjectService projectService)
    {
        this.contextFactory = contextFactory;
        this.authenticationContext = authenticationContext;
        this.projectService = projectService;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final MapBuilder<String, Object> contextBuilder = MapBuilder.newBuilder(context);

        final VelocityRequestContext requestContext = contextFactory.getJiraVelocityRequestContext();

        final VelocityRequestSession session = requestContext.getSession();
        final String projectKey = (String) session.getAttribute(SessionKeys.CURRENT_ADMIN_PROJECT);

        if (StringUtils.isNotBlank(projectKey))
        {
            final ProjectService.GetProjectResult projectResult = projectService.getProjectByKeyForAction(authenticationContext.getLoggedInUser(), projectKey, ProjectAction.EDIT_PROJECT_CONFIG);
            if (projectResult.isValid())
            {
                final Project project = projectResult.getProject();
                contextBuilder.add("project", project);
            }
        }
        final String tab = (String) session.getAttribute(SessionKeys.CURRENT_ADMIN_PROJECT_TAB);

        if (StringUtils.isNotBlank(tab))
        {
            contextBuilder.add("tab", tab);
        }
        else
        {
            contextBuilder.add("tab", "summary");
        }

        return JiraVelocityUtils.getDefaultVelocityParams(contextBuilder.toMap(), authenticationContext);
    }
}
