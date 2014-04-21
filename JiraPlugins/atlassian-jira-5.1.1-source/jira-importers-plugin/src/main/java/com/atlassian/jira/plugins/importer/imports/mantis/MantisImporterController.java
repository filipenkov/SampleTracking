package com.atlassian.jira.plugins.importer.imports.mantis;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;
import com.atlassian.jira.plugins.importer.web.AbstractDatabaseImporterController;
import com.atlassian.jira.plugins.importer.web.ConfigFileHandler;
import com.atlassian.jira.plugins.importer.web.ImportProcessBean;
import com.atlassian.jira.plugins.importer.web.ImporterSetupPage;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.plugins.importer.web.RemoteSiteValidator;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;

public class MantisImporterController extends AbstractDatabaseImporterController {

	private final ExternalUtils utils;
	private final DateTimeFormatterFactory dateTimeFormatterFactory;

	public MantisImporterController(JiraDataImporter mantisImporter, ExternalUtils utils, ConfigFileHandler configFileHandler, DateTimeFormatterFactory dateTimeFormatterFactory) {
		super(configFileHandler, mantisImporter, "issue.importer.jira.mantis.import.bean", "Mantis");
		this.utils = utils;
		this.dateTimeFormatterFactory = dateTimeFormatterFactory;
	}

	@Override
	public ImportDataBean createDataBean() {
		final ImportProcessBean importProcessBean = getImportProcessBeanFromSession();
		return new MantisDataBean(importProcessBean.getJdbcConnection(),
				(MantisConfigBean) importProcessBean.getConfigBean(), importProcessBean.getUrlBean(),
				dateTimeFormatterFactory);
	}

	public AbstractDatabaseConfigBean createConfigBean(JdbcConnection jdbcConnection) {
		return new MantisConfigBean(jdbcConnection, utils);
	}

	public void validateConnection(ImporterSetupPage importerSetupPage) {
		new RemoteSiteValidator().validateConnection(importerSetupPage);
		if (!importerSetupPage.hasAnyErrors()) {
			final SiteConfiguration siteConfiguration = new SiteConfiguration(importerSetupPage.getSiteUrl(),
					importerSetupPage.getSiteCredentials(), importerSetupPage.getSiteUsername(),
					importerSetupPage.getSitePassword());

			importerSetupPage.addErrorMessages(new MantisClient(siteConfiguration).validateConnection());
		}
	}

}
