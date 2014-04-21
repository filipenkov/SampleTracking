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
import com.atlassian.jira.plugins.importer.web.TextsUtil;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import org.ofbiz.core.entity.GenericValue;

public class CreateProjectManagerImpl implements CreateProjectManager {

    private final JiraAuthenticationContext authenticationContext;
    private final ApplicationProperties applicationProperties;
	private final ProjectService projectService;
	private final PermissionSchemeManager permissionSchemeManager;
	private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
	private final WorkflowSchemeManager workflowSchemeManager;
	private CreateProjectHandler createProjectHandler;


	public CreateProjectManagerImpl(JiraAuthenticationContext authenticationContext,
            ApplicationProperties applicationProperties,
			ProjectService projectService, PermissionSchemeManager permissionSchemeManager,
			IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, WorkflowSchemeManager workflowSchemeManager) {
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
		this.projectService = projectService;
		this.permissionSchemeManager = permissionSchemeManager;
		this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
		this.workflowSchemeManager = workflowSchemeManager;
	}

	public void setCreateProjectHandler(CreateProjectHandler createProjectHandler) {
		this.createProjectHandler = createProjectHandler;
	}

	public Project createProject(User loggedInUser, final ExternalProject externalProject) throws ExternalException {
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

			final Project project;
			if (createProjectHandler != null) {
				ErrorCollection errors = new SimpleErrorCollection();
				project = createProjectHandler.createProject(externalProject.getName(),
					externalProject.getKey(), externalProject.getDescription(), externalProject.getLead(),
					externalProject.getUrl(), externalProject.getAssigneeType(), errors);

				if (errors.hasAnyErrors()) {
					throw new ExternalException("Unable to create project: " + errors.toString());
				}
			} else {
				final ProjectService.CreateProjectValidationResult result =
						projectService.validateCreateProject(loggedInUser, externalProject.getName(),
					externalProject.getKey(), externalProject.getDescription(), externalProject.getLead(),
					externalProject.getUrl(), externalProject.getAssigneeType());
				if (!result.isValid()) {
					final String msg = TextsUtil.buildErrorMessage(result.getErrorCollection());
					throw new ExternalException(msg);
				}
				project = projectService.createProject(result);
			}

			// Add the default schemes for this project
			permissionSchemeManager.addDefaultSchemeToProject(project.getGenericValue());
			issueTypeScreenSchemeManager.associateWithDefaultScheme(project.getGenericValue());

			if (externalProject.getWorkflowSchemeName() != null) {
				final GenericValue scheme = workflowSchemeManager.getScheme(externalProject.getWorkflowSchemeName());
				workflowSchemeManager.addSchemeToProject(project.getGenericValue(), scheme);
			}

			return project;
		} catch (final Exception e) {
			throw new ExternalException("Unable to create project: " + externalProject, e);
		}
	}

	private boolean isUnassignedIssuesAllowed() {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED);
    }

}
