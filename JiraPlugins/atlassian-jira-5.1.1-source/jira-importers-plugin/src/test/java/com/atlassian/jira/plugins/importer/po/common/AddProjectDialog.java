/*
 * Copyright (c) 2012. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.common;

import com.atlassian.jira.pageobjects.components.fields.SingleSelect;
import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import static org.junit.Assert.assertTrue;

/**
 * This is a copy with some modifications of JIRA AddProjectDialog PO.
 * JIM has a little bit different requirements so we use our own copy (e.g. our dialog can be used for editing too)
 *
 */
public class AddProjectDialog extends FormDialog {
	private PageElement nameElement;
	private PageElement keyElement;
	private PageElement keyManuallyEdited;
	private PageElement submit;
	private PageElement leadContainer;
	private SingleSelect leadSelect;

	public AddProjectDialog() {
		super("jim-create-project-dialog");
	}

	@Init
	public void init() {
		nameElement = find(By.name("name"));
		keyElement = find(By.name("key"));
		submit = find(By.name("Add"));
		leadContainer = find(By.id("lead-picker"));
		leadSelect = binder.bind(SingleSelect.class, leadContainer);
		keyManuallyEdited = find(By.name("keyEdited"));
		if (keyManuallyEdited != null) {
			// Prevent auto key generation. You have to enter your key manually in tests. See CreateProjectField.js
			driver.executeScript("jQuery('input[name=keyEdited]').val('true')");
		}
	}

	public void setFields(String key, String name, String lead) {
		setKey(key);
		setName(name);
		if (lead != null) {
			setLead(lead);
		}
	}

	public AddProjectDialog setName(String name) {
		assertDialogOpen();
		setElement(nameElement, name);
		return this;
	}

	public AddProjectDialog setKey(String key) {
		assertDialogOpen();
		setElement(keyElement, key);
		return this;
	}

	public String getKey() {
		assertDialogOpen();
		return keyElement.getValue();
	}

	public String getName() {
		assertDialogOpen();
		return nameElement.getValue();
	}

	public AddProjectDialog setLead(String lead) {
		assertDialogOpen();
		assertTrue("The lead element is not present. Only one user in the system?", isLeadPresent());
		leadSelect.select(lead);
		return this;
	}

	public boolean isLeadPresent() {
		return leadContainer.isPresent();
	}

	public AddProjectDialog submitFail() {
		submit(submit);
		assertDialogOpen();
		return this;
	}

	public <T> T submitSuccess(Class<T> clazz) {
		submit(submit);
		assertDialogClosed();
		return binder.bind(clazz);
	}

	public boolean isLeadpickerDisabled() {
		return leadSelect.isAutocompleteDisabled();
	}
}

