package com.atlassian.streams.jira;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.streams.spi.StreamsValidator;

/**
 * Jira implementation of the validator
 *
 * @since v3.0
 */
public class JiraStreamsValidator implements StreamsValidator
{
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;

    public JiraStreamsValidator(final ProjectManager projectManager, final PermissionManager permissionManager,
            final JiraAuthenticationContext authenticationContext)
    {
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
    }

    public boolean isValidKey(final String key)
    {
        final Project project = projectManager.getProjectObjByKey(key);
        return project != null && permissionManager.hasPermission(Permissions.BROWSE, project, authenticationContext.getLoggedInUser());
    }
}
