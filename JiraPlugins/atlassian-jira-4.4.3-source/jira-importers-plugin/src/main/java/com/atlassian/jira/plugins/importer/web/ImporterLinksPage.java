/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.plugin.web.WebInterfaceManager;
import webwork.action.ActionContext;

public class ImporterLinksPage extends ImporterProcessSupport.Database {

	public ImporterLinksPage(ImporterControllerFactory importerControllerFactory,
			UsageTrackingService usageTrackingService, WebInterfaceManager webInterfaceManager) {
		super(usageTrackingService, importerControllerFactory, webInterfaceManager);
	}

	@Override
    @RequiresXsrfCheck
	protected void doValidation() {
		super.doValidation();

        if (getConfigBean() != null) {
		    getConfigBean().populateLinkMappings(ActionContext.getParameters());
        }
	}

	@Override
	public String getFormTitle() {
		return "Setup links";
	}
}
