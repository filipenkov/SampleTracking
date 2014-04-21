/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.po.common;


import com.atlassian.pageobjects.binder.ValidateState;
import org.junit.Assert;
import org.openqa.selenium.By;

public class ImporterFieldMappingsPage extends AbstractImporterWizardPage {

	@ValidateState
	public void validate() {
		Assert.assertEquals("Verifying page is in expected tab", "ImporterFieldMappingsPage", getActiveTabMeta());
		Assert.assertEquals("Set up field mappings", getPageTitle());
	}

	@Override
	public String getUrl() {
		return null;
	}

	public ImporterValueMappingsPage next() {
		Assert.assertTrue("Next button is disabled", nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(ImporterValueMappingsPage.class);
	}

	public boolean isMappingSelected(String field) {
		return driver.findElement(By.id(field)).isSelected();
	}

	public ImporterFieldMappingsPage setMappingEnabled(String field, boolean enabled) {
		setCheckbox(field, enabled); // straight field name -> id mapping
		return this;
	}

	public String getWorkflowScheme() {
		return getValue("workflowScheme");
	}

	public ImporterFieldMappingsPage setWorkflowScheme(String scheme) {
		setSelectByValue("workflowScheme", scheme);
		return this;
	}
}
