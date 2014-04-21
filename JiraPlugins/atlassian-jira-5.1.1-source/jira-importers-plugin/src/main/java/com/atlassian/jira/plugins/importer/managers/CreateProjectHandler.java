/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.managers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.project.Project;

public interface CreateProjectHandler {
	Project createProject(User loggedInUser, String name, String key, String description, String lead, String url,
						  Long assigneeType, ImportLogger log) throws CreateException;

    boolean canCreateProjects(User user);
}
