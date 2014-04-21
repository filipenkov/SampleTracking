package com.atlassian.jira.plugins.importer.imports.bugzilla;

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

/**
 * TODO: Document this class / interface here
 *
 * @since v5.1
 */
public class BugzillaImporterController extends AbstractDatabaseImporterController {
	private final ExternalUtils utils;
	private final DateTimeFormatterFactory dateTimeFormatterFactory;

	public BugzillaImporterController(JiraDataImporter bugzillaImporter, ExternalUtils utils, ConfigFileHandler configFileHandler,
			DateTimeFormatterFactory dateTimeFormatterFactory) {
		super(configFileHandler, bugzillaImporter, "issue.importer.jira.bugzilla.import.bean", "Bugzilla");
		this.utils = utils;
		this.dateTimeFormatterFactory = dateTimeFormatterFactory;
	}

	@Override
	public ImportDataBean createDataBean() {
		final ImportProcessBean importProcessBean = getImportProcessBeanFromSession();
		return new BugzillaDataBean(importProcessBean.getJdbcConnection(),
				(BugzillaConfigBean) importProcessBean.getConfigBean(), importProcessBean.getUrlBean(),
				dateTimeFormatterFactory);
	}

	@Override
	public AbstractDatabaseConfigBean createConfigBean(JdbcConnection jdbcConnection) {
		return new BugzillaConfigBean(jdbcConnection, utils);
	}

	public void validateConnection(ImporterSetupPage importerSetupPage) {
		new RemoteSiteValidator().validateConnection(importerSetupPage);
		if (!importerSetupPage.hasAnyErrors()) {
			final SiteConfiguration siteConfiguration = new SiteConfiguration(importerSetupPage.getSiteUrl(),
					importerSetupPage.getSiteCredentials(), importerSetupPage.getSiteUsername(), importerSetupPage.getSitePassword());
			importerSetupPage.addErrorMessages(new BugzillaClient(siteConfiguration).validateConnection());
		}
	}
}
