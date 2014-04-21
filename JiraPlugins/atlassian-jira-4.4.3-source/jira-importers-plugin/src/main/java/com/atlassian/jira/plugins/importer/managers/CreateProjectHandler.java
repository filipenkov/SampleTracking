/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.managers;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.ErrorCollection;

import javax.annotation.Nullable;

public interface CreateProjectHandler {

    @Nullable
    Project createProject(String name, String key, String description, String lead,
                          String url, Long assigneeType, ErrorCollection errors);

}
