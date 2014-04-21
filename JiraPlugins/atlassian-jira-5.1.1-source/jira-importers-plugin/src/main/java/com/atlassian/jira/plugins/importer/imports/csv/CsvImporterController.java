/*
 * Copyright (c) 2012. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.csv;

import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.ExternalCommentMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.ExternalUserMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.StaticProjectMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.TimeEstimateConverter;
import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.web.AbstractImporterController;
import com.atlassian.jira.plugins.importer.web.AbstractSetupPage;
import com.atlassian.jira.plugins.importer.web.ImportProcessBean;
import com.atlassian.jira.plugins.importer.imports.csv.web.CsvFieldMappingsPage;
import com.atlassian.jira.plugins.importer.imports.csv.web.CsvProjectMappingsPage;
import com.atlassian.jira.plugins.importer.imports.csv.web.CsvSetupPage;
import com.atlassian.jira.plugins.importer.imports.csv.web.CsvValueMappingsPage;
import com.google.common.collect.Lists;
import org.codehaus.jackson.JsonParseException;

import java.io.File;
import java.util.List;

public class CsvImporterController extends AbstractImporterController {
	private final ExternalUtils utils;
	private final CustomFieldManager customFieldManager;
    private final AttachmentManager attachmentManager;
	private final JiraHome jiraHome;

	public CsvImporterController(JiraDataImporter importer, ExternalUtils utils, CustomFieldManager customFieldManager, JiraHome jiraHome, AttachmentManager attachmentManager) {
		super(importer, "issue.importer.jira.csv.import.bean", "CSV");
		this.utils = utils;
		this.customFieldManager = customFieldManager;
		this.jiraHome = jiraHome;
        this.attachmentManager = attachmentManager;
    }

	@Override
	public boolean createImportProcessBean(AbstractSetupPage setupPage) {
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
			return false;
		}
        storeImportProcessBeanInSession(bean);
		return true;
	}

	@Override
	public ImportDataBean createDataBean() throws Exception {
		final ImportProcessBean importProcessBean = getImportProcessBeanFromSession();

		final CsvConfigBean configBean = (CsvConfigBean) importProcessBean.getConfigBean();

		final CsvConfiguration config = new CsvConfiguration(configBean, utils.getProjectManager());

		// Configure the data bean
		final CsvDataBean dataBean = new CsvDataBean(configBean, customFieldManager, jiraHome, utils.getIssueManager(), attachmentManager);

		// Add project if a single project per CSV
		if (config.isSingleProjectCsv()) {
			final ExternalProject externalProject = config.getSingleProjectBean();

			dataBean.setProjectMapper(new StaticProjectMapper(externalProject));
		}

			// Add custom user mappers
		final List<ExternalUserMapper> customUserMappers = config.getCustomUserMappers();
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
