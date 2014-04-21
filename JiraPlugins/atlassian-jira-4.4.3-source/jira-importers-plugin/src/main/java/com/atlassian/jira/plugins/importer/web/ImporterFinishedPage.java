/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.plugins.importer.web.wizard.TabsSimpleLinkFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.sal.api.websudo.WebSudoNotRequired;

import javax.annotation.Nullable;

@WebSudoNotRequired
public class ImporterFinishedPage extends ImporterProcessSupport {

	private final ImporterControllerFactory importerControllerFactory;

	public ImporterFinishedPage(
			UsageTrackingService usageTrackingService,
			ImporterControllerFactory importerControllerFactory,
			WebInterfaceManager webInterfaceManager) {
		super(usageTrackingService, importerControllerFactory, webInterfaceManager);
		this.importerControllerFactory = importerControllerFactory;
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
		ImporterController controller = importerControllerFactory.getController(getExternalSystem());
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

	public String convertToNiceHtmlString(String str) {
		return TextsUtil.convertToNiceHtmlString(str);
	}
}

