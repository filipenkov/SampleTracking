/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.csv;

import com.atlassian.jira.pageobjects.components.fields.AutoComplete;
import com.atlassian.jira.pageobjects.components.fields.QueryableDropdownSelect;
import com.atlassian.jira.pageobjects.components.fields.Suggestion;
import com.atlassian.jira.plugins.importer.po.common.AbstractImporterWizardPage;
import com.atlassian.jira.util.lang.Pair;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.annotation.Nullable;
import java.util.List;

public class CsvFieldMappingsPage extends AbstractImporterWizardPage {

	@FindBy(css = ".jim-hints-section.nextButtonHints")
	private WebElement hintSection;


	public String getUrl() {
		return "/secure/admin/views/CsvFieldMappingsPage!default.jspa?externalSystem=CSV";
	}

	public List<Pair<String, String>> getUnmappedFields() {
		List<Pair<String, String>> fields = Lists.newArrayList();
		driver.findElement(By.cssSelector("#unmappedFieldsWarning a.showMore")).click();
		try {
			List<WebElement> rows = driver.findElement(By.id("unmappedFields")).findElements(By.xpath("tbody/tr"));
			for (WebElement row : rows) {
				List<WebElement> cols = row.findElements(By.xpath("td"));
				fields.add(Pair.of(cols.get(0).getText(), cols.get(2).getText()));
			}
		} finally {
			driver.findElement(By.cssSelector("#unmappedFieldsWarning a.showMore")).click();
		}
		return fields;
	}


	public List<WebElement> getMappingFields() {
		return driver.findElements(By.className("importField"));
	}

	public List<WebElement> getMappingCheckboxes() {
		return driver.findElements(By.className("manual-mapping-checkbox"));
	}


	public CsvFieldMappingsPage setImportCheckbox(String fieldName, boolean isChecked) {
		final String encodedFieldName = com.atlassian.jira.plugins.importer.imports.csv.web.CsvFieldMappingsPage
				.getFieldName(fieldName);
		final WebElement checkbox = driver
				.findElement(By.cssSelector("tr#" + encodedFieldName + " .field-mapping-checkbox"));
		if (isChecked != checkbox.isSelected()) {
			checkbox.click();
		}
		return this;
	}

	public CsvFieldMappingsPage setFieldMappingByVal(String fieldName, @Nullable String mappingVal) {
		final String fieldId = com.atlassian.jira.plugins.importer.imports.csv.web.CsvFieldMappingsPage.getFieldName(fieldName);
		final String extractor = "return AJS.$(\"select#" + fieldId + "-mapping option[value='" + mappingVal + "']\").text();";
		final String display = StringUtils.trim((String) driver.executeScript(extractor));
		return setFieldMapping(fieldName, display);
	}

	public CsvFieldMappingsPage setFieldMapping(String fieldName, @Nullable String mapping) {
		setImportCheckbox(fieldName, mapping != null);
		final Suggestion activeSuggestion = getFieldMappingSelect(fieldName).query(mapping).getActiveSuggestion();
		activeSuggestion.click();
		return this;
	}

	public String getDisplayedFieldMapping(String fieldName) {
		final String fieldId = com.atlassian.jira.plugins.importer.imports.csv.web.CsvFieldMappingsPage.getFieldName(fieldName);
		return driver.findElement(By.id(fieldId + "-mapping-field")).getAttribute("value");
	}

	public CsvFieldMappingsPage setMapValues(String fieldName, boolean value) {
		final WebElement checkbox = driver.findElement(By.id("manual-mapping-"
				+ com.atlassian.jira.plugins.importer.imports.csv.web.CsvFieldMappingsPage.getFieldName(fieldName)));
		if (checkbox.isSelected() != value) {
			checkbox.click();
		}
		return this;
	}

	public String getTargetFieldName(String fieldName) {
		return com.atlassian.jira.plugins.importer.imports.csv.web.CsvFieldMappingsPage.getFieldName(fieldName) + "-mapping";
	}

	public CsvValueMappingsPage next() {
		Assert.assertTrue(nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(CsvValueMappingsPage.class);
	}

	public CsvFieldMappingsPage nextWithError() {
		assertNextEnabled();
		nextButton.click();
		return pageBinder.bind(CsvFieldMappingsPage.class);
	}

	public CsvCreateCustomFieldPage newCustomField(String fieldName) {
		setFieldMapping(fieldName, "New Custom Field");
		driver.waitUntilElementIsVisible(By.cssSelector(".aui-popup.aui-dialog-content-ready"));
		return pageBinder.bind(CsvCreateCustomFieldPage.class);
	}

	public String getHintSectionText() {
		return hintSection.getText();
	}

	public boolean hasDuplicateColumnInfo(String fieldName) {
		return !driver.findElements(By.cssSelector(String.format("tr#%s td span.duplicate-column-info",
				com.atlassian.jira.plugins.importer.imports.csv.web.CsvFieldMappingsPage.getFieldName(fieldName)))).isEmpty();
	}

	public String getDuplicateColumnMessage(String fieldName) {
		String fieldId = com.atlassian.jira.plugins.importer.imports.csv.web.CsvFieldMappingsPage.getFieldName(fieldName);
		driver.executeScript(
				String.format("AJS.$(\"tr#%s td span.duplicate-column-info\").trigger(\"mousemove\");", fieldId));
		String message = driver
				.executeScript("return AJS.$(\".aui-inline-dialog div.contents div:first-child\").text();").toString();
		driver.executeScript(
				String.format("AJS.$(\"tr#%s td span.duplicate-column-info\").trigger(\"mouseout\");", fieldId));
		return message;
	}

	public List<String> getHints() {
		return Lists.newArrayList(Iterables.filter(Iterables
				.transform(driver.findElements(By.cssSelector("#importer-info-box p")), ITUtils.TEXT_FUNCTION),
						Predicates.not(Predicates.or(Predicates.<String>isNull(), Predicates.equalTo("")))));
	}

	public AutoComplete getFieldMappingSelect(String fieldName) {
		final String fieldId = com.atlassian.jira.plugins.importer.imports.csv.web.CsvFieldMappingsPage.getFieldName(fieldName);
		return pageBinder.bind(QueryableDropdownSelect.class, By.id(fieldId + "-mapping-single-select"), By.id(fieldId + "-mapping-suggestions"));
	}
}
