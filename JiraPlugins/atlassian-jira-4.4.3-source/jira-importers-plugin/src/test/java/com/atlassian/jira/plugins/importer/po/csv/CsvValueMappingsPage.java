/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.csv;

import com.atlassian.jira.plugins.importer.po.common.AbstractImporterWizardPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterLogsPage;
import org.apache.commons.lang.StringUtils;
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
		String href = driver.findElement(By.cssSelector("a[class~=addConstantLink][class~=" + type + "][class~=" + value + "]")).getAttribute("href");
		driver.navigate().to(StringUtils.substringBefore(driver.getCurrentUrl(), "CsvValueMappingsPage") +  href);
		return pageBinder.bind(CsvValueMappingsPage.class);
	}

	public CsvValueMappingsPage click(WebElement we) {
		we.click();
		return pageBinder.bind(CsvValueMappingsPage.class);
	}
}
