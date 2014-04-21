/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.po.common;


import com.atlassian.pageobjects.binder.ValidateState;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class ImporterValueMappingsPage extends AbstractImporterWizardPage {

	@ValidateState
	public void validate() {
		Assert.assertEquals("Verifying page is in expected tab", "ImporterValueMappingsPage", getActiveTabMeta());
		Assert.assertEquals("Setup value mappings", getPageTitle());
	}

	@Override
	public String getUrl() {
		return null;
	}

	public ImporterLinksPage next() {
		Assert.assertTrue("Next button is disabled", nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(ImporterLinksPage.class);
	}

	public ImporterLogsPage nextToLogs() {
		Assert.assertTrue("Next button is disabled", nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(ImporterLogsPage.class);
	}


	public ImporterFieldMappingsPage prev() {
		Assert.assertTrue(previousButton.isEnabled());
		previousButton.click();
		return pageBinder.bind(ImporterFieldMappingsPage.class);
	}

	public void setMappingSelect(String field, String label, String value) {
		final String id = getMappingId(field, label);
		final By explicitSelect = By.id(id + "_select");
		final WebElement element = driver.elementExists(explicitSelect)
				? driver.findElement(explicitSelect)
				: driver.findElement(By.id(id));
		new Select(element).selectByValue(value);
	}

	public ImporterValueMappingsPage setMapping(String field, String label, String newValue) {
		final String id = getMappingId(field, label);
		final WebElement element = driver.findElement(By.id(id));
		element.sendKeys(newValue);

		return this;
	}

	public String getMappingId(String field, String label) {
		//final String query = String.format("//tr[td='%s']//*[normalize-space(self::label)='%s']", field, label);
		final String query = String.format("//label[text()='%s']", label);
		return driver.findElement(By.xpath(query)).getAttribute("for");
	}

	public String getMappingValue(String field, String label) {
		final String id = getMappingId(field, label);
		return driver.findElement(By.id(id)).getValue();
	}

	public boolean hasMappingFor(String field) {
		final String query = String.format("//tr[td='%s']", field);
		return driver.elementExists(By.xpath(query));
	}
}
