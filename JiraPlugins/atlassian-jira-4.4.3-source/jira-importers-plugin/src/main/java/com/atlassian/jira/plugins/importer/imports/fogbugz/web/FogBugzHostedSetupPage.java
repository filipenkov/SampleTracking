/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.web;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.plugins.importer.web.AbstractSetupPage;
import com.atlassian.jira.plugins.importer.web.ImportProcessBean;
import com.atlassian.jira.plugins.importer.web.ImporterControllerFactory;
import com.atlassian.jira.plugins.importer.web.RemoteSiteImporterSetupPage;
import com.atlassian.jira.plugins.importer.web.SessionConnectionConfiguration;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.plugin.web.WebInterfaceManager;

public class FogBugzHostedSetupPage extends AbstractSetupPage implements RemoteSiteImporterSetupPage {

	private String siteUrl;
	private String siteUsername;
	private String sitePassword;
	private ImportProcessBean importBean;

	public FogBugzHostedSetupPage(ExternalUtils utils, UsageTrackingService usageTrackingService,
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

		getController().storeImportProcessBeanInSession(null);

		return INPUT;
	}

	@Override
	@RequiresXsrfCheck
	protected String doExecute() throws Exception {
		getController().storeImportProcessBeanInSession(importBean);

		return super.doExecute();
	}

	public String getSiteUrl() {
		return siteUrl;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
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
	public boolean isSubtasksEnabled() {
		return true;
	}

	@Override
	public boolean isSiteUrlRequired() {
		return true;
	}

	@Override
	public String getFormTitle() {
		return getText("jira-importer-plugin.wizard.connect.fogbugz.title");
	}

	@Override
	public String getFormDescription() {
		return getText("jira-importer-plugin.wizard.connect.fogbugz.description");
	}

	@Override
	public String getLoginLabel() {
		return getText("jira-importer-plugin.wizard.connect.fogbugz.login.label");
	}

	@Override
	public String getPasswordLabel() {
		return getText("jira-importer-plugin.wizard.connect.fogbugz.password.label");
	}
}
