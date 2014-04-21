/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaClient;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaDataBean;
import com.atlassian.jira.plugins.importer.imports.csv.CsvConfigBean;
import com.atlassian.jira.plugins.importer.imports.csv.CsvConfiguration;
import com.atlassian.jira.plugins.importer.imports.csv.CsvDataBean;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.ExternalCommentMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.StaticProjectMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.TimeEstimateConverter;
import com.atlassian.jira.plugins.importer.imports.fogbugz.FogBugzImporterController;
import com.atlassian.jira.plugins.importer.imports.fogbugz.hosted.FogBugzHostedImporterController;
import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporterFactory;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisClient;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisDataBean;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalImporterController;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalSchemeManager;
import com.atlassian.jira.plugins.importer.imports.trac.TracImporterController;
import com.atlassian.jira.plugins.importer.imports.trac.TracWikiConverter;
import com.atlassian.jira.plugins.importer.web.csv.CsvFieldMappingsPage;
import com.atlassian.jira.plugins.importer.web.csv.CsvProjectMappingsPage;
import com.atlassian.jira.plugins.importer.web.csv.CsvSetupPage;
import com.atlassian.jira.plugins.importer.web.csv.CsvValueMappingsPage;
import com.atlassian.jira.util.lang.Pair;
import com.google.common.collect.Lists;
import org.codehaus.jackson.JsonParseException;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImporterControllerFactory {
	private final Map<String, ImporterController> registeredImporters = new LinkedHashMap<String, ImporterController>();

	public ImporterControllerFactory(ConstantsManager constantsManager,
			JiraDataImporterFactory jiraDataImporterFactory,
			ExternalUtils utils, PivotalSchemeManager pivotalSchemeManager,
			TracWikiConverter wikiConverter) {

		final List<ImporterController> controllers = Lists.<ImporterController> newArrayList(
				new BugzillaImporterController(jiraDataImporterFactory.create(), utils),
				new MantisImporterController(jiraDataImporterFactory.create(), utils),
				new FogBugzImporterController(jiraDataImporterFactory.create(), utils),
				new FogBugzHostedImporterController(jiraDataImporterFactory.create(), utils),
				new PivotalImporterController(jiraDataImporterFactory.create(), utils, pivotalSchemeManager),
				new CsvImporterController(jiraDataImporterFactory.create(), utils),
				new TracImporterController(jiraDataImporterFactory.create(), utils, wikiConverter));

		for(ImporterController controller : controllers) {
			registeredImporters.put(controller.getId(), controller);
		}
	}

	public ImporterController getController(String externalSystem) {
		return registeredImporters.get(externalSystem);
	}

	public Set<String> getSupportedImporters() {
		return Collections.unmodifiableSet(registeredImporters.keySet());
	}

    private static class CsvImporterController extends AbstractImporterController {
		private final ExternalUtils utils;

		public CsvImporterController(JiraDataImporter importer, ExternalUtils utils) {
			super(importer, "issue.importer.jira.csv.import.bean", "CSV");
			this.utils = utils;
		}

		@Override
		public String getTitle() {
			return utils.getAuthenticationContext().getI18nHelper().getText("jira-importer-plugin.external.csv");
		}

		@Override
		public String getDescription() {
			return utils.getAuthenticationContext().getI18nHelper().getText("jira-importer-plugin.external.csv.description");
		}

		@Override
		public Pair<String, String> getLogo() {
			return Pair.of("com.atlassian.jira.plugins.jira-importers-plugin:graphics", "csv");
		}

		@Override
		public ImportProcessBean createImportProcessBean(AbstractSetupPage setupPage) {
			CsvSetupPage csvSetupPage = (CsvSetupPage) setupPage;
			ImportProcessBean bean = new ImportProcessBean();
			try {
				final CsvConfigBean configBean = new CsvConfigBean(
						csvSetupPage.getMultipart().getFile(CsvSetupPage.CSV_FILE_INPUT_NAME),
						csvSetupPage.getEncoding(),
						csvSetupPage.getDelimiterChar(), utils);

				final File temporaryConfigFile = csvSetupPage.getMultipart().getFile(CsvSetupPage.CONFIG_FILE_INPUT_NAME);

				if (temporaryConfigFile != null) {
					configBean.copyFromProperties(temporaryConfigFile);
				}

				bean.setConfigBean(configBean);
			} catch (Exception e) {
				if (e instanceof JsonParseException) {
					csvSetupPage.addErrorMessage(
							csvSetupPage.getText("jira-importer-plugin.import.setup.page.config.file.format.changed"));
				}
				csvSetupPage.addError(CsvSetupPage.CONFIG_FILE_INPUT_NAME, e.getMessage());
				return null;
			}
			return bean;
		}

		@Override
		public ImportDataBean createDataBean(DateTimeFormatterFactory dateTimeFormatterFactory) throws Exception {
            final ImportProcessBean importProcessBean = getImportProcessBeanFromSession();

            final CsvConfigBean configBean = (CsvConfigBean) importProcessBean.getConfigBean();

            final CsvConfiguration config = new CsvConfiguration(configBean, utils.getProjectManager());

            // Configure the data bean
            final CsvDataBean dataBean = new CsvDataBean(configBean, utils.getCustomFieldManager());

            // Add project if a single project per CSV
            if (config.isSingleProjectCsv()) {
                final ExternalProject externalProject = config.getSingleProjectBean();

                dataBean.setProjectMapper(new StaticProjectMapper(externalProject));
            }

                // Add custom user mappers
            final List customUserMappers = config.getCustomUserMappers();
            if (customUserMappers != null) {
                dataBean.setUserMappers(customUserMappers);
            }

            // Add custom comment mappers
            final ExternalCommentMapper customCommentMapper = config.getCustomCommentMapper();
            if (customCommentMapper != null) {
                dataBean.setCommentMapper(customCommentMapper);
            }

            // Add customer time estimate converter
            final TimeEstimateConverter customTimeEstimateConverter = config.getCustomTimeEstimateConverter();
            if (customTimeEstimateConverter != null) {
                dataBean.setTimeEstimateConverter(customTimeEstimateConverter);
            }

            return dataBean;
		}

        @Override
        public List<String> getSteps() {
            return Lists.newArrayList(
                    CsvSetupPage.class.getSimpleName(),
					CsvProjectMappingsPage.class.getSimpleName(),
					CsvFieldMappingsPage.class.getSimpleName(),
					CsvValueMappingsPage.class.getSimpleName()
            );
        }
	}

	private static class BugzillaImporterController extends AbstractDatabaseImporterController {
		private final ExternalUtils utils;

		public BugzillaImporterController(JiraDataImporter bugzillaImporter, ExternalUtils utils) {
            super(bugzillaImporter, "issue.importer.jira.bugzilla.import.bean", "Bugzilla");
			this.utils = utils;
		}

		@Override
		public String getTitle() {
			return "Bugzilla";
		}

		@Override
		public String getDescription() {
			return utils.getAuthenticationContext().getI18nHelper().getText("jira-importer-plugin.external.bugzilla.description");
		}

		@Override
		public String getSupportedVersions() {
			return utils.getAuthenticationContext().getI18nHelper().getText("jira-importer-plugin.external.bugzilla.versions");
		}

		@Override
		public ImportDataBean createDataBean(DateTimeFormatterFactory dateTimeFormatterFactory) {
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

	private static class MantisImporterController extends AbstractDatabaseImporterController {

		private final ExternalUtils utils;

		public MantisImporterController(JiraDataImporter mantisImporter, ExternalUtils utils) {
            super(mantisImporter, "issue.importer.jira.mantis.import.bean", "Mantis");
			this.utils = utils;
		}

		@Override
		public String getTitle() {
			return "Mantis";
		}

		@Override
		public String getDescription() {
			return utils.getAuthenticationContext().getI18nHelper().getText("jira-importer-plugin.external.mantis.description");
		}

		@Override
		public String getSupportedVersions() {
			return utils.getAuthenticationContext().getI18nHelper().getText("jira-importer-plugin.external.mantis.versions");
		}

		@Override
		public ImportDataBean createDataBean(DateTimeFormatterFactory dateTimeFormatterFactory) {
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
						importerSetupPage.getSiteCredentials(), importerSetupPage.getSiteUsername(), importerSetupPage.getSitePassword());

                importerSetupPage.addErrorMessages(new MantisClient(siteConfiguration).validateConnection());
			}
		}

	}

}
