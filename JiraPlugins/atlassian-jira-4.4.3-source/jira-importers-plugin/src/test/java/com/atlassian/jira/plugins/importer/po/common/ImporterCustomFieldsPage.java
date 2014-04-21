/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.po.common;

import com.atlassian.pageobjects.binder.ValidateState;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ImporterCustomFieldsPage extends AbstractImporterWizardPage {

	@Override
	public String getUrl() {
		return null;
	}

	@ValidateState
	public void validate() {
		Assert.assertEquals("Verifying page is in expected tab", "ImporterCustomFieldsPage", getActiveTabMeta());
		Assert.assertEquals("Setup custom fields", getPageTitle());
	}

	public ImporterCustomFieldsPage selectFieldMapping(String field, String mapping) {
        WebElement select = driver.findElement(By.id(field + "_select"));
        for(WebElement element : select.findElements(By.tagName("option"))) {
            if (mapping.equals(element.getValue())) {
                element.setSelected();
            }
        }
		return this;
	}

	public ImporterCustomFieldsPage selectOtherFieldMapping(String field, String mapping) {
		selectFieldMapping(field, "OTHER_VALUE");
        WebElement otherInput = driver.findElement(By.name(field));
        otherInput.clear();
		otherInput.sendKeys(mapping);
		return this;
	}

	public ImporterFieldMappingsPage next() {
		Assert.assertTrue("Next button is disabled", nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(ImporterFieldMappingsPage.class);
	}
}
