/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */
package com.atlassian.jira.plugins.importer.imports.fogbugz.hosted;

import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.imports.fogbugz.web.FogBugzHostedSetupPage;
import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.web.*;
import com.google.common.collect.Lists;

import java.util.List;

public class FogBugzHostedImporterController extends AbstractImporterController {
	private final ExternalUtils utils;
    private final ConfigFileHandler configFileHandler;

	public FogBugzHostedImporterController(JiraDataImporter fogbugzImporter, ExternalUtils utils, ConfigFileHandler configFileHandler) {
		super(fogbugzImporter, "issue.importer.jira.fogbugz.hosted.import.bean", "FogBugzHosted");
		this.utils = utils;
        this.configFileHandler = configFileHandler;
    }

	@Override
	public boolean createImportProcessBean(AbstractSetupPage setupPage) {
		final FogBugzHostedSetupPage fogBugzHostedSetupPage = (FogBugzHostedSetupPage) setupPage;

		validateRemoteSiteConnection(fogBugzHostedSetupPage);

		if (fogBugzHostedSetupPage.invalidInput()) {
			return false;
		}

		final SiteConfiguration siteConfiguration = new SiteConfiguration(
				fogBugzHostedSetupPage.getSiteUrl(),
				fogBugzHostedSetupPage.getSiteCredentials(),
				fogBugzHostedSetupPage.getSiteUsername(),
				fogBugzHostedSetupPage.getSitePassword());

		final ImportProcessBean importProcessBean = new ImportProcessBean();
		importProcessBean.setUrlBean(siteConfiguration);

		final FogBugzHostedConfigBean configBean = new FogBugzHostedConfigBean(
				new FogBugzClient(fogBugzHostedSetupPage.getSiteUrl(),
						fogBugzHostedSetupPage.getSiteUsername(), fogBugzHostedSetupPage.getSitePassword()), utils);

        if (!configFileHandler.populateFromConfigFile(fogBugzHostedSetupPage, configBean)) {
            return false;
        }

        importProcessBean.setConfigBean(configBean);

        storeImportProcessBeanInSession(importProcessBean);
		return true;
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="BC_UNCONFIRMED_CAST")
	@Override
	public ImportDataBean createDataBean() {
		final ImportProcessBean importProcessBean = getImportProcessBeanFromSession();
		return new FogBugzHostedDataBean((FogBugzHostedConfigBean) importProcessBean.getConfigBean());
	}

	@Override
	public List<String> getSteps() {
		return Lists.newArrayList(
				FogBugzHostedSetupPage.class.getSimpleName(),
				ImporterProjectMappingsPage.class.getSimpleName(),
				ImporterFieldMappingsPage.class.getSimpleName(),
				ImporterValueMappingsPage.class.getSimpleName(),
				ImporterLinksPage.class.getSimpleName()
		);
	}

	public void validateRemoteSiteConnection(RemoteSiteImporterSetupPage importerSetupPage) {
		if (new RemoteSiteValidator().validateConnection(importerSetupPage)) {
			try {
				final FogBugzClient client = new FogBugzClient(importerSetupPage.getSiteUrl(),
						importerSetupPage.getSiteUsername(), importerSetupPage.getSitePassword());
				client.login();
				client.logout();
			} catch (FogBugzRemoteException e) {
				importerSetupPage.addErrorMessage("Cannot login to FogBugz site: " + e.getMessage());
			}
		}
	}
}

