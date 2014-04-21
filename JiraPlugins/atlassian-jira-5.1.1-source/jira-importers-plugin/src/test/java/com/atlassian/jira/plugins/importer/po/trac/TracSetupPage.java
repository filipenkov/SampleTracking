/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.trac;

import com.atlassian.jira.plugins.importer.po.common.AbstractImporterWizardPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterProjectsMappingsPage;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TracSetupPage extends AbstractImporterWizardPage {


	@FindBy(name = "environmentFile")
	private WebElement environmentFile;

	@FindBy(name = "configFile")
	private WebElement configurationFile;

	@FindBy(name = "encoding")
	private WebElement encoding;

	public String getUrl() {
		return "/secure/admin/views/TracSetupPage!default.jspa?externalSystem=com.atlassian.jira.plugins.jira-importers-plugin:tracImporter";
	}

	public TracSetupPage setEncoding(String encoding) {
		this.encoding.sendKeys(encoding);
		return this;
	}

	public TracSetupPage setConfigurationFile(String configurationFile) {
		this.configurationFile.sendKeys(configurationFile);
		return this;
	}

	public TracSetupPage setEnvironmentFile(String filePath) {
		environmentFile.sendKeys(filePath);
		return this;
	}

	public ImporterProjectsMappingsPage next() {
		Assert.assertTrue(nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(ImporterProjectsMappingsPage.class);
	}

	public TracSetupPage nextWithError() {
		assertNextEnabled();
		nextButton.click();
		return pageBinder.bind(TracSetupPage.class);
	}
}
