package com.atlassian.jira.plugins.monitor;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * Base action for monitoring. Does common permission checks, as well as checking if monitoring is enabled.
 *
 * @since v5.1
 */
@WebSudoRequired
@SuppressWarnings ("UnusedDeclaration")
public class MonitorAction extends JiraWebActionSupport
{
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final MonitoringFeature monitoringFeature;

    public MonitorAction(PermissionManager permissionManager, JiraAuthenticationContext jiraAuthenticationContext, MonitoringFeature monitoringFeature)
    {
        this.permissionManager = permissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.monitoringFeature = monitoringFeature;
    }

    @Override
    protected String doExecute() throws Exception
    {
        if (!monitoringFeature.enabled() || !permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, jiraAuthenticationContext.getLoggedInUser()))
        {
            return "securitybreach";
        }

        return super.doExecute();
    }
}
