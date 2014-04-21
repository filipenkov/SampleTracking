/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.managers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.plugins.importer.external.ExternalException;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nonnull;

public class CreateProjectManagerImpl implements CreateProjectManager {

    private final JiraAuthenticationContext authenticationContext;
    private final ApplicationProperties applicationProperties;
	private final PermissionSchemeManager permissionSchemeManager;
	private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
	private final WorkflowSchemeManager workflowSchemeManager;
    private final CreateProjectHandlerProvider createProjectHandlerProvider;
    private final DefaultCreateProjectHandler defaultCreateProjectHandler;

    public CreateProjectManagerImpl(JiraAuthenticationContext authenticationContext,
			ApplicationProperties applicationProperties,
			ProjectService projectService, PermissionSchemeManager permissionSchemeManager,
			IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, WorkflowSchemeManager workflowSchemeManager,
            CreateProjectHandlerProvider createProjectHandlerProvider, GlobalPermissionManager globalPermissionManager) {
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
		this.permissionSchemeManager = permissionSchemeManager;
		this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
		this.workflowSchemeManager = workflowSchemeManager;
        this.createProjectHandlerProvider = createProjectHandlerProvider;
        this.defaultCreateProjectHandler = new DefaultCreateProjectHandler(projectService, globalPermissionManager);
    }

	public Project createProject(User loggedInUser, final ExternalProject externalProject, ImportLogger log) throws ExternalException {
        if (!canCreateProjects(loggedInUser)) {
            throw new ExternalException(String.format("User %s is not allowed to create projects.", loggedInUser.getName()));
        }

		try {
			// Set lead to current user if none exists
			if (externalProject.getLead() == null) {
				externalProject.setLead(authenticationContext.getLoggedInUser().getName());
			}

			// JRA-19699: if there is no assignee type - set it to either UNASSIGNED or PROJECT LEAD
			if (externalProject.getAssigneeType() == null) {
				if (isUnassignedIssuesAllowed()) {
					externalProject.setAssigneeType(AssigneeTypes.UNASSIGNED);
				} else {
					externalProject.setAssigneeType(AssigneeTypes.PROJECT_LEAD);
				}
			}

			final Project project = getCurrentHandler().createProject(loggedInUser, externalProject.getName(),
                    externalProject.getKey(), externalProject.getDescription(), externalProject.getLead(),
                    externalProject.getUrl(), externalProject.getAssigneeType(), log);

			// Add the default schemes for this project
			permissionSchemeManager.addDefaultSchemeToProject(project);
			issueTypeScreenSchemeManager.associateWithDefaultScheme(project);

			if (externalProject.getWorkflowSchemeName() != null) {
				final GenericValue scheme = workflowSchemeManager.getScheme(externalProject.getWorkflowSchemeName());
				workflowSchemeManager.addSchemeToProject(project.getGenericValue(), scheme);
			}

			return project;
		} catch (final Exception e) {
			throw new ExternalException("Unable to create project: " + externalProject, e);
		}
	}

    @Override
    public boolean canCreateProjects(User user) {
        return getCurrentHandler().canCreateProjects(user);
    }

    private boolean isUnassignedIssuesAllowed() {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED);
    }

    @Nonnull
    public CreateProjectHandler getCurrentHandler() {
        CreateProjectHandler currentHandler = createProjectHandlerProvider.getHandler();
        if (currentHandler == null) {
            currentHandler = defaultCreateProjectHandler;
        }
        return currentHandler;
    }

    /**
     * For testing.
     * @return
     */
    @Nonnull
    public DefaultCreateProjectHandler getDefaultHandler() {
        return defaultCreateProjectHandler;
    }
}
