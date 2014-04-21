package com.atlassian.jira.plugins.importer.rest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.managers.CreateProjectHandler;
import com.atlassian.jira.project.Project;

public class BackdoorCreateProjectHandler implements CreateProjectHandler {
    @Override
    public Project createProject(User loggedInUser, String name, String key, String description, String lead, String url, Long assigneeType, ImportLogger log) throws CreateException {
        return null;
    }

    @Override
    public boolean canCreateProjects(User user) {
        return false;
    }
}
