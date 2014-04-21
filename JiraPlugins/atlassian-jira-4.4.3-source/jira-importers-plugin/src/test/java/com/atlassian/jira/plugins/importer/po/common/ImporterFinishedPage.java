/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.common;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImporterFinishedPage extends AbstractImporterPage {

	@FindBy(id = "projectsImported")
	private WebElement projectsImported;

	@FindBy(id = "issuesImported")
	private WebElement issuesImported;

	public String getUrl() {
		return "/secure/admin/views/ImporterFinishedPage!default.jspa";
	}

	public boolean isSuccess() {
		return !driver.elementExists(By.className("error"));
	}

	public String getLog() {
		final List<WebElement> downloads = driver.findElements(By.linkText("download a detailed log"));
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

	private final Pattern successMessagePattern = Pattern.compile("(\\d+) projects and (\\d+) issues imported successfully!");

	private String parseSuccessMessage(int group) {
		final Matcher matcher = successMessagePattern.matcher(projectsImported.getText());
		Assert.assertTrue(matcher.find());
		return matcher.group(group);
	}

	public String getProjectsImported() {
		return parseSuccessMessage(1);
	}

	public String getIssuesImported() {
		return parseSuccessMessage(2);
	}
}
