/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.csv;

import com.atlassian.jira.plugins.importer.po.common.AbstractImporterWizardPage;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.Collections;
import java.util.List;

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
		return "/secure/admin/views/CsvSetupPage!default.jspa?externalSystem=CSV";
	}

	public CsvSetupPage webSudo() {
		super.webSudo();
		return this;
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

	public List<String> getGeneralErrors() {
		try {
			return Lists.newArrayList(Collections2.transform(driver.findElements(By.cssSelector("#jimform > div.error")),
					new Function<WebElement, String>() {
						@Override
						public String apply(WebElement input) {
							return input.getText();
						}
					}));
		} catch (NoSuchElementException e) {
			return Collections.emptyList();
		}
	}

}
