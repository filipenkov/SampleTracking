/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.core.util.FileSize;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.plugin.web.WebInterfaceManager;
import webwork.action.ServletActionContext;
import webwork.config.Configuration;
import webwork.multipart.MultiPartRequestWrapper;

public abstract class AbstractSetupPage extends ImporterProcessSupport {
	protected final ExternalUtils utils;
	private boolean useConfigFile;

	public AbstractSetupPage(ExternalUtils utils, UsageTrackingService usageTrackingService,
			ImporterControllerFactory importerControllerFactory, WebInterfaceManager webInterfaceManager) {
		super(usageTrackingService, importerControllerFactory, webInterfaceManager);
		this.utils = utils;
	}

	public boolean isIssueLinkingEnabled() {
		return getApplicationProperties().getOption(APKeys.JIRA_OPTION_ISSUELINKING);
	}

	public boolean getTimeTrackingEnabled() {
		return utils.getFieldManager().isTimeTrackingOn();
	}

	public MultiPartRequestWrapper getMultipart() {
		return ServletActionContext.getMultiPartRequest();
	}

	public Long getAttachmentSize() {
		return new Long(Configuration.getString(APKeys.JIRA_ATTACHMENT_SIZE));
	}

	public FileSize getFileSize() {
		return new FileSize();
	}

	@Override
	public String getFormTitle() {
		return getText("jira-importer-plugin.config.wizard.title.prefix", getTitle()) + " " + getText("jira-importer-plugin.external.mappings.setup");
	}

	@SuppressWarnings("unused")
	public void setUseConfigFile(boolean useConfigFile) {
		this.useConfigFile = useConfigFile;
	}

	@SuppressWarnings("unused")
	public boolean getUseConfigFile() {
		return useConfigFile;
	}
}
