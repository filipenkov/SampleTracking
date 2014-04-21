/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.importer.impl;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.plugins.importer.external.ExternalUserUtils;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporterFactory;
import com.atlassian.jira.plugins.importer.managers.CreateConstantsManager;
import com.atlassian.jira.plugins.importer.managers.CreateProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.user.util.UserUtil;

public class JiraDataImporterFactoryImpl implements JiraDataImporterFactory {

	private final ExternalUtils utils;
	private final WorklogManager worklogManager;
	private final FieldManager fieldManager;
	private final WatcherManager watcherManager;
	private final VoteManager voteManager;
	private final IssueIndexManager indexManager;
	private final CreateConstantsManager createConstantsManager;
	private final SubTaskManager subTaskManager;
	private final VersionManager versionManager;
	private final ExternalUserUtils externalUserUtils;
	private final JiraContextTreeManager jiraContextTreeManager;
	private final CreateProjectManager createProjectManager;
	private final CrowdService crowdService;
	private final OptionsManager optionsManager;
	private final SearchProviderFactory searchProviderFactory;
	private final UserUtil userUtil;
	private final JiraLicenseService jiraLicenseService;

	public JiraDataImporterFactoryImpl(ExternalUtils utils,
			WorklogManager worklogManager, FieldManager fieldManager,
			WatcherManager watcherManager, VoteManager voteManager, IssueIndexManager indexManager,
			CreateConstantsManager createConstantsManager, SubTaskManager subTaskManager,
			VersionManager versionManager, ExternalUserUtils externalUserUtils,
			JiraContextTreeManager jiraContextTreeManager, CreateProjectManager createProjectManager,
			CrowdService crowdService, OptionsManager optionsManager, SearchProviderFactory searchProviderFactory,
			UserUtil userUtil, JiraLicenseService jiraLicenseService) {

		this.utils = utils;
		this.worklogManager = worklogManager;
		this.fieldManager = fieldManager;
		this.watcherManager = watcherManager;
		this.voteManager = voteManager;
		this.indexManager = indexManager;
		this.createConstantsManager = createConstantsManager;
		this.subTaskManager = subTaskManager;
		this.versionManager = versionManager;
		this.externalUserUtils = externalUserUtils;
		this.jiraContextTreeManager = jiraContextTreeManager;
		this.createProjectManager = createProjectManager;
		this.crowdService = crowdService;
		this.optionsManager = optionsManager;
		this.searchProviderFactory = searchProviderFactory;
		this.userUtil = userUtil;
		this.jiraLicenseService = jiraLicenseService;
	}

	@SuppressWarnings("unused")
	public JiraDataImporterFactoryImpl(ExternalUtils utils,
			WorklogManager worklogManager, FieldManager fieldManager,
			WatcherManager watcherManager, VoteManager voteManager, IssueIndexManager indexManager,
			CreateConstantsManager createConstantsManager, SubTaskManager subTaskManager,
			VersionManager versionManager, ExternalUserUtils externalUserUtils,
			CreateProjectManager createProjectManager, CrowdService crowdService, OptionsManager optionsManager,
			SearchProviderFactory searchProviderFactory,
			UserUtil userUtil, JiraLicenseService jiraLicenseService) {
		this(utils, worklogManager, fieldManager, watcherManager, voteManager, indexManager,
				createConstantsManager, subTaskManager, versionManager, externalUserUtils,
				ComponentManager.getComponentInstanceOfType(JiraContextTreeManager.class), createProjectManager,
				crowdService, optionsManager, searchProviderFactory, userUtil, jiraLicenseService);
	}

	@Override
	public JiraDataImporter create() {
		return new DefaultJiraDataImporter(utils, worklogManager, fieldManager, watcherManager, voteManager,
				indexManager, createConstantsManager, subTaskManager, versionManager, externalUserUtils, jiraContextTreeManager,
				createProjectManager, crowdService, optionsManager, searchProviderFactory, userUtil, jiraLicenseService);
	}
}
