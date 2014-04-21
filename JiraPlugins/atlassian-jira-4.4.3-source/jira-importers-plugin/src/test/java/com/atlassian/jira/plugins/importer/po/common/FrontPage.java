/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.common;

import com.atlassian.pageobjects.binder.ValidateState;
import junit.framework.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FrontPage extends AbstractImporterWizardPage {
	@FindBy(css = "li.menu-item.active-tab")
	private WebElement activeTab;

	@ValidateState
	public void validate() {
		Assert.assertEquals("Verifying page is in expected tab", "external_import", driver.findElement(By.cssSelector("head meta[name='admin.active.tab']")).getAttribute("content"));
		Assert.assertEquals("Verifying active tab", "External System Import", activeTab.getText());
		Assert.assertTrue("Verifying current url", driver.getCurrentUrl().endsWith(getUrl()));
	}

	@Override
	public String getUrl() {
		return "/secure/admin/views/ExternalImport1.jspa";
	}

	public HasPrevious clickImporter(String importer) {
		driver.findElement(By.id("import_" + importer)).click();
		return pageBinder.bind(HasPrevious.class);
	}

	@Override
	public FrontPage webSudo() {
		return (FrontPage) super.webSudo();	//To change body of overridden methods use File | Settings | File Templates.
	}
}
