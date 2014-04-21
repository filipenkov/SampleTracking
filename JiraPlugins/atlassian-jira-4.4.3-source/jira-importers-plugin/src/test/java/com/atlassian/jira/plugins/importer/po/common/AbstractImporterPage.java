/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.common;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.jira.page.JiraAbstractPage;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;
import java.util.ResourceBundle;

public abstract class AbstractImporterPage extends JiraAbstractPage {
	@Inject
	protected AtlassianWebDriver driver;

	@Inject
	protected PageBinder pageBinder;

	@Init
	@SuppressWarnings("unused")
	void disableNavigateAlert() {
		driver.executeScript("window.selenium = true;");
	}

	protected final ResourceBundle i18n = ResourceBundle.getBundle("com.atlassian.jira.plugins.importer.web.action.util.messages");

	public List<String> getGlobalErrors() {
		try {
			return Lists.newArrayList(
					Collections2
							.transform(driver.findElements(By.cssSelector("div.error")),
									new Function<WebElement, String>() {
										@Override
										public String apply(WebElement input) {
											return input.getText();
										}
									}));
		} catch (NoSuchElementException e) {
			return Lists.newArrayList();
		}
	}

	public List<String> getGlobalErrors2() {
		final List<String> warnings = Lists.newArrayList();

		for (WebElement warningBox : driver.findElements(By.cssSelector("div[class~=aui-message][class~=error]"))) {
			WebElement title = Iterables.getFirst(warningBox.findElements(By.cssSelector("p.title")), null);
			if (title != null) {
				warnings.add(title.getText());
			}

			List<WebElement> list = warningBox.findElements(By.cssSelector("ul li"));
			if (!list.isEmpty()) {
				for (WebElement warning : list) {
					warnings.add(warning.getText());
				}
			} else {
				warnings.add(warningBox.getText());
			}
		}

		return warnings;
	}

	public List<String> getWarnings() {
		final List<String> warnings = Lists.newArrayList();

		for (WebElement warningBox : driver.findElements(By.cssSelector("div[class~=aui-message][class~=warning]"))) {
			WebElement title = Iterables.getFirst(warningBox.findElements(By.cssSelector("p.title")), null);
			if (title != null) {
				warnings.add(title.getText());
			}

			List<WebElement> list = warningBox.findElements(By.cssSelector("ul li"));
			if (!list.isEmpty()) {
				for (WebElement warning : list) {
					warnings.add(warning.getText());
				}
			} else {
				warnings.add(warningBox.getText());
			}
		}

		return warnings;
	}

	public List<String> getFieldErrors() {
		return Lists.newArrayList(
				Collections2.transform(driver.findElements(By.cssSelector("div.field-group > div.error")),
						new Function<WebElement, String>() {
							@Override
							public String apply(WebElement input) {
								return input.getText();
							}
						}));
	}

	public void setSelectByValue(String name, String value) {
		driver.findElement(By.name(name)).findElement(By.xpath("descendant::option[@value='" + value + "']")).setSelected();
	}

	public void setSelect(String name, String value) {
		driver.findElement(By.name(name)).findElement(By.xpath("descendant::option[text()='" + value + "']")).setSelected();
	}

	public List<WebElement> getSelectOptions(String name) {
		return driver.findElement(By.name(name)).findElements(By.xpath("descendant::option"));
	}

	@Nullable
	public String getSelectedOptionText(String name) {
		WebElement select = driver.findElement(By.name(name));
		WebElement element = Iterables.getFirst(select.findElements(By.xpath(
				String.format("descendant::option[@value='%s']", select.getValue()))), null);
		return element != null ? element.getText() : null;
	}

	public void setCheckbox(String id, boolean value) {
		setCheckbox(By.id(id), value);
	}

	public void setCheckbox(By by, boolean value) {
		final WebElement checkbox = driver.findElement(by);
		if (checkbox.isSelected() != value) {
			checkbox.click();
		}
	}

	public void setField(String name, String value) {
		WebElement element = driver.findElement(By.name(name));
		if ("checkbox".equals(element.getAttribute("type"))) {
			setCheckbox(name, Boolean.valueOf(value));
		} else if ("select".equals(element.getTagName())) {
			setSelectByValue(name, value);
		} else {
			element.clear();
			element.sendKeys(value);
		}
	}

	public String getValue(String field) {
		return driver.findElement(By.name(field)).getValue();
	}

	public String getText(String field) {
		return driver.findElement(By.name(field)).getText();
	}


}
