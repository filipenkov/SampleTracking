/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.pivotal.web;

import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.plugins.importer.web.ImporterValueMappingsPage;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;

public class PivotalUserMappingsPage extends ImporterValueMappingsPage {
	public PivotalUserMappingsPage(UsageTrackingService usageTrackingService,
			WebInterfaceManager webInterfaceManager, PluginAccessor pluginAccessor) {
		super(usageTrackingService, webInterfaceManager, pluginAccessor);
	}

	@Override
	public String getFormTitle() {
		return getText("jira-importer-plugin.wizard.usermappings.title");
	}
}
