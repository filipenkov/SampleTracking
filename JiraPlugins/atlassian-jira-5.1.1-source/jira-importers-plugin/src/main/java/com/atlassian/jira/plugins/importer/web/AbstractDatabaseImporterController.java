/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.configurator.db.DatabaseConfig;
import com.atlassian.jira.configurator.db.DatabaseConfigFactory;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public abstract class AbstractDatabaseImporterController extends AbstractImporterController {
	private final ConfigFileHandler configFileHandler;

	protected abstract AbstractDatabaseConfigBean createConfigBean(JdbcConnection jdbcConnection);

	protected abstract void validateConnection(ImporterSetupPage setupPage);

	protected JdbcConnection createDatabaseConnectionBean(
			ImporterSetupPage importerSetupPage) {

		DatabaseConfig databaseConfig = DatabaseConfigFactory
				.getDatabaseConfigFor(importerSetupPage.getDatabaseTypeEnum());

		try {
			String url = databaseConfig.getUrl(importerSetupPage.getJdbcHostname(), importerSetupPage.getJdbcPort(),
							importerSetupPage.getJdbcDatabase());

			if (StringUtils.isNotEmpty(importerSetupPage.getJdbcAdvanced())) {
				final String sep;
				if ("net.sourceforge.jtds.jdbc.Driver".equals(databaseConfig.getClassName())) {
					sep = ";";
				} else {
					sep = url.contains("?") ? "&" : "?";
				}
				url += sep + importerSetupPage.getJdbcAdvanced();
			}

			return new JdbcConnection(databaseConfig.getClassName(), url,
					importerSetupPage.getJdbcUsername(), importerSetupPage.getJdbcPassword());
		} catch (ValidationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean createImportProcessBean(AbstractSetupPage setupPage) {
		final ImporterSetupPage importerSetupPage = (ImporterSetupPage) setupPage;
		final JdbcConnection jdbcConnection = createDatabaseConnectionBean(importerSetupPage);

		jdbcConnection.validateConnection(setupPage);
		validateConnection(importerSetupPage);

		final ImportProcessBean importBean = new ImportProcessBean();

		final SiteConfiguration siteConfiguration = new SiteConfiguration(importerSetupPage.getSiteUrl(),
				importerSetupPage.getSiteCredentials(), importerSetupPage.getSiteUsername(),
				importerSetupPage.getSitePassword());

		importBean.setUrlBean(siteConfiguration);

		if (!importerSetupPage.hasAnyErrors()) {
			final AbstractConfigBean configBean = createConfigBean(jdbcConnection);

			if (!configFileHandler.populateFromConfigFile(importerSetupPage, configBean)) {
                return false;
            }

			importBean.setJdbcConnection(jdbcConnection);
			importBean.setConfigBean(configBean);
		}

        storeImportProcessBeanInSession(importBean);
        return !importerSetupPage.hasAnyErrors();
	}

	public AbstractDatabaseImporterController(ConfigFileHandler configFileHandler, JiraDataImporter importer, String sessionAttributeName, String id) {
		super(importer, sessionAttributeName, id);
		this.configFileHandler = configFileHandler;
	}

	@Override
	public List<String> getSteps() {
		return Lists.newArrayList(
				ImporterSetupPage.class.getSimpleName(),
				ImporterProjectMappingsPage.class.getSimpleName(),
				ImporterCustomFieldsPage.class.getSimpleName(),
				ImporterFieldMappingsPage.class.getSimpleName(),
				ImporterValueMappingsPage.class.getSimpleName(),
				ImporterLinksPage.class.getSimpleName()
		);
	}
}
