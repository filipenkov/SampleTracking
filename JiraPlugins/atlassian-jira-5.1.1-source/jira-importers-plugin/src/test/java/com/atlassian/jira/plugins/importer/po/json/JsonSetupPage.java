/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.json;

import com.atlassian.jira.plugins.importer.po.common.AbstractImporterWizardPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterLogsPage;
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

public class JsonSetupPage extends AbstractImporterWizardPage {

	@FindBy(name = "jsonFile")
	private WebElement jsonFile;

	public String getUrl() {
		return "/secure/admin/views/JsonSetupPage!default.jspa?externalSystem=com.atlassian.jira.plugins.jira-importers-plugin:jsonImporter";
	}

	public JsonSetupPage setJsonFile(String filePath) {
		jsonFile.sendKeys(filePath);
		return this;
	}

	public ImporterLogsPage next() {
		Assert.assertTrue(nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(ImporterLogsPage.class);
	}

	public JsonSetupPage nextWithErrors() {
		Assert.assertTrue(nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(JsonSetupPage.class);
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
