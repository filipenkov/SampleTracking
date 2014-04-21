/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.common;

import com.atlassian.webdriver.utils.element.ElementLocated;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Set;

public class ImporterLogsPage extends AbstractImporterPage {

	@FindBy(name = "submitButton")
	private WebElement abortButton;

	public String getUrl() {
		return "/secure/admin/views/ImporterLogsPage!viewLogs.jspa";
	}

	public ImporterFinishedPage waitUntilFinished() {
        driver.waitUntilElementIsLocated(By.id("whatNow"));
		return pageBinder.bind(ImporterFinishedPage.class);
	}

	public ImporterFinishedPage waitUntilFinished(int timeoutInSeconds) {
		driver.waitUntil(new ElementLocated(By.id("whatNow"), null), timeoutInSeconds);
		return pageBinder.bind(ImporterFinishedPage.class);
	}

	public String getLog() {
		final List<WebElement> downloads = driver.findElements(By.linkText("download"));
		for (WebElement download : downloads) {
			final String href = download.getAttribute("href");
			if (StringUtils.endsWith(href, "/log")) {
				final String originalWindowHandle = driver.getWindowHandle();
				final Set<String> before = driver.getWindowHandles();
				download.click();
				driver.waitUntil(new Function() {
					@Override
					public Object apply(Object input) {
						final Set<String> after = driver.getWindowHandles();
						final Sets.SetView<String> difference = Sets.difference(after, before);
						return !difference.isEmpty() && StringUtils.isNotBlank(difference.iterator().next());
					}
				});
				final Set<String> after = driver.getWindowHandles();
				final String logHandle = Iterables.getOnlyElement(Sets.difference(after, before));
				final WebDriver window = driver.switchTo().window(logHandle);
				final String log = window.getPageSource();
				window.close();
				driver.switchTo().window(originalWindowHandle);
				return log;
			}
		}
		throw new NotFoundException("Download link for log not found");
	}
}
