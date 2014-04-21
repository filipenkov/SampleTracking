/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.fogbugz;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;
import com.atlassian.jira.plugins.importer.web.AbstractDatabaseImporterController;
import com.atlassian.jira.plugins.importer.web.ConfigFileHandler;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.plugins.importer.web.ImportProcessBean;
import com.atlassian.jira.plugins.importer.web.ImporterSetupPage;

public class FogBugzImporterController extends AbstractDatabaseImporterController {
	private final ExternalUtils utils;
	private final DateTimeFormatterFactory dateTimeFormatterFactory;

	public static final String FOGBUGZ_IMPORT_CONFIG_BEAN = "issue.importer.jira.fogbugz.import.bean";

	public FogBugzImporterController(JiraDataImporter fogbugzImporter, ExternalUtils utils, ConfigFileHandler configFileHandler, DateTimeFormatterFactory dateTimeFormatterFactory) {
		super(configFileHandler, fogbugzImporter, FOGBUGZ_IMPORT_CONFIG_BEAN, "FogBugz");
		this.utils = utils;
		this.dateTimeFormatterFactory = dateTimeFormatterFactory;
	}

	@Override
	public ImportDataBean createDataBean() {
		final ImportProcessBean importProcessBean = getImportProcessBeanFromSession();
		return new FogBugzDataBean((FogBugzConfigBean) importProcessBean.getConfigBean(), dateTimeFormatterFactory);
	}

	@Override
	public AbstractDatabaseConfigBean createConfigBean(JdbcConnection jdbcConnection) {
		return new FogBugzConfigBean(jdbcConnection, utils);
	}

	@Override
	protected void validateConnection(ImporterSetupPage setupPage) {
	}

}