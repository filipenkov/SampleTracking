/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.fogbugz.hosted;

import com.atlassian.jira.plugins.importer.po.common.AbstractImporterWizardPage;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FogBugzHostedImporterSetupPage extends AbstractImporterWizardPage {
	@FindBy(id = "siteUsername")
	WebElement username;

	@FindBy(id = "sitePassword")
	WebElement password;

	@FindBy(id = "siteUrl")
	WebElement url;

	@Override
	public FogBugzHostedImporterSetupPage webSudo() {
		super.webSudo();
		return this;
	}

	public FogBugzHostedImporterSetupPage setSiteUsername(String username) {
		this.username.sendKeys(username);
		driver.executeScript("AJS.$('#siteUsername').change();");
		return this;
	}

	public FogBugzHostedImporterSetupPage setSiteUrl(String url) {
		this.url.sendKeys(url);
		driver.executeScript("AJS.$('#siteUrl').change();");
		return this;
	}

	public FogBugzHostedImporterSetupPage setSitePassword(String password) {
		this.password.sendKeys(password);
		driver.executeScript("AJS.$('#sitePassword').change();");
		return this;
	}

	@Override
	public String getUrl() {
		return "/secure/admin/views/FogBugzHostedSetupPage!default.jspa?externalSystem=FogBugzHosted";
	}

	public FogBugzHostedProjectMappingsPage next() {
		Assert.assertTrue(nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(FogBugzHostedProjectMappingsPage.class);
	}
}
