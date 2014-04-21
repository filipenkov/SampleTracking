/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import webwork.action.ActionContext;

public class ImporterValueMappingsPage extends ImporterProcessSupport.Database {

	public ImporterValueMappingsPage(UsageTrackingService usageTrackingService,
			WebInterfaceManager webInterfaceManager, PluginAccessor pluginAccessor) {
		super(usageTrackingService, webInterfaceManager, pluginAccessor);
	}

	@Override
    @RequiresXsrfCheck
	protected void doValidation() {
		super.doValidation();

        if (getConfigBean() != null) {
		    getConfigBean().getValueMappingHelper().populateValueMappings(ActionContext.getParameters());
        }
	}

	@Override
	public String getFormTitle() {
		return getText("jira-importer-plugin.external.mappings.value");
	}

	@Override
	public String doDefault() throws Exception {
		if (getConfigBean() != null) {
			getConfigBean().getValueMappingHelper().initDistinctValuesCache();
		}
		return super.doDefault();
	}
}
