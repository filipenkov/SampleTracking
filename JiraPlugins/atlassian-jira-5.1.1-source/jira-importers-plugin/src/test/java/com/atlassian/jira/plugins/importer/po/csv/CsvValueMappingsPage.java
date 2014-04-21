/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.csv;

import com.atlassian.jira.plugins.importer.po.common.AbstractImporterWizardPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterLogsPage;
import com.atlassian.webdriver.utils.by.ByJquery;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class CsvValueMappingsPage extends AbstractImporterWizardPage {


	public String getUrl() {
		return "/secure/admin/views/CsvValueMappingsPage!default.jspa?externalSystem=CSV";
	}

	public ImporterLogsPage next() {
		Assert.assertTrue(nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(ImporterLogsPage.class);
	}

	public List<WebElement> getAddConstantLinks() {
		return driver.findElements(By.cssSelector("a.addConstantLink"));
	}

	public CsvValueMappingsPage addConstant(String type, String value) {
		final WebElement addConstantLink = driver.findElement(ByJquery.$(String.format("a.addConstantLink:contains(Add new %s):contains(%s)", type, value)));
		addConstantLink.click();
		return pageBinder.bind(CsvValueMappingsPage.class);
	}

	public CsvValueMappingsPage click(WebElement we) {
		we.click();
		return pageBinder.bind(CsvValueMappingsPage.class);
	}
}
