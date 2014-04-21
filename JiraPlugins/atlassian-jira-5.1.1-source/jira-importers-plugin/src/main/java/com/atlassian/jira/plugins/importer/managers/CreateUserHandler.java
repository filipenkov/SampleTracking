/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.managers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;

public interface CreateUserHandler {

	User createUserNoNotification(String name, String password, String email, String fullName, ImportLogger importLogger)
			throws com.atlassian.jira.exception.PermissionException, com.atlassian.jira.exception.CreateException;

}
