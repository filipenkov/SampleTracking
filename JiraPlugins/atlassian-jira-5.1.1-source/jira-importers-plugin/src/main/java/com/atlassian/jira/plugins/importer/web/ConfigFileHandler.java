/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractConfigBean;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.jira.web.util.WebAttachmentManager;
import org.codehaus.jackson.JsonParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import webwork.multipart.MultiPartRequestWrapper;

import java.io.File;

@Component
public class ConfigFileHandler {
	public static final String CONFIG_FILE_INPUT_NAME = "configFile";

	private final WebAttachmentManager webAttachmentManager;

	@Autowired
	public ConfigFileHandler(WebAttachmentManager webAttachmentManager) {
		this.webAttachmentManager = webAttachmentManager;
	}

	public boolean verifyConfigFileParam(AbstractSetupPage setupPage) {
		try {
			AttachmentUtils.checkValidTemporaryAttachmentDirectory();
		} catch (AttachmentException e) {
			setupPage.addError(CONFIG_FILE_INPUT_NAME, e.getMessage());
			return false;
		}

		final MultiPartRequestWrapper multipart = setupPage.getMultipart();
		if (multipart != null) {
			try {
				webAttachmentManager.validateAttachmentIfExists(multipart, CONFIG_FILE_INPUT_NAME, false);
			} catch (final AttachmentException e) {
				setupPage.addError(CONFIG_FILE_INPUT_NAME, e.getMessage());
				return false;
			}
		}
		return true;
	}

	public boolean populateFromConfigFile(AbstractSetupPage setupPage, AbstractConfigBean configBean) {
		final File configFile = setupPage.getMultipart().getFile(CONFIG_FILE_INPUT_NAME);
		if (configFile != null) {
			try {
				configBean.copyFromProperties(configFile);
			} catch (Exception e) {
				if (e instanceof JsonParseException) {
					setupPage.addErrorMessage(
							setupPage.getText("jira-importer-plugin.import.setup.page.config.file.format.changed"));
				}
				setupPage.addError(CONFIG_FILE_INPUT_NAME, e.getMessage());
				return false;
			}
		}
		return true;
	}

}
