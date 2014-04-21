/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.json.web;

import com.atlassian.jira.plugins.importer.extensions.ImporterController;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.plugins.importer.web.AbstractSetupPage;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.jira.web.util.WebAttachmentManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;

public class JsonSetupPage extends AbstractSetupPage {
	public static final String FILE_INPUT_NAME = "jsonFile";

	private final WebAttachmentManager webAttachmentManager;

	public JsonSetupPage(UsageTrackingService usageTrackingService, ExternalUtils utils,
                         WebAttachmentManager webAttachmentManager,
                         WebInterfaceManager webInterfaceManager,
                         PluginAccessor pluginAccessor) {
		super(utils, usageTrackingService, webInterfaceManager, pluginAccessor);
		this.webAttachmentManager = webAttachmentManager;
	}

	@Override
	protected void doValidation() {
		super.doValidation();

		if (isPreviousClicked()) {
			return;
		}

		try {
			AttachmentUtils.checkValidTemporaryAttachmentDirectory();
		} catch (AttachmentException e) {
			addError(FILE_INPUT_NAME, e.getMessage());
			return;
		}

		if (getMultipart() == null) {
			addError(FILE_INPUT_NAME, getText("jira-importer-plugin.csv.setup.page.file.is.empty"));
			return;
		}

		try {
			webAttachmentManager.validateAttachmentIfExists(getMultipart(), FILE_INPUT_NAME, true);
		} catch (final AttachmentException e) {
			addError(FILE_INPUT_NAME, e.getMessage());
			return;
		}
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

        if(!isPreviousClicked() && !controller.createImportProcessBean(this)) {
            return INPUT;
        }

		return super.doExecute();
	}

	@Override
	public String getFormTitle() {
		return getText("jira-importer-plugin.JsonSetupPage.form.title");
	}

    @Override
    public String getFormDescription() {
        return getText("jira-import-plugin.you.can.generate.json");
    }
}
