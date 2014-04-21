/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.common;

import com.atlassian.jira.plugins.importer.imports.HttpDownloader;
import com.atlassian.jira.plugins.importer.po.pivotal.RapidBoardPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.ProductInstance;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImporterFinishedPage extends AbstractImporterPage implements Page {
	@Inject
	protected PageBinder pageBinder;

	@Inject
	protected ProductInstance productInstance;

	@FindBy(id = "projectsImported")
	private WebElement projectsImported;

	@FindBy(id = "issuesImported")
	private WebElement issuesImported;

	private String externalSystem;

	public ImporterFinishedPage() {
	}

	public ImporterFinishedPage(String externalSystem) {
		this.externalSystem = externalSystem;
	}

	public String getUrl() {
		return "/secure/admin/views/ImporterFinishedPage!default.jspa"
				+ (externalSystem != null ? ("?externalSystem=" + externalSystem) : "");
	}

	public boolean isSuccess() {
		return !driver.elementExists(By.className("error"));
	}

	public boolean isSuccessWithNoWarnings() {
		return !driver.elementExists(By.className("error")) && getWarnings().isEmpty();
	}

	protected String downloadSource(final WebElement download) {
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

	public String getLog() {
		final WebElement downloadLink = Iterables.getOnlyElement(
				driver.findElements(By.linkText("download a detailed log")));

		final String log = downloadSource(downloadLink);
		if (log == null) {
			throw new NotFoundException("Download link for log not found");
		}
		return log;
	}

	public String getConfiguration() throws IOException {
		final WebElement downloadLink = Iterables.getOnlyElement(
				driver.findElements(By.linkText("save the configuration")));
		final String href = downloadLink.getAttribute("href");
		final Set<Cookie> cookies = driver.manage().getCookies();
		final HttpDownloader downloader = new HttpDownloader();
		final HttpContext context = downloader.createHttpContext();
		final CookieStore store = (CookieStore) context.getAttribute(ClientContext.COOKIE_STORE);
		for(Cookie cookie : cookies) {
			BasicClientCookie httpCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
			httpCookie.setDomain(cookie.getDomain());
			httpCookie.setPath(cookie.getPath());
			httpCookie.setExpiryDate(cookie.getExpiry());
			store.addCookie(httpCookie);
		}

		return IOUtils.toString(new FileInputStream(downloader.getAttachmentFromUrl(context, href, href)));
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

	public boolean isImportAgainVisible() {
		return !driver.findElements(By.cssSelector("#importAgain a")).isEmpty();
	}

	public Map<String, String> getRapidBoardLinks() {
		final Map<String, String> result = Maps.newHashMap();
		final List<WebElement> links = driver.findElements(By.cssSelector("#rapidBoardLinks span a"));
		for(WebElement link : links) {
			result.put(link.getText(), link.getAttribute("href"));
		}
		return result;
	}

	public RapidBoardPage gotoRapidBoard(String name) {
		driver.navigate().to(getRapidBoardLinks().get(name));
		return pageBinder.bind(RapidBoardPage.class);
	}
}
