/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web.trac;

import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.plugins.importer.web.AbstractSetupPage;
import com.atlassian.jira.plugins.importer.web.ImportProcessBean;
import com.atlassian.jira.plugins.importer.web.ImporterControllerFactory;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.jira.web.util.WebAttachmentManager;
import com.atlassian.plugin.web.WebInterfaceManager;

public class TracSetupPage extends AbstractSetupPage {
    public static final String FILE_INPUT_NAME = "environmentFile";
	public static final String CONFIG_FILE_INPUT_NAME = "configFile";

    private final WebAttachmentManager webAttachmentManager;
	private ImportProcessBean importBean;

	public TracSetupPage(UsageTrackingService usageTrackingService, ExternalUtils utils,
			WebAttachmentManager webAttachmentManager,
			ImporterControllerFactory importerControllerFactory, WebInterfaceManager webInterfaceManager) {
        super(utils, usageTrackingService, importerControllerFactory, webInterfaceManager);
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

		try {
			webAttachmentManager.validateAttachmentIfExists(getMultipart(), CONFIG_FILE_INPUT_NAME, false);
		} catch (final AttachmentException e) {
			addError(CONFIG_FILE_INPUT_NAME, e.getMessage());
			return;
		}

		if (!invalidInput()) {
			importBean = getController().createImportProcessBean(this);
		}
    }

    @Override
    public String doDefault() throws Exception {
        if (!isAdministrator()) {
			return "denied";
		}

		getController().storeImportProcessBeanInSession(null);

		return INPUT;
    }

    @Override
    @RequiresXsrfCheck
	protected String doExecute() throws Exception {
        getController().storeImportProcessBeanInSession(importBean);

		return super.doExecute();
	}

}
