/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.imports.importer.AbstractConfigBean2;
import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.imports.pivotal.web.PivotalProjectMappingsPage;
import com.atlassian.jira.plugins.importer.imports.pivotal.web.PivotalSetupPage;
import com.atlassian.jira.plugins.importer.web.AbstractImporterController;
import com.atlassian.jira.plugins.importer.web.AbstractSetupPage;
import com.atlassian.jira.plugins.importer.web.ImportProcessBean;
import com.atlassian.jira.plugins.importer.web.RemoteSiteImporterSetupPage;
import com.atlassian.jira.plugins.importer.web.RemoteSiteValidator;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;
import com.google.common.collect.ImmutableList;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import java.util.List;

public class PivotalImporterController extends AbstractImporterController {
	private final ExternalUtils utils;
	private final PivotalSchemeManager pivotalSchemeManager;

	private final Logger logger = Logger.getLogger(PivotalImporterController.class);
	public static final String PIVOTAL_IMPORT_CONFIG_BEAN = "issue.importer.jira.pivotal.import.bean";

	public PivotalImporterController(JiraDataImporter pivotalImporter, ExternalUtils utils, PivotalSchemeManager pivotalSchemeManager) {
		super(pivotalImporter, PIVOTAL_IMPORT_CONFIG_BEAN, "Pivotal");
		this.utils = utils;
		this.pivotalSchemeManager = pivotalSchemeManager;
	}

	@Override
	public String getTitle() {
		return "Pivotal Tracker";
	}

	@Override
	public String getDescription() {
		return utils.getAuthenticationContext().getI18nHelper().getText("jira-importer-plugin.external.pivotaltracker.description");
	}

	@Override
	public ImportProcessBean createImportProcessBean(AbstractSetupPage setupPage) {
		final PivotalSetupPage pivotalSetupPage = (PivotalSetupPage) setupPage;

		validateRemoteSiteConnection(pivotalSetupPage);

		if (pivotalSetupPage.invalidInput()) {
			return null;
		}

		final SiteConfiguration siteConfiguration = new SiteConfiguration("http://www.pivotaltracker.com",
				pivotalSetupPage.getSiteCredentials(),
				pivotalSetupPage.getSiteUsername(),
				pivotalSetupPage.getSitePassword());

		final ImportProcessBean importProcessBean = new ImportProcessBean();
		importProcessBean.setUrlBean(siteConfiguration);

		final AbstractConfigBean2 configBean = new PivotalConfigBean(siteConfiguration, utils);

		importProcessBean.setConfigBean(configBean);

		return importProcessBean;
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="BC_UNCONFIRMED_CAST")
	@Override
	public ImportDataBean createDataBean(DateTimeFormatterFactory dateTimeFormatterFactory) {
		final ImportProcessBean importProcessBean = getImportProcessBeanFromSession();
		return new PivotalDataBean((PivotalConfigBean) importProcessBean.getConfigBean(),
				pivotalSchemeManager, utils.isTimeTrackingOn());
	}

	public void validateRemoteSiteConnection(RemoteSiteImporterSetupPage importerSetupPage) {
		if (new RemoteSiteValidator().validateConnection(importerSetupPage)) {
			try {
				final PivotalClient pivotalClient = new CachingPivotalClient();
				pivotalClient.login(importerSetupPage.getSiteUsername(), importerSetupPage.getSitePassword());
				pivotalClient.logout();
			} catch (PivotalHttpException e) {
				if (e.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
					importerSetupPage.addErrorMessage(utils.getAuthenticationContext().getI18nHelper().getText(
							"jira-importer-plugin.external.pivotal.login.invalid.credentials"));
					logger.info("Problem while authenticating to Pivotal Tracker site", e);
				} else {
					importerSetupPage.addErrorMessage("Cannot login to Pivotal Tracker site: " + e.getMessage());
				}
			} catch (PivotalRemoteException e) {
				importerSetupPage.addErrorMessage("Cannot login to Pivotal Tracker site: " + e.getMessage());
			}
		}
	}

	@Override
	public List<String> getSteps() {
        return ImmutableList.of(PivotalSetupPage.class.getSimpleName(),
				PivotalProjectMappingsPage.class.getSimpleName());
	}
}
