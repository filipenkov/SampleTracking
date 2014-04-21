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

	@Override
	public String getUrl() {
		return "/secure/admin/views/PivotalSetupPage!default.jspa?externalSystem=Pivotal";
	}

	@Override
	public PivotalImporterSetupPage webSudo() {
		super.webSudo();
		return this;
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

	public boolean nextButtonEnabled() {
		return nextButton.isEnabled();
	}

	public PivotalProjectsMappingsPage next() {
		Assert.assertTrue(nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(PivotalProjectsMappingsPage.class);
	}

}
