/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.importer.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;

import java.util.concurrent.Callable;

public class ImporterCallable implements Callable<Void> {
	private final JiraDataImporter jiraDataImporter;
	private final User user;

	public ImporterCallable(JiraDataImporter jiraDataImporter, User user) {
		this.jiraDataImporter = jiraDataImporter;
		this.user = user;
	}

	public Void call() throws Exception {
		ComponentManager.getInstance().getJiraAuthenticationContext().setLoggedInUser(user);
		jiraDataImporter.doImport();
		return null;
	}
}
