/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.managers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugins.importer.external.ExternalException;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.project.Project;

public interface CreateProjectManager {

    Project createProject(User loggedInUser, final ExternalProject externalProject) throws ExternalException;

}
