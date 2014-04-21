/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.csv;

import com.atlassian.jira.plugins.importer.po.common.AbstractImporterWizardPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterProjectsMappingsPage;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class CsvProjectMappingsPage extends AbstractImporterWizardPage {

	@FindBy(name = "userEmailSuffix")
	private WebElement userEmailSuffix;

	@FindBy(name = "dateImportFormat")
	private WebElement dateImportFormat;

	public String getUrl() {
		return "/secure/admin/views/CsvProjectMappingsPage!default.jspa?externalSystem=CSV";
	}

	public CsvProjectMappingsPage setReadFromCsv(boolean readFromCsv) {
		driver.findElement(By.id(readFromCsv ? "CSV_project_uncheck" : "CSV_project_check")).click();
		return this;
	}

	public boolean isReadingFromCsv() {
		return driver.findElement(By.id("CSV_project_uncheck")).isSelected();
	}



	public CsvFieldMappingsPage next() {
		Assert.assertTrue(nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(CsvFieldMappingsPage.class);
	}

	public String getUserEmailSuffix() {
		return userEmailSuffix.getValue();
	}

	public CsvProjectMappingsPage setUserEmailSuffix(String userEmailSuffix) {
		this.userEmailSuffix.clear();
		this.userEmailSuffix.sendKeys(userEmailSuffix);
		return this;
	}

	public String getDateImportFormat() {
		return dateImportFormat.getValue();
	}

	public CsvProjectMappingsPage setDateImportFormat(String format) {
		dateImportFormat.clear();
		dateImportFormat.sendKeys(format);
		return this;
	}

	public CsvProjectMappingsPage nextWithError() {
		assertNextEnabled();
		nextButton.submit();
		return this;
	}

	public CsvProjectMappingsPage createProject(String name, String key) {
		driver.findElement(By.cssSelector("#CSV-select-single-select span.drop-menu")).click();
		driver.waitUntilElementIsVisible(By.cssSelector("#CSV-select-suggestions a[title='Create New']"));
		driver.findElement(By.cssSelector("#CSV-select-suggestions a[title='Create New']")).click();
		driver.waitUntilElementIsVisible(By.name("name"));

		ImporterProjectsMappingsPage.fillProjectDetails(driver, name, key);
		return this;
	}

	public String getProjectName() {
		return driver.findElement(By.id("CSV_project_name")).getValue();
	}

	public String getProjectKey() {
		return driver.findElement(By.id("CSV_project_key")).getValue();
	}

	public CsvProjectMappingsPage setExistingProject(String nameOrKey) {
		ImporterProjectsMappingsPage.setProject(driver, "CSV", nameOrKey);
		return this;
	}
}
