/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal.web;

import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.plugins.importer.web.ImporterValueMappingsPage;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;

public class PivotalValueMappingsPage extends ImporterValueMappingsPage {
	private static final String PREVIOUS_STEP = PivotalProjectMappingsPage.class.getSimpleName();

	public PivotalValueMappingsPage(UsageTrackingService usageTrackingService,
			WebInterfaceManager webInterfaceManager, PluginAccessor pluginAccessor) {
		super(usageTrackingService, webInterfaceManager, pluginAccessor);
	}

	@Override
	protected String doExecute() throws Exception {
		final int previousStep = getController().getSteps().indexOf(PREVIOUS_STEP);
		final int goToStep = !isFinishClicked() && isNextClicked() ? previousStep + 1 : previousStep;
		return getRedirect(getController().getSteps().get(goToStep) + "!default.jspa?externalSystem=" + getExternalSystem());
	}

	@Override
	public int getCurrentStep() {
		// we lie here about the current step to make non-linear wizard possible
		return getController().getSteps().indexOf(PREVIOUS_STEP) + 1;
	}
}
