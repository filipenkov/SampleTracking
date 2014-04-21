/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.rest;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.security.JiraAuthenticationContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("validation")
@Produces({MediaType.APPLICATION_JSON})
public class ValidationResource {
	private final JiraAuthenticationContext authenticationContext;
	private final ProjectService projectService;

	public ValidationResource(JiraAuthenticationContext authenticationContext,
			ProjectService projectService) {
		this.authenticationContext = authenticationContext;
		this.projectService = projectService;
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/validateProject")
	public Response validateProject(@FormParam("key") String projectKey, @FormParam("name") String projectName, @FormParam("lead") String lead) {
		final ProjectService.CreateProjectValidationResult createProjectValidationResult =
				projectService.validateCreateProject(authenticationContext
						.getLoggedInUser(), projectName, projectKey, "", lead, "", null);

		return Response.ok(createProjectValidationResult.getErrorCollection().getErrors()).build();
	}

}
