/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal.web;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalClient;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.plugins.importer.web.AbstractSetupPage;
import com.atlassian.jira.plugins.importer.web.ImportProcessBean;
import com.atlassian.jira.plugins.importer.web.ImporterControllerFactory;
import com.atlassian.jira.plugins.importer.web.RemoteSiteImporterSetupPage;
import com.atlassian.jira.plugins.importer.web.SessionConnectionConfiguration;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.plugin.web.WebInterfaceManager;

public class PivotalSetupPage extends AbstractSetupPage implements RemoteSiteImporterSetupPage {

	private String siteUsername;
	private String sitePassword;
	private ImportProcessBean importBean;

	public PivotalSetupPage(ExternalUtils utils, UsageTrackingService usageTrackingService,
			ImporterControllerFactory importerControllerFactory, WebInterfaceManager webInterfaceManager) {
		super(utils, usageTrackingService, importerControllerFactory, webInterfaceManager);
	}


	@Override
	protected void doValidation() {
		super.doValidation();

		if (isPreviousClicked()) {
			return;
		}

		importBean = getController().createImportProcessBean(this);
	}

	@Override
	public String doDefault() throws Exception {
		if (!isAdministrator()) {
			return "denied";
		}
		return INPUT;
	}

	@Override
	@RequiresXsrfCheck
	protected String doExecute() throws Exception {
		getController().storeImportProcessBeanInSession(importBean);

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
		this.siteUsername = siteUsername;
	}

	public String getSitePassword() {
		return sitePassword;
	}

	public void setSitePassword(String sitePassword) {
		this.sitePassword = sitePassword;
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
}
