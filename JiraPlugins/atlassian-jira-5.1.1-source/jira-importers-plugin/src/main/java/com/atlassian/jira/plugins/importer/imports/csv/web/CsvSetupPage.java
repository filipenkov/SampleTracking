/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.csv.web;

import com.atlassian.jira.plugins.importer.extensions.ImporterController;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.imports.csv.CsvConfigBean;
import com.atlassian.jira.plugins.importer.imports.csv.CsvFieldNameValidator;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.plugins.importer.web.AbstractSetupPage;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.jira.web.util.WebAttachmentManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.EOFException;
import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Set;

public class CsvSetupPage extends AbstractSetupPage {
	public static final String CSV_FILE_INPUT_NAME = "csvFile";
	public static final String CONFIG_FILE_INPUT_NAME = "configFile";
	private static final String TAB_STRING = "\\t";

	private final WebAttachmentManager webAttachmentManager;

	private String delimiter = CsvConfigBean.DEFAULT_DELIMITER.toString();
	private String encoding = CsvConfigBean.DEFAULT_ENCODING;

	public CsvSetupPage(UsageTrackingService usageTrackingService, ExternalUtils utils,
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
			addError(CSV_FILE_INPUT_NAME, e.getMessage());
			return;
		}

		if (getMultipart() == null) {
			addError(CSV_FILE_INPUT_NAME, getText("jira-importer-plugin.csv.setup.page.file.is.empty"));
			return;
		}

		try {
			webAttachmentManager.validateAttachmentIfExists(getMultipart(), CSV_FILE_INPUT_NAME, true);
		} catch (final AttachmentException e) {
			addError(CSV_FILE_INPUT_NAME, e.getMessage());
			return;
		}

		try {
			webAttachmentManager.validateAttachmentIfExists(getMultipart(), CONFIG_FILE_INPUT_NAME, false);
		} catch (final AttachmentException e) {
			addError(CONFIG_FILE_INPUT_NAME, e.getMessage());
			return;
		}

		final File temporaryCsvFile = getMultipart().getFile(CSV_FILE_INPUT_NAME);

		if (StringUtils.isNotEmpty(getDelimiter()) && getDelimiter().length() > 1) {
			addError("delimiter", getText("jira-importer-plugin.csv.setup.page.must.be.one.character"));
		}

		if (!Charset.isSupported(getEncoding())) {
			addError("encoding", getText("jira-importer-plugin.csv.file.invalid.encoding"));
		}

		if (!invalidInput()) {
			try {
				final Set<String> headerRow = new CsvConfigBean(temporaryCsvFile, getEncoding(),
						getDelimiterChar(), utils).getHeaderRow();
				final CsvFieldNameValidator validator = new CsvFieldNameValidator();
				int index = 0;
				for (Iterator<String> it = headerRow.iterator(); it.hasNext(); index++) {
					final String header = it.next();
					final Set<CsvFieldNameValidator.Error> errors = validator.check(header);
					if (!errors.isEmpty()) {
						addErrorMessage(header, index, errors);
					}
				}
			} catch (Exception e) {
				if (e.getCause() instanceof EOFException) {
					addError(CSV_FILE_INPUT_NAME, getText("jira-importer-plugin.csv.setup.page.file.is.empty"));
				} else {
					addErrorMessage(e.getMessage());
				}
			}
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

	/**
	 * Set's the delimiter property. An extra check is done to see if the delimiter
	 * is a '\t' if the passed delimiter is, it will be set via <code>setTabCharacter(String delimter)</code>
	 *
	 * @param delimiter
	 */
	@SuppressWarnings("unused")
	public void setDelimiter(String delimiter) {
		if (!setTabCharacter(delimiter))
			this.delimiter = delimiter;
	}

	/**
	 * Method to test if the user entered a 'tab' character and set it as the delimiter if found.
	 *
	 * @param delimeter
	 * @return boolean 'true' if we found and set the delimter as a tab character
	 */
	private boolean setTabCharacter(String delimeter) {
		if (TAB_STRING.equals(delimeter)) {
			// We have a valid tab delimiter, set this as the delimiter
			this.delimiter = String.valueOf('\t');
			return true;
		}
		return false;
	}

	public String getDelimiter() {
		return delimiter;
	}

	@Nullable
	public Character getDelimiterChar() {
		if (delimiter != null && delimiter.length() >= 1)
			return Character.valueOf(delimiter.charAt(0));
		else
			return null;
	}

	public String getEncoding() {
		return encoding;
	}

	@SuppressWarnings("unused")
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	private void addErrorMessage(String header, int index, Set<CsvFieldNameValidator.Error> errors) {
		final String sb = StringUtils.join(Iterables.transform(errors,
				new Function<CsvFieldNameValidator.Error, String>() {
					public String apply(@Nonnull CsvFieldNameValidator.Error from) {
						return MessageFormat.format("''{0}''", getText(from.getKey()));
					}
				}).iterator(), ", ");
		addErrorMessage(getText("jira-importer-plugin.csv.setup.page.first.row", header, Integer.toString(index + 1), sb));
	}

	@Override
	@RequiresXsrfCheck
	protected String doExecute() throws Exception {
		final ImporterController controller = getController();
		if (controller == null) {
			return RESTART_NEEDED;
		}

		if (!isPreviousClicked() && !controller.createImportProcessBean(this)) {
            return INPUT;
        }

		return super.doExecute();
	}

	@Override
	public String getFormTitle() {
		return getText("jira-importer-plugin.CsvSetupPage.form.title");
	}

}
