/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.web;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugins.importer.extensions.ImporterController;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.plugins.importer.web.AbstractSetupPage;
import com.atlassian.jira.plugins.importer.web.ConfigFileHandler;
import com.atlassian.jira.plugins.importer.web.RemoteSiteImporterSetupPage;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import org.apache.commons.lang.StringUtils;

public class FogBugzHostedSetupPage extends AbstractSetupPage implements RemoteSiteImporterSetupPage {

	private String siteUrl;
	private String siteUsername;
	private String sitePassword;
    private final ConfigFileHandler configFileHandler;

    public FogBugzHostedSetupPage(ExternalUtils utils, UsageTrackingService usageTrackingService,
			WebInterfaceManager webInterfaceManager, PluginAccessor pluginAccessor, ConfigFileHandler configFileHandler) {
		super(utils, usageTrackingService, webInterfaceManager, pluginAccessor);
        this.configFileHandler = configFileHandler;
    }


	@Override
	protected void doValidation() {
		if (isPreviousClicked()) {
			return;
		}

        super.doValidation();

        if (!configFileHandler.verifyConfigFileParam(this)) return;
	}

	@Override
	public String doDefault() throws Exception {
		if (!isAdministrator()) {
			return "denied";
		}

		final ImporterController controller = getController();
		if (controller == null) {
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
		return siteUrl;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = StringUtils.trim(siteUrl);
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
