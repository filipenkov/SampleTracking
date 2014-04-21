/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.imports.config.UserNameMapper;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.imports.pivotal.web.PivotalProjectMappingsPage;
import com.atlassian.jira.plugins.importer.imports.pivotal.web.PivotalSetupPage;
import com.atlassian.jira.plugins.importer.imports.pivotal.web.PivotalUserMappingsPage;
import com.atlassian.jira.plugins.importer.web.AbstractImporterController;
import com.atlassian.jira.plugins.importer.web.AbstractSetupPage;
import com.atlassian.jira.plugins.importer.web.ConfigFileHandler;
import com.atlassian.jira.plugins.importer.web.ImportProcessBean;
import com.atlassian.jira.plugins.importer.web.RemoteSiteImporterSetupPage;
import com.atlassian.jira.plugins.importer.web.RemoteSiteValidator;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;
import com.google.common.collect.ImmutableList;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;

public class PivotalImporterController extends AbstractImporterController {
	private final ExternalUtils utils;
	private final PivotalSchemeManager pivotalSchemeManager;
	private final PivotalRapidBoardManager pivotalRapidBoardManager;
	private final ConfigFileHandler configFileHandler;

	private final Logger logger = Logger.getLogger(PivotalImporterController.class);
	public static final String PIVOTAL_IMPORT_CONFIG_BEAN = "issue.importer.jira.pivotal.import.bean";

	public PivotalImporterController(JiraDataImporter pivotalImporter, ExternalUtils utils,
			PivotalSchemeManager pivotalSchemeManager, PivotalRapidBoardManager pivotalRapidBoardManager, ConfigFileHandler configFileHandler) {
		super(pivotalImporter, PIVOTAL_IMPORT_CONFIG_BEAN, "Pivotal");
		this.utils = utils;
		this.pivotalSchemeManager = pivotalSchemeManager;
		this.pivotalRapidBoardManager = pivotalRapidBoardManager;
		this.configFileHandler = configFileHandler;
	}

	@Override
	public boolean createImportProcessBean(AbstractSetupPage setupPage) {
		final PivotalSetupPage pivotalSetupPage = (PivotalSetupPage) setupPage;

		validateRemoteSiteConnection(pivotalSetupPage);

		if (pivotalSetupPage.invalidInput()) {
			return false;
		}

		final SiteConfiguration siteConfiguration = new SiteConfiguration("http://www.pivotaltracker.com",
				pivotalSetupPage.getSiteCredentials(),
				pivotalSetupPage.getSiteUsername(),
				pivotalSetupPage.getSitePassword());

		final ImportProcessBean importProcessBean = new ImportProcessBean();
		importProcessBean.setUrlBean(siteConfiguration);

		final PivotalConfigBean configBean = new PivotalConfigBean(siteConfiguration, utils, this);
		configBean.setShowUserMappingPage(pivotalSetupPage.isShowUsernameMapping());

		if (!configFileHandler.populateFromConfigFile(pivotalSetupPage, configBean)) {
            return false;
        }

		importProcessBean.setConfigBean(configBean);

        storeImportProcessBeanInSession(importProcessBean);
		return true;
	}

	@Override
	public PivotalDataBean createDataBean() {
		return createDataBean(true);
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="BC_UNCONFIRMED_CAST")
	public PivotalDataBean createDataBean(boolean mapUserNames) {
		final PivotalConfigBean configBean = getConfigBeanFromSession();
		return new PivotalDataBean(configBean, pivotalSchemeManager, utils.isTimeTrackingOn(), pivotalRapidBoardManager, mapUserNames);
	}

	@Nullable
	PivotalConfigBean getConfigBeanFromSession() {
		final ImportProcessBean importProcessBean = getImportProcessBeanFromSession();
		return importProcessBean != null ? (PivotalConfigBean) importProcessBean.getConfigBean() : null;
	}

	public void validateRemoteSiteConnection(RemoteSiteImporterSetupPage importerSetupPage) {
		if (new RemoteSiteValidator().validateConnection(importerSetupPage)) {
			try {
				final PivotalClient pivotalClient = new CachingPivotalClient(UserNameMapper.NO_MAPPING);
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
		final ImmutableList.Builder<String> builder = ImmutableList.builder();

		builder.add(PivotalSetupPage.class.getSimpleName(), PivotalProjectMappingsPage.class.getSimpleName());

		final PivotalConfigBean config = getConfigBeanFromSession();
		if (config != null && config.isShowUserMappingPage()) {
			builder.add(PivotalUserMappingsPage.class.getSimpleName());
		}
		
		return builder.build();
	}

    @Override
    public boolean isUsingConfiguration() {
        return false;
    }
}
