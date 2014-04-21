/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.pivotal;

import com.atlassian.jira.plugins.importer.po.common.AbstractImporterWizardPage;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class PivotalImporterSetupPage extends AbstractImporterWizardPage {

	@FindBy(id = "siteUsername")
	WebElement username;

	@FindBy(id = "sitePassword")
	WebElement password;

	@FindBy(name = "configFile")
	private WebElement configFile;

	@Override
	public String getUrl() {
		return "/secure/admin/views/PivotalSetupPage!default.jspa?externalSystem=com.atlassian.jira.plugins.jira-importers-plugin:pivotalTrackerImporter";
	}

	public PivotalImporterSetupPage setUsername(String username) {
		this.username.sendKeys(username);
		driver.executeScript("AJS.$('#siteUsername').change();");
		return this;
	}

	public PivotalImporterSetupPage setPassword(String password) {
		this.password.sendKeys(password);
		driver.executeScript("AJS.$('#sitePassword').change();");
		return this;
	}

	public PivotalImporterSetupPage showAdvanced() {
		driver.executeScript("AJS.$('#importerSetupPage-advanced.collapsed').removeClass('collapsed').addClass('expanded')");
		return this;
	}

	public PivotalImporterSetupPage setMapUsernames(boolean mapUsernames) {
		setCheckbox("showUsernameMapping", mapUsernames);
		return this;
	}

	public PivotalImporterSetupPage setConfigFile(String location) {
		setCheckbox("useConfigFile", true);
		configFile.sendKeys(location);
		return this;
	}

	public boolean nextButtonEnabled() {
		return nextButton.isEnabled();
	}

	public PivotalProjectsMappingsPage next() {
		Assert.assertTrue(nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(PivotalProjectsMappingsPage.class);
	}

}
