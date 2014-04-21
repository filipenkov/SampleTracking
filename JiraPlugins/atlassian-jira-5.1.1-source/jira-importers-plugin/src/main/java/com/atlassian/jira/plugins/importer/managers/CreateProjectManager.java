/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.managers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugins.importer.external.ExternalException;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.project.Project;

import javax.annotation.Nonnull;

public interface CreateProjectManager {

    Project createProject(User loggedInUser, final ExternalProject externalProject, ImportLogger log) throws ExternalException;

    boolean canCreateProjects(User user);

    /**
     * For testing.
     * @return
     */
    @Nonnull
    CreateProjectHandler getCurrentHandler();

    /**
     * For testing.
     * @return
     */
    @Nonnull
    CreateProjectHandler getDefaultHandler();

}
