/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.po.common;

import com.atlassian.jira.plugins.importer.web.ImporterProjectMappingsPage;
import com.atlassian.pageobjects.binder.ValidateState;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import java.util.List;

public class ImporterProjectsMappingsPage<T extends ImporterProjectsMappingsPage<T>> extends AbstractImporterWizardPage {
	@Override
	public String getUrl() {
		return null;
	}

	@ValidateState
	public void validate() {
		Assert.assertEquals("Verifying page is in expected tab", "ImporterProjectMappingsPage", getActiveTabMeta());
		Assert.assertEquals(i18n.getString("jira-importer-plugin.wizard.projectmappings.title"), getPageTitle());
	}

	public ImporterCustomFieldsPage next() {
		Assert.assertTrue("Next button is disabled", nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(ImporterCustomFieldsPage.class);
	}

	public ImporterProjectsMappingsPage nextWithError() {
		assertNextEnabled();
		nextButton.click();
		return pageBinder.bind(ImporterProjectsMappingsPage.class);
	}

	public String getExtProjectName(String id) {
		return driver.findElement(By.id(ImporterProjectMappingsPage.getProjectFieldId(id) + "_extName")).getText();
	}

	// setting project lead is non-trivial.
	public static void fillProjectDetails(AtlassianWebDriver driver, @Nullable String name, @Nullable String key) {
		if (name != null) {
			WebElement element = driver.findElement(By.name("name"));
			element.clear();
			element.sendKeys(name);
		}

		if (key != null) {
			WebElement element = driver.findElement(By.name("key"));
			element.clear();
			element.sendKeys(key);
		}

		final WebElement addButton = driver.findElement(By.name("Add"));
		driver.waitUntil(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(@Nullable WebDriver input) {
				return addButton.isEnabled();
			}
		});
		addButton.click();
		driver.waitUntilElementIsNotLocated(By.name("Add"));
	}

	public ImporterProjectsMappingsPage editProject(String id, @Nullable String name, @Nullable String key, @Nullable String lead) {
		final AddProjectDialog addProjectDialog = openProjectEdit(id);
		addProjectDialog.setName(name);
		addProjectDialog.setKey(key);
		addProjectDialog.submitSuccess(getClass());
		return this;
	}

	public AddProjectDialog openProjectEdit(String id) {
		driver.findElement(By.id(ImporterProjectMappingsPage.getProjectFieldId(id) + "_edit")).click();
		return pageBinder.bind(AddProjectDialog.class);
	}

	public T createProject(String id, @Nullable String name, @Nullable String key) {
		return createProjectImpl(ImporterProjectMappingsPage.getProjectFieldId(id), name, key);
	}

    public boolean isCreateProjectAvailable(String projectName) {
        final String id = ImporterProjectMappingsPage.getProjectFieldId(projectName);
        driver.findElement(By.cssSelector("#" + id + "-select-single-select span.drop-menu")).click();
        return driver.elementExists(By.cssSelector("#" + id + "-select-suggestions a[title='Create New']"));
    }

	protected T createProjectImpl(String id, @Nullable String name, @Nullable String key) {
		driver.findElement(By.cssSelector("#" + id + "-select-single-select span.drop-menu")).click();
		driver.waitUntilElementIsVisible(By.cssSelector("#" + id + "-select-suggestions a[title='Create New']"));
		driver.findElement(By.cssSelector("#" + id + "-select-suggestions a[title='Create New']")).click();
		driver.waitUntilElementIsVisible(By.name("name"));

		final AddProjectDialog addProjectDialog = pageBinder.bind(AddProjectDialog.class);
		addProjectDialog.setName(name);
        addProjectDialog.setKey(key);
		return (T) addProjectDialog.submitSuccess(this.getClass());
	}

	public boolean isProjectImported(String name) {
		return driver.findElement(By.id(ImporterProjectMappingsPage.getProjectFieldId(name) + "_project_check")).isSelected();
	}

	public ImporterProjectsMappingsPage setProjectImported(String name, boolean enabled) {
		setCheckbox(ImporterProjectMappingsPage.getProjectFieldId(name) + "_project_check", enabled);
		return this;
	}

	public ImporterProjectsMappingsPage setImportAllProjects(boolean importAll) {
		final String selector = "input.project_checkbox" + (importAll ? ":not(:checked)" : ":checked");
		driver.executeScript("AJS.$('" + selector + "').click();");
		return this;
	}

	public boolean areAllProjectsSelected() {
		for (WebElement checkbox : driver.findElements(By.cssSelector("input.project_checkbox"))) {
			if (!checkbox.isSelected()) {
				return false;
			}
		}
		return true;
	}

	public boolean setProject(String externalProject, String name) {
		return setProject(driver, ImporterProjectMappingsPage.getProjectFieldId(externalProject), name);
	}

	public static boolean setProject(AtlassianWebDriver driver, String id, String name) {
		final WebElement input = driver.findElement(By.cssSelector("input#" + id + "-select-field"));
		input.clear();
		input.sendKeys(name);

		final WebElement suggestions = driver.findElement(By.id(id + "-select-suggestions"));
		final List<WebElement> suggested = suggestions.findElements(By.partialLinkText(name));
		if (suggested.isEmpty()) {
			Assert.assertFalse("Expecting dropdown to display either suggestion or No Matches",
					suggestions.findElements(By.className("no-suggestions")).isEmpty());
		    driver.executeScript(String.format("AJS.$('input#%s-select-field').blur()", id));
			return false;
		}
		Assert.assertEquals("Expecting test data to be specific enough that exactly 0 or 1 project matches", 1, suggested.size());
		suggested.get(0).click();
		return true;
	}

	// gets error generated by the action. see below for JavaScript-generated one
	@Nullable
	public String getActionErrorMessage(String id) {
		final By msgLocator = By.id(ImporterProjectMappingsPage.getProjectFieldId(id) + "_project_errorMsg");
		return driver.elementExists(msgLocator) ? driver.findElement(msgLocator).getText() : null;
	}

	public boolean hasError(String externalProject) {
		final String id = ImporterProjectMappingsPage.getProjectFieldId(externalProject);
		return driver.elementExists(By.cssSelector("#" + id + "-select-single-select ~ div.error"));
	}

	public String getError(String externalProject) {
		final String id = ImporterProjectMappingsPage.getProjectFieldId(externalProject);
		return driver.findElement(By.cssSelector("#" + id + "-select-single-select ~ div.error")).getText();
	}
}
