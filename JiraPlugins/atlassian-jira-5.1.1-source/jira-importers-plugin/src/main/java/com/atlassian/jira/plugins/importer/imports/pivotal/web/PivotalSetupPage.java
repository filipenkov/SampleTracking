/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal.web;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugins.importer.extensions.ImporterController;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalClient;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalRapidBoardManager;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.plugins.importer.web.AbstractSetupPage;
import com.atlassian.jira.plugins.importer.web.ConfigFileHandler;
import com.atlassian.jira.plugins.importer.web.RemoteSiteImporterSetupPage;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import org.apache.commons.lang.StringUtils;

public class PivotalSetupPage extends AbstractSetupPage implements RemoteSiteImporterSetupPage {

	private String siteUsername;
	private String sitePassword;
	private boolean showUsernameMapping;

	private final PivotalRapidBoardManager pivotalRapidBoardManager;
	private final ConfigFileHandler configFileHandler;

	public PivotalSetupPage(ExternalUtils utils, UsageTrackingService usageTrackingService,
			WebInterfaceManager webInterfaceManager,
			PivotalRapidBoardManager pivotalRapidBoardManager, ConfigFileHandler configFileHandler,
			PluginAccessor pluginAccessor) {
		super(utils, usageTrackingService, webInterfaceManager, pluginAccessor);
		this.pivotalRapidBoardManager = pivotalRapidBoardManager;
		this.configFileHandler = configFileHandler;
	}

	@Override
	protected void doValidation() {
		super.doValidation();

		if (!configFileHandler.verifyConfigFileParam(this)) {
			return;
		}

		if (isPreviousClicked()) {
			return;
		}
	}

	@Override
	public String doDefault() throws Exception {
		if (!isAdministrator()) {
			return "denied";
		}
		if (getController() == null) {
			return RESTART_NEEDED;
		}
		return INPUT;
	}

	@Override
	@RequiresXsrfCheck
	protected String doExecute() throws Exception {
		final ImporterController controller = getController();
		if (controller == null) {
			return RESTART_NEEDED;
		}

		if (!isPreviousClicked() && !controller.createImportProcessBean(this)) {
            return INPUT;
        }

		return super.doExecute();
	}

	public String getSiteUrl() {
		return PivotalClient.PIVOTAL_ROOT_URL;
	}

	public void setSiteUrl(String siteUrl) {
	}

	public String getSiteUsername() {
		return siteUsername;
	}

	public void setSiteUsername(String siteUsername) {
		this.siteUsername = StringUtils.trim(siteUsername);
	}

	public String getSitePassword() {
		return sitePassword;
	}

	public void setSitePassword(String sitePassword) {
		this.sitePassword = StringUtils.trim(sitePassword);
	}

	public boolean isShowUsernameMapping() {
		return showUsernameMapping;
	}

	public void setShowUsernameMapping(boolean showUsernameMapping) {
		this.showUsernameMapping = showUsernameMapping;
	}

	public boolean getSiteCredentials() {
		return true;
	}

	public boolean getAttachmentsEnabled() {
		return getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS);
	}

	public boolean getIssueLinkingEnabled() {
		return getApplicationProperties().getOption(APKeys.JIRA_OPTION_ISSUELINKING);
	}

	@Override
	public boolean isIssueLinkingEnabled() {
		return true; // no issue linking needed
	}

	@Override
	public boolean isSiteUrlRequired() {
		return false;
	}

	@Override
	public String getFormTitle() {
		return getText("jira-importer-plugin.wizard.connect.pivotal.title");
	}

	@Override
	public String getFormDescription() {
		return getText("jira-importer-plugin.wizard.connect.pivotal.description");
	}

	@Override
	public String getLoginLabel() {
		return getText("jira-importer-plugin.wizard.connect.pivotal.login.label");
	}

	@Override
	public String getPasswordLabel() {
		return getText("jira-importer-plugin.wizard.connect.pivotal.password.label");
	}

	@SuppressWarnings("unused")
	public boolean isGreenHooperFeaturesEnabled() {
		return pivotalRapidBoardManager.isGreenHooperFeaturesEnabled();
	}

	@SuppressWarnings("unused")
	public boolean isGreenHopperInstalledAndEnabled() {
		return pivotalRapidBoardManager.isGreenHopperInstalledAndEnabled();
	}

}
