/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import javax.annotation.Nullable;

@WebSudoRequired
public class ExternalImport extends ImporterProcessSupport {

	public ExternalImport(UsageTrackingService usageTrackingService,
			ImporterControllerFactory importerControllerFactory, WebInterfaceManager webInterfaceManager) {
		super(usageTrackingService, importerControllerFactory, webInterfaceManager);
	}

	@Override
	public String doDefault() throws Exception {
		return INPUT;
	}

	@Override
	public String doExecute() throws Exception {
		if (!isAdministrator()) {
			return "denied";
		}

		return "success";
	}

    @SuppressWarnings("unused")
	public String doInitialOptIn() throws Exception {
		if (!isAdministrator()) {
			return "denied";
		}
		getUsageTrackingService().activate();
		getUsageTrackingService().includeTrackingWhenActive();
		return doExecute();
	}

    @SuppressWarnings("unused")
	public String doInitialOptOut() throws Exception {
		if (!isAdministrator()) {
			return "denied";
		}
		getUsageTrackingService().deactivate();
		return doExecute();
	}

	public ImporterControllerFactory getImporterControllerFactory() {
		return importerControllerFactory;
	}

	@Override
	@Nullable
	public String getFormTitle() {
		return getText("jira-importer-plugin.external.external.import");
	}

	@Override
	public String getWizardActiveSection() {
		return "admin_system_menu/top_system_section/import_export_section";
	}

	@Override
	public String getWizardActiveTab() {
		return "external_import";
	}
}
