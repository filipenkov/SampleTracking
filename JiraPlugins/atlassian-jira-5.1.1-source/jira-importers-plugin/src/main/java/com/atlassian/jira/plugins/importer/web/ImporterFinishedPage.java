/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.plugins.importer.extensions.ImporterController;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.plugins.importer.web.wizard.TabsSimpleLinkFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.sal.api.websudo.WebSudoNotRequired;

import javax.annotation.Nullable;

@WebSudoNotRequired
public class ImporterFinishedPage extends ImporterProcessSupport {

	public ImporterFinishedPage(
			UsageTrackingService usageTrackingService,
			WebInterfaceManager webInterfaceManager,
			PluginAccessor pluginAccessor) {
		super(usageTrackingService, webInterfaceManager, pluginAccessor);
    }

	@Override
	public String doDefault() throws Exception {
		return doExecute();
	}

	@Override
	protected String doExecute() throws Exception {
		if (!isAdministrator()) {
			return "denied";
		}

		if (getImporter() != null && getImporter().getStats() != null) {
			return INPUT;
		} else {
			return "restartimporterneeded";
		}
	}

	@Nullable
	public JiraDataImporter getImporter() {
		ImporterController controller = getController();
		return controller != null ? controller.getImporter() : null;
	}

	@Override
	@Nullable
	public String getFormTitle() {
		return null;
	}

	@Override
	public String getWizardActiveTab() {
		return TabsSimpleLinkFactory.STEP_I18N_FINAL;
	}
}

