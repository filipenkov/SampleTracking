/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.common;

import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.annotation.Nullable;

public abstract class AbstractImporterWizardPage extends AbstractImporterPage {
	@FindBy(id = "nextButton")
	protected WebElement nextButton;

	@FindBy(id = "previousButton")
	protected WebElement previousButton;

	@FindBy(name = "webSudoPassword")
	private WebElement webSudoPassword;

	@FindBy(css = "li.menu-item.active-tab")
	protected WebElement activeTab;

	@FindBy(css = ".admin-active-area h1")
	WebElement pageTitle;

	protected AbstractImporterWizardPage webSudo() {
		final By authenticateButtonLocator = By.id("authenticateButton");
		if (!driver.elementExists(authenticateButtonLocator)) {
			return this;
		}
		webSudoPassword.sendKeys("admin");
		driver.findElement(authenticateButtonLocator).click();
		driver.waitUntilElementIsNotLocated(authenticateButtonLocator);
		return this;
	}

	public String getActiveTabText() {
		return activeTab.getText();
	}

	public String getPageTitle() {
		return pageTitle.getText();
	}

	protected String getActiveTabMeta() {
		return driver.findElement(By.cssSelector("head meta[name='admin.active.tab']")).getAttribute("content");
	}

	public boolean isNextEnabled() {
		return nextButton.isEnabled();
	}

	protected void assertNextEnabled() {
		driver.findElement(By.id("ImporterLogsPage_tab")).click();
		driver.waitUntil(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(@Nullable WebDriver input) {
				return nextButton.isEnabled();
			}
		}, 1);
	}
}
