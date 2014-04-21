/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.common;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public abstract class CommonImporterSetupPage extends AbstractImporterWizardPage {

	@FindBy(id = "siteCredentials")
	private WebElement siteCredentials;

	@FindBy(id = "siteUrl")
	private WebElement siteUrl;

	@FindBy(id = "siteUsername")
	private WebElement siteUsername;

	@FindBy(id = "sitePassword")
	private WebElement sitePassword;

	@FindBy(id = "jdbcUsername")
	private WebElement jdbcUsername;

	@FindBy(id = "jdbcDatabase")
	private WebElement jdbcDatabase;

	@FindBy(id = "jdbcPort")
	private WebElement jdbcPort;

	@FindBy(id = "jdbcPassword")
	private WebElement jdbcPassword;

	@FindBy(id = "jdbcHostname")
	private WebElement jdbcHostname;

	@FindBy(id = "databaseType")
	private WebElement databaseType;

	@FindBy(name = "configFile")
	private WebElement configFile;

	public List<String> getFieldErrors() {
		return Lists.transform(driver.findElements(By.cssSelector("td.formErrors .errorArea li")), new Function<WebElement, String>() {
			@Override
			public String apply(WebElement input) {
				return input.getText();
			}
		});
	}

	public CommonImporterSetupPage setSiteCredentials(boolean siteCredentials) {
		if (siteCredentials)  {
			this.siteCredentials.setSelected();
		} else {
			this.siteCredentials.clear();
		}
		return this;
	}

	public CommonImporterSetupPage setSiteUrl(String siteUrl) {
		this.siteUrl.clear();
		this.siteUrl.sendKeys(siteUrl);
		return this;
	}

	public CommonImporterSetupPage setSiteUsername(String siteUsername) {
		this.siteUsername.clear();
		this.siteUsername.sendKeys(siteUsername);
		return this;
	}

	public CommonImporterSetupPage setSitePassword(String sitePassword) {
		this.sitePassword.clear();
		this.sitePassword.sendKeys(sitePassword);
		return this;
	}

	public CommonImporterSetupPage setJdbcPort(String jdbcPort) {
		this.jdbcPort.clear();
		this.jdbcPort.sendKeys(jdbcPort);
		return this;
	}

	public CommonImporterSetupPage setJdbcUsername(String jdbcUsername) {
		this.jdbcUsername.clear();
		this.jdbcUsername.sendKeys(jdbcUsername);
		return this;
	}

	public CommonImporterSetupPage setJdbcPassword(String jdbcPassword) {
		this.jdbcPassword.clear();
		this.jdbcPassword.sendKeys(jdbcPassword);
		return this;
	}

	public CommonImporterSetupPage setJdbcHostname(String jdbcHostname) {
		this.jdbcHostname.clear();
		this.jdbcHostname.sendKeys(jdbcHostname);
		return this;
	}

	public CommonImporterSetupPage setJdbcDatabase(String database) {
		this.jdbcDatabase.clear();
		this.jdbcDatabase.sendKeys(database);
		return this;
	}

	public CommonImporterSetupPage setDatabaseType(String driverName) {
		this.databaseType.clear();
		this.databaseType.sendKeys(driverName);
		return this;
	}

	public CommonImporterSetupPage setConfigFile(String location) {
		setCheckbox("useConfigFile", true);
		configFile.sendKeys(location);
		return this;
	}

	public ImporterProjectsMappingsPage next() {
		assertNextEnabled();
		nextButton.click();
		return pageBinder.bind(ImporterProjectsMappingsPage.class);
	}

	public CommonImporterSetupPage nextWithError() {
		assertNextEnabled();
		nextButton.click();
		return this;
	}

	public CommonImporterSetupPage webSudo() {
		super.webSudo();
		return this;
	}

}
