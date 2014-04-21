/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.csv;

import com.atlassian.jira.plugins.importer.po.common.AbstractImporterWizardPage;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class CsvSetupPage extends AbstractImporterWizardPage {

	@FindBy(css = "#advanced h3.toggle-title")
	private WebElement advanced;

	@FindBy(name = "csvFile")
	private WebElement csvFile;

	@FindBy(name = "configFile")
	private WebElement configurationFile;

	@FindBy(name = "encoding")
	private WebElement encoding;

	@FindBy(name = "delimiter")
	private WebElement delimiter;

	public String getUrl() {
		return "/secure/admin/views/CsvSetupPage!default.jspa?externalSystem=com.atlassian.jira.plugins.jira-importers-plugin:csvImporter";
	}

	public CsvSetupPage setEncoding(String encoding) {
		showAdvanced();
		this.encoding.sendKeys(encoding);
		return this;
	}

	public CsvSetupPage setDelimiter(String delimiter) {
		showAdvanced();
		this.delimiter.clear();
		this.delimiter.sendKeys(delimiter);
		return this;
	}

	public void showAdvanced() {
		driver.executeScript("importer.toggle.expand(\"#advanced\");");
	}

	public CsvSetupPage setConfigurationFile(String configurationFile) {
		setCheckbox("useConfigFile", true);
		this.configurationFile.sendKeys(configurationFile);
		return this;
	}

	public CsvSetupPage setCsvFile(String filePath) {
		csvFile.sendKeys(filePath);
		return this;
	}

	public CsvProjectMappingsPage next() {
		Assert.assertTrue(nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(CsvProjectMappingsPage.class);
	}

}
