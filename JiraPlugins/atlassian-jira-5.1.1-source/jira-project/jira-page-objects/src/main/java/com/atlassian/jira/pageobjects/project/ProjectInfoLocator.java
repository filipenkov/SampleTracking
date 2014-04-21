package com.atlassian.jira.pageobjects.project;

import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Object that can be used to get the project key/id from the page.
 *
 * @since v4.4
 */
public class ProjectInfoLocator
{
    @Inject
    private PageElementFinder locator;

    public String getProjectKey()
    {
        return locator.find(By.cssSelector("meta[name='projectKey']")).getAttribute("content");
    }

    public long getProjectId()
    {
        return Long.parseLong(locator.find(By.cssSelector("meta[name='projectId']")).getAttribute("content"));
    }
}
