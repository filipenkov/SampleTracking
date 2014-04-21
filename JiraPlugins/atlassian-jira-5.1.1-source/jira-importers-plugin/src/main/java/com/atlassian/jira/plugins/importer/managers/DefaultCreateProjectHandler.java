package com.atlassian.jira.plugins.importer.managers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.web.TextsUtil;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;

public class DefaultCreateProjectHandler implements CreateProjectHandler {

    private final ProjectService projectService;
    private final GlobalPermissionManager globalPermissionManager;

    public DefaultCreateProjectHandler(ProjectService projectService, GlobalPermissionManager globalPermissionManager) {
        this.projectService = projectService;
        this.globalPermissionManager = globalPermissionManager;
    }

    @Override
    public Project createProject(User loggedInUser, String name, String key, String description, String lead, String url, Long assigneeType, ImportLogger log) throws CreateException {
        final ProjectService.CreateProjectValidationResult result =
                projectService.validateCreateProject(loggedInUser, name, key, description, lead, url, assigneeType);
        if (!result.isValid()) {
            final String msg = TextsUtil.buildErrorMessage(result.getErrorCollection());
            throw new CreateException(msg);
        }
        return projectService.createProject(result);
    }

    @Override
    public boolean canCreateProjects(User user) {
        return globalPermissionManager.hasPermission(Permissions.ADMINISTER, user);
    }
}
