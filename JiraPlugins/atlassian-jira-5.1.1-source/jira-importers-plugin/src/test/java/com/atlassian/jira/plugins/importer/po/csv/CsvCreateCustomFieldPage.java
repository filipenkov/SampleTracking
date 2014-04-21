/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.csv;

import com.atlassian.jira.plugins.importer.po.common.AbstractImporterWizardPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class CsvCreateCustomFieldPage extends AbstractImporterWizardPage {
	@Override
	public String getUrl() {
		return null;
	}

	@FindBy(css = ".aui-popup input[name='customFieldName']")
	private WebElement fieldName;

	@FindBy(css = ".aui-popup select[name='customFieldType']")
	private WebElement fieldType;

	@FindBy(css = ".aui-popup input[type='submit']")
	private WebElement submitButton;

	@FindBy(css = ".aui-popup .cancel")
	private WebElement cancelButton;

	public void submit() {
		submitButton.click();
	}

	public CsvCreateCustomFieldPage setFieldName(String name) {
		fieldName.sendKeys(name);
		return this;
	}

	public CsvCreateCustomFieldPage setFieldType(String fieldType) {
		this.fieldType.findElement(By.xpath("descendant::option[@value='" + fieldType + "']")).click();
		return this;
	}
}
