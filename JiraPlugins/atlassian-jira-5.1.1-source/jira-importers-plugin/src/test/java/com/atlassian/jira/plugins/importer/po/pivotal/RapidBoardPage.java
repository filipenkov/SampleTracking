/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.pivotal;

import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;

public class RapidBoardPage implements Page {
	@Inject
	protected AtlassianWebDriver driver;

	@Inject
	protected PageBinder pageBinder;

	@Override
	public String getUrl() {
		return "/secure/RapidBoard.jspa";
	}

	public LinkedHashMap<String, String> getColumnHeaders() {
		final LinkedHashMap<String, String> columns = Maps.newLinkedHashMap();
		final By by = By.cssSelector(".ghx-column-header-group .ghx-column-headers .ghx-column");
		driver.waitUntilElementIsVisible(by);
		for(WebElement we : driver.findElements(by)) {
			columns.put(we.findElement(By.tagName("h2")).getText(), we.getAttribute("data-id"));
		};
		return columns;
	}

	public List<String> getIssuesForColumnInFirstSwimlane(String columnId) {
		By by = By.cssSelector(
				String.format("div.ghx-swimlane.ghx-first ul.ghx-columns li.ghx-column[data-column-id='%s']"
						+ " div.ghx-issue div.ghx-key a", columnId));
		return Immutables.transformThenCopyToList(driver.findElements(by), new Function<WebElement, String>() {
            @Override
            public String apply(@Nullable WebElement webElement) {
                return webElement.getAttribute("title");
            }
        });
	}
}
