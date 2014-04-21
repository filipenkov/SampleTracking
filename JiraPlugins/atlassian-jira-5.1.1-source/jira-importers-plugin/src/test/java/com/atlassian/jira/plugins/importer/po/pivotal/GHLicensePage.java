/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.pivotal;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.collect.Maps;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class GHLicensePage extends AbstractJiraPage {

    @ElementBy(cssSelector = "a.upm-plugin-details-link")
    PageElement details;

	@Override
	public String getUrl() {
		return "/plugins/servlet/upm#manage/com.pyxis.greenhopper.jira";
	}

	public GHLicensePage setLicense(String license) {
        final PageElement licenseTextArea = elementFinder.find(By.cssSelector("form.upm-license-form.upm-plugin-license-editable textarea"));
        licenseTextArea.clear();
		licenseTextArea.type(license);
		return this;
	}

	public GHLicensePage update() {
        elementFinder.find(By.cssSelector("form.upm-license-form.upm-plugin-license-editable input.submit")).click();
		return this;
	}

	public String getLicenseStatus() {
        final By status = By.cssSelector("dd.upm-plugin-license-status");
        driver.waitUntilElementIsVisible(status);
        return elementFinder.find(status).getText();
	}

    @Override
    public TimedCondition isAt() {
        return details.withTimeout(TimeoutType.SLOW_PAGE_LOAD).timed().isVisible();
    }
}
